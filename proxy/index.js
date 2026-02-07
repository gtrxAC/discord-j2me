require('dotenv').config();
const express = require('express');
const axios = require('axios');
const FormData = require('form-data');
const multer = require('multer')
const path = require('path');
const sanitizeHtml = require('sanitize-html');
const crypto = require('crypto').webcrypto;
const { LRUCache } = require('lru-cache');
const WebSocket = require('ws');

const storage = multer.memoryStorage()
const upload = multer({ storage: storage })

const EmojiConvertor = require('emoji-js');
const emoji = new EmojiConvertor();
emoji.replace_mode = 'unified';

const PORT = process.env.PORT || 8080;
const BASE = "/api/v9";
const BASE_L = "/api/l";
const DEST_BASE = "https://discord.com/api/v9";

const app = express();
app.set('view engine', 'ejs');
app.set('views', './views');
app.use(express.static(path.join(__dirname, 'static'), {extensions: ['html']}));
app.use(defaultContentType);
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const uploadTokens = new Map();

// ID -> username mapping cache (used for parsing mentions)
const userCache = new LRUCache({max: 10000});
const channelCache = new LRUCache({max: 10000});

function handleError(res, e) {
    if (e.response) {
        console.log(e.response);
        res.status(e.response.status).send(e.response.data ?? e.response.statusText);
    } else {
        console.log(e);
        res.status(500).send('Proxy error');
    }
}

function stringifyUnicode(obj) {
    return JSON.stringify(obj)
        .replace(/[\u007F-\uFFFF]/g, (match) => {
            return '\\u' + ('0000' + match.charCodeAt(0).toString(16)).slice(-4);
        });
}

function defaultContentType(req, res, next) {
    if (req.headers["content-type"] === undefined || !req.headers["content-type"].startsWith("multipart/form-data")) {
        req.headers["content-type"] = "application/json";
    }
    next();
}

function getTokenFromUploadToken(token) {
    if (!token.startsWith("j2me-")) return token;
    if (!uploadTokens.has(token)) return token;

    const uploadToken = uploadTokens.get(token);
    if (new Date() > uploadToken.expires) return token;

    return uploadToken.token;
}

function getToken(req, res, next) {
    let token;

    if (req.query?.token) {
        token = req.query.token;
    }
    else if (req.headers?.authorization) {
        token = req.headers.authorization;
    }
    else if (req.body?.token) {
        token = req.body.token;
        delete req.body.token;
    }

    if (!token) {
        res.status(400).send({message: "Failed to receive token. Check your token or try changing the \"Send token as\" option in the login screen."});
        return;
    }

    // Check that the token is of the correct length (at least 70 characters, could be slightly shorter with some user IDs, not sure)
    if (!token.startsWith("j2me-")) {
        if (token.length == 62) {
            res.status(400).send({message: "Token is too short. In some cases, the token may get cut off if supplied via manifest or JAD. Please check for missing characters."});
            return;
        }
        else if (token.length < 65) {
            res.status(400).send({message: "Token is written or copied incorrectly (too short)"});
            return;
        }
    }

    if (req.route.path == `${BASE}/channels/:channel/upload`) {
        res.locals.uploadToken = token;
        token = getTokenFromUploadToken(token);
    } else {
        // Remove any characters that should not be in a token (leftover whitespace, quotation marks, control characters, etc)
        token = token.replace(/[^\w\-\+\/\=\.]/g, "");
    }

    if (token.length < 60) {
        res.status(400).send({message: "Token is written or copied incorrectly (invalid characters)"});
        return;
    }

    // https://docs.discord.food/reference#client-properties
    // Using android app's super props because it may be suspicious if we use web client's props and the version number never updates. On android it would be more plausible that someone doesn't auto update the app.
    const superProps = {
        "os": "Android",
        "browser": "Discord Android",
        "device": "a20e", // Samsung Galaxy A20e
        "system_locale": "en-US",
        "has_client_mods": false,
        "client_version": "262.5 - rn",
        "release_channel": "alpha",
        "device_vendor_id": "17503929-a4b8-4490-87bf-0222adfdadc8",
        "design_id": 2,
        "browser_user_agent": "",
        "browser_version": "",
        "os_version": "34", // Android 14
        "client_build_number": 3463,
        "client_event_source": null
    }

    // Headers taken from Firefox HTTP request, some may be unnecessary
    // I don't know which headers the Android app sends
    res.locals.headers = {
        "User-Agent": "Discord-Android/262205;RNA",
        "Authorization": token,
        "X-Super-Properties": btoa(JSON.stringify(superProps)),
        "X-Discord-Locale": "en-US",
        "X-Discord-Timezone": "Europe/Kyiv",

        // these I'm not sure about
        "Accept": "*/*",
        "Accept-Language": "en-US,en;q=0.9",
        "Accept-Encoding": "gzip, deflate, br, zstd",
        // "X-Debug-Options": "bugReporterEnabled",
        "Alt-Used": "discord.com",
        // "Connection": "keep-alive",
        "Cookie": "locale=en-US",

        // these are likely unnecessary
        // "Referer": "https://discord.com/channels/.../...",
        // "Sec-Fetch-Dest": "empty",
        // "Sec-Fetch-Mode": "cors",
        // "Sec-Fetch-Site": "same-origin",
        // "Priority": "u=0",
    };

    // Get user ID from token (base64 decode the token up to the first period)
    try {
        let idPart = token.split('.')[0];
        try {
            res.locals.userID = atob(idPart);
            BigInt(res.locals.userID);  // verify that it is a valid numeric ID
        } catch (e) {
            idPart = idPart.replace(/\-/g, "+").replace(/_/g, "/");  // base64url?
            res.locals.userID = atob(idPart);
            BigInt(res.locals.userID);
        }
    }
    catch (e) {
        res.status(400).send({message: "Token is written incorrectly"});
        return;
    }
    next();
}

function parseMessageContent(content, showGuildEmoji, convertTags = true) {
    if (!content) return content;
    let result = content;

    if (convertTags) {
        // try to convert <@12345...> format into @username
        result = result.replace(/<@(\d{15,})>/gm, (mention, id) => {
            if (userCache.has(id)) return `@${userCache.get(id)}`;
            else return mention;
        })
        // try to convert <#12345...> format into #channelname
        .replace(/<#(\d{15,})>/gm, (mention, id) => {
            if (channelCache.has(id)) return `#${channelCache.get(id)}`;
            else return mention;
        })
    }

    if (!showGuildEmoji) {
        // replace <:name:12345...> emoji format with :name:
        result = result.replace(/<a?(:\w*:)\d{15,}>/gm, "$1")
    }

    // Replace Unicode emojis with :name: textual representations
    emoji.colons_mode = true;
    result = emoji.replace_unified(result);

    // Replace regional indicator emojis with textual representations
    result = result.replace(/\ud83c[\udde6-\uddff]/g, match => {
        return ":regional_indicator_"
            + String.fromCharCode(match.charCodeAt(1) - 0xdde6 + 97)
            + ":";
    })

    return result;
}

function parseMessageObject(msg, req, showGuildEmoji, showEdited) {
    const result = {
        id: msg.id
    }
    if (showEdited && msg.edited_timestamp) {
        result.edited_timestamp = msg.edited_timestamp;
    }
    if (msg.author) {
        result.author = {
            id: msg.author.id,
            avatar: msg.author.avatar,
            global_name: msg.author.global_name
        }
        if (msg.author.global_name == null || req.query.droidcord) {
            result.author.username = msg.author.username;
        }
    }
    if (msg.type >= 1 && msg.type <= 11) result.type = msg.type;

    // Parse content 
    if (msg.content) {
        result.content = parseMessageContent(msg.content, showGuildEmoji);
        if (result.content != msg.content) result._rc = msg.content;
    }

    if (msg.referenced_message) {
        let content = parseMessageContent(msg.referenced_message.content, showGuildEmoji);

        // Replace newlines with spaces (reply is shown as one line)
        content = content.replace(/\r\n|\r|\n/gm, "  ");

        if (showGuildEmoji) {
            // Show 80 characters, but ensure that server emojis don't get cut off in-between
            // (could do this for standard emojis too)
            let newContent = content.slice(0, 80);
            i = 81;
            while (i < content.length && newContent.lastIndexOf(">") < newContent.lastIndexOf("<")) {
                newContent = content.slice(0, i);
                i++;
            };
            content = newContent;
        } else {
            if (content && content.length > 50) {
                content = content.slice(0, 47).trim() + '...';
            }
        }
        result.referenced_message = {
            author: {
                global_name: msg.referenced_message.author.global_name,
                id: msg.referenced_message.author.id,
                avatar: msg.referenced_message.author.avatar
            },
            content
        }
        if (msg.referenced_message.author.global_name == null || req.query.droidcord) {
            result.referenced_message.author.username =
                msg.referenced_message.author.username;
        }
    }

    if (msg.attachments?.length) {
        result.attachments = msg.attachments
            .map(att => {
                var ret = {
                    filename: att.filename,
                    size: att.size,
                    width: att.width,
                    height: att.height,
                    proxy_url: att.proxy_url
                };
                if (req.query.droidcord) {
                    ret.content_type = att.content_type;
                }
                return ret;
            })
    }
    if (msg.sticker_items?.length) {
        result.sticker_items = [{name: msg.sticker_items[0].name}];
    }
    if (msg.embeds?.length) {
        result.embeds = msg.embeds.map(emb => {
            var ret = {
                title: parseMessageContent(emb.title, true, false),
                description: parseMessageContent(emb.description, true, false)
            };
            if (req.query.droidcord) {
                ret.url = emb.url;
                ret.author = emb.author;
                ret.provider = emb.provider;
                ret.footer = emb.footer;
                ret.timestamp = emb.timestamp;
                ret.color = emb.color;
                ret.thumbnail = emb.thumbnail;
                ret.image = emb.image;
                ret.video = emb.video;
                ret.fields = emb.fields;
            }
            return ret;
        })
    }

    // Need first mentioned user for group DM join/leave notification messages
    if ((msg.type == 1 || msg.type == 2) && msg.mentions.length) {
        result.mentions = [
            {
                id: msg.mentions[0].id,
                global_name: msg.mentions[0].global_name
            }
        ]
        if (msg.mentions[0].global_name == null) {
            result.mentions[0].username = msg.mentions[0].username;
        }
    }

    return result;
}

function generateLiteIDHash(id) {
    return (BigInt(id)%100000n).toString(36);
}

function generateUploadToken(token) {
    const randArr = new Uint8Array(16);
    crypto.getRandomValues(randArr);
    const result = "j2me-" + new Array(...randArr).map(n => n.toString(16)).join('');
    const expires = new Date();
    expires.setDate(expires.getDate() + 7);
    uploadTokens.set(result, {token, expires});
    return result;
}

// Get user's server list
// Has cache which can be used with query parameter "c". This param is included by newer clients except when the list is force refreshed
const userGuilds = new LRUCache({max: 200});

app.get(`${BASE}/users/@me/guilds`, getToken, async (req, res) => {
    try {
        if (req.query.c !== undefined && userGuilds.has(res.locals.userID)) {
            res.send(userGuilds.get(res.locals.userID));
        } else {
            const response = await axios.get(
                `${DEST_BASE}/users/@me/guilds`,
                {headers: res.locals.headers}
            );
            const guilds = response.data.map(g => {
                const result = {id: g.id, name: g.name};
                if (g.icon != null) result.icon = g.icon;
                return result;
            })
            const guildsStr = stringifyUnicode(guilds);
            userGuilds.set(res.locals.userID, guildsStr);
            res.send(guildsStr);
        }
    }
    catch (e) { handleError(res, e); }
});

// Get server channels
app.get(`${BASE}/guilds/:guild/channels`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/guilds/${req.params.guild}/channels`,
            {headers: res.locals.headers}
        )

        // Populate channel name cache
        response.data.forEach(ch => {
            channelCache.set(ch.id, ch.name);
        })

        const channels = response.data
            .filter(ch => [0, 2, 5, 15, 16].includes(ch.type))
            .map(ch => {
                return {
                    id: ch.id,
                    type: ch.type,
                    guild_id: ch.guild_id,
                    name: ch.name,
                    position: ch.position,
                    last_message_id: ch.last_message_id
                }
            });
        res.send(stringifyUnicode(channels));
    }
    catch (e) { handleError(res, e); }
});

app.get(`/upload`, async (req, res) => {
    res.send(
`<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload</title>
</head>
<body>
    <p>File uploading is currently disabled due to concerns about Discord flagging file uploads from third-party clients as spam.</p>
    <p>If you want to try uploading anyway, <a href="${req.url.replace('/upload', '/upload2')}">click here</a> at your own risk.</p>
</body>
</html>`
    )
})

// File upload form
app.get(`/upload2`, async (req, res) => {
    try {
        if (!req.query?.channel || !req.query?.token) {
            res.send(`<p>Token or destination channel not defined</p>`);
            return;
        }

        const reply = req.query.reply;
        let username, content;

        if (reply) {
            const messageData = await axios.get(
                `${DEST_BASE}/channels/${req.query.channel}/messages?around=${reply}&limit=1`,
                {headers: {Authorization: getTokenFromUploadToken(req.query.token)}}
            );
            username = messageData.data[0].author.global_name ?? messageData.data[0].author.username ?? "(no name)";
            content = messageData.data[0].content ?? "";
        }

        res.send(
`<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload</title>
</head>
<body>
    <h1>Upload file</h1>
    ${reply ? `<p>Replying to ${sanitizeHtml(username)}</p><p>${sanitizeHtml(content.slice(0, 50))}</p>` : ""}
    <form method="post" enctype="multipart/form-data" action="${BASE}/channels/${req.query.channel}/upload">
        <input type="hidden" name="token" value="${req.query.token}" />
        <input type="hidden" name="bypass" value="1" />
        ${reply ? `<input type="hidden" name="reply" value="${reply}" />` : ""}

        <label for="file">File:</label><br />
        <input type="file" name="files" id="files"></input><br />

        <label for="content">Text:</label><br />
        <textarea name="content" id="content"></textarea><br />

        ${reply ? `<input type="checkbox" name="ping" id="ping" checked></input>` : ""}
        ${reply ? `<label for="ping">Mention author</label><br />` : ""}

        <input type="submit" value="Upload" />
    </form>
</body>
</html>`
        );
    }
    catch (e) { handleError(res, e); }
});

// Get DM channels
app.get(`${BASE}/users/@me/channels`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/users/@me/channels`,
            {headers: res.locals.headers}
        );
        const channels = response.data
            .filter(ch => ch.type == 1 || ch.type == 3)
            .map(ch => {
                const result = {
                    id: ch.id,
                    type: ch.type,
                    last_message_id: ch.last_message_id
                }

                // Add name and icon for group DMs, recipient name and avatar for normal DMs
                if (ch.type == 3) {
                    result.name = ch.name;
                    if (ch.icon != null) result.icon = ch.icon;
                } else {
                    result.recipients = [{
                        global_name: ch.recipients[0].global_name,
                    }];

                    if (ch.recipients[0].avatar != null) {
                        result.recipients[0].id = ch.recipients[0].id;
                        result.recipients[0].avatar = ch.recipients[0].avatar;
                    }
                    if (ch.recipients[0].global_name == null) {
                        result.recipients[0].username = ch.recipients[0].username;
                    }
                }
                return result;
            })
        res.send(stringifyUnicode(channels));
    }
    catch (e) { handleError(res, e); }
});

// Get messages
app.get(`${BASE}/channels/:channel/messages`, getToken, async (req, res) => {
    try {
        let proxyUrl = `${DEST_BASE}/channels/${req.params.channel}/messages`;
        let queryParam = [];
        let showGuildEmoji = false;
        let showEdited = false;
        if (req.query.limit) queryParam.push(`limit=${req.query.limit}`);
        if (req.query.before) queryParam.push(`before=${req.query.before}`);
        if (req.query.after) queryParam.push(`after=${req.query.after}`);
        if (req.query.emoji || req.query.em) showGuildEmoji = true;
        if (req.query.edit || req.query.ed) showEdited = true;
        if (queryParam.length) proxyUrl += '?' + queryParam.join('&');

        const response = await axios.get(proxyUrl, {headers: res.locals.headers});

        // Populate username cache
        response.data.forEach(msg => {
            userCache.set(msg.author.id, msg.author.username);
        })

        const messages = response.data.map(msg => {
            const result = parseMessageObject(msg, req, showGuildEmoji, showEdited);

            // Content from forwarded message
            if (msg.message_snapshots) {
                result.message_snapshots = [{
                    message: parseMessageObject(msg.message_snapshots[0].message, req, showGuildEmoji, showEdited)
                }]
            }
            return result;
        })
        res.send(stringifyUnicode(messages));

        // Mark latest message as read if client requested to do so and we are not reading an older page of messages
        if (req.query.m == 1 && !req.query.before && !req.query.after && response.data?.[0]?.id) {
            axios.post(
                `${DEST_BASE}/channels/${req.params.channel}/messages/${response.data[0].id}/ack`,
                {token: null},
                {headers: res.locals.headers}
            )
            .catch(e => {
                console.log(e);
            })
        }
    }
    catch (e) { handleError(res, e); }
});

// Send message
const sendMessage = async (req, res) => {
    try {
        const response = await axios.post(
            `${DEST_BASE}/channels/${req.params.channel}/messages`,
            req.body,
            {headers: res.locals.headers}
        );
        res.send(stringifyUnicode({id: response.data.id}));
    }
    catch (e) { handleError(res, e); }
}
app.post(`${BASE}/channels/:channel/messages`, getToken, sendMessage);

// Send message with attachments
app.post(`${BASE}/channels/:channel/upload`, upload.single('files'), getToken, async (req, res) => {
    try {
        const form = new FormData();
        let text = "Message sent!";

        if (req.file != null) {
            const options = {
                header: `\r\n--${form.getBoundary()}\r\nContent-Disposition: form-data; name="files[0]"; filename="${req.file.originalname}"\r\nContent-Type: ${req.file.mimetype}\r\n\r\n`
            };
            form.append('files[0]', req.file.buffer, options);
            text = "File sent!"
        }
        let bypass = false;
        if (req.body) {
            if (req.body.bypass) {
                bypass = true;
            }
            else if (req.body.content?.startsWith("#")) {
                bypass = true;
                req.body.content = req.body.content.slice(1);
            }
            const json = {
                content: req.body.content
            }
            if (req.body.reply !== undefined) {
                json.message_reference = {
                    message_id: req.body.reply
                }
            }
            if (Number(req.body.ping) != 1 && req.body.ping != "on") {
                json.allowed_mentions = {
                    replied_user: false
                }
            }
            const options = {
                header: `\r\n--${form.getBoundary()}\r\nContent-Disposition: form-data; name="payload_json"\r\nContent-Type: application/json\r\n\r\n`
            };
            form.append('payload_json', JSON.stringify(json), options);
        }
        if (!bypass) {
            res.status(400).send({
                message: "Uploading is disabled due to Discord flagging uploads from 3rd-party clients as spam. To upload anyway at your own risk, include a # at the beginning of your message. The # will not be included in the actual message."
            });
            return;
        }

        await axios.post(
            `${DEST_BASE}/channels/${req.params.channel}/messages`,
            form,
            {headers: res.locals.headers}
        )

        res.send(
            `<p>${text}</p><a href="/upload2?channel=${req.params.channel}&token=${res.locals.uploadToken ?? res.locals.headers.Authorization}">Send another</a>`
        );
    }
    catch (e) { handleError(res, e); }
});

// Mark message as read
app.post(`${BASE}/channels/:channel/messages/:message/ack`, getToken, async (req, res) => {
    try {
        await axios.post(
            `${DEST_BASE}/channels/${req.params.channel}/messages/${req.params.message}/ack`,
            req.body,
            {headers: res.locals.headers}
        );
        res.send("ok");
    }
    catch (e) { handleError(res, e); }
});

// Get user info (only ID is used)
app.get(`${BASE}/users/@me`, getToken, async (req, res) => {
    try {
        res.send(JSON.stringify({
            id: res.locals.userID,
            _uploadtoken: generateUploadToken(res.locals.headers.Authorization),
            _liteproxy: true,

            // Number and display name of latest available release version.
            _latest: 29,
            _latestname: "5.2.0",

            // Latest available beta version.
            // If there is no beta version, the version number should be set to 0 (so clients will always download the newer release version).
            // If there is a beta version, the beta version number should be higher than the release one.
            _latestbeta: 0,
            _latestbetaname: "",

            // Version number of emoji JSON data.
            // When the JSON is edited, this number should be increased by one.
            // If this number increases, the clients re-download the JSON.
            _emojiversion: 6,

            // Version numbers of each emoji spritesheet png.
            // If any of these numbers increase, or if new sheets are added, the clients re-download the appropriate sheets.
            // Seems you might have to use 1 as the initial version number if adding a new sheet
            //             0  1  2  3  4  5  6  7  8  9  10 11 12
            _emojisheets: [0, 1, 0, 0, 1, 1, 1, 2, 1, 1, 2, 3, 1]
        }));
    }
    catch (e) { handleError(res, e); }
});

// Get server member
app.get(`${BASE}/guilds/:guild/members/:member`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/guilds/${req.params.guild}/members/${req.params.member}`,
            {headers: res.locals.headers}
        );
        const member = {
            user: response.data.user,
            roles: response.data.roles,
            joined_at: response.data.joined_at
        };
        if (response.data.nick != null) member.avatar = response.data.nick;
        if (response.data.avatar != null) member.avatar = response.data.avatar;
        if (response.data.permissions != null) member.permissions = response.data.permissions;
        res.send(stringifyUnicode(member));
    }
    catch (e) { handleError(res, e); }
});

// Edit message (non-standard because J2ME doesn't support PATCH method)
async function editMessage(req, res) {
    try {
        await axios.patch(
            `${DEST_BASE}/channels/${req.params.channel}/messages/${req.params.message}`,
            req.body,
            {headers: res.locals.headers}
        );
        res.set('Content-Type', 'text/plain');
        res.send("ok");
    }
    catch (e) { handleError(res, e); }
}
app.post(`${BASE}/channels/:channel/messages/:message/edit`, getToken, editMessage);

// Delete message (non-standard because J2ME doesn't support DELETE method)
async function deleteMessage(req, res) {
    try {
        await axios.delete(
            `${DEST_BASE}/channels/${req.params.channel}/messages/${req.params.message}`,
            {headers: res.locals.headers}
        );
        res.set('Content-Type', 'text/plain');
        res.send("ok");
    }
    catch (e) { handleError(res, e); }
}
app.get(`${BASE}/channels/:channel/messages/:message/delete`, getToken, deleteMessage);

// Get server's role list (cached with 1 day TTL)
const roleCache = new LRUCache({max: 1000, ttl: 24*60*60*1000, updateAgeOnGet: false});

app.get(`${BASE}/guilds/:guild/roles`, getToken, async (req, res) => {
    try {
        let roleData;
        if (roleCache.has(req.params.guild)) {
            roleData = roleCache.get(req.params.guild);
        } else {
            const response = await axios.get(
                `${DEST_BASE}/guilds/${req.params.guild}/roles`,
                {headers: res.locals.headers}
            );
            roleData = response.data.sort((a, b) => a.position - b.position);
            roleCache.set(req.params.guild, roleData);
        }
        
        const roles = roleData.map(r => {
            var ret = {
                id: r.id,
                color: r.color
            };
            if (req.query.droidcord) {
                ret.name = r.name;
                ret.position = r.position;
                ret.permissions = r.permissions;
            }
            return ret;
        })
        res.send(stringifyUnicode(roles));
    }
    catch (e) { handleError(res, e); }
});

// Get user's settings (cached with 1 day TTL)
// Output currently not compressed/reduced in any way
const settingsCache = new LRUCache({max: 1000, ttl: 24*60*60*1000, updateAgeOnGet: false});

app.get(`${BASE}/users/@me/settings`, getToken, async (req, res) => {
    try {
        let settingsData;
        if (settingsCache.has(res.locals.userID)) {
            settingsData = settingsCache.get(res.locals.userID);
        } else {
            const response = await axios.get(
                `${DEST_BASE}/users/@me/settings`,
                {headers: res.locals.headers}
            );
            settingsData = response.data;
            settingsCache.set(res.locals.userID, settingsData);
        }
        
        res.send(stringifyUnicode(settingsData));
    }
    catch (e) { handleError(res, e); }
});

// Get threads for channel
app.get(`${BASE}/channels/:channel/threads/search`, getToken, async (req, res) => {
    try {
        let proxyUrl = `${DEST_BASE}/channels/${req.params.channel}/threads/search`
        let queryParam = [];
        if (req.query.archived) queryParam.push(`archived=${req.query.archived}`);
        if (req.query.sort_by) queryParam.push(`sort_by=${req.query.sort_by}`);
        if (req.query.sort_order) queryParam.push(`sort_order=${req.query.sort_order}`);
        if (req.query.limit) queryParam.push(`limit=${req.query.limit}`);
        if (req.query.tag_setting) queryParam.push(`tag_setting=${req.query.tag_setting}`);
        if (req.query.offset) queryParam.push(`offset=${req.query.offset}`);
        if (queryParam.length) proxyUrl += '?' + queryParam.join('&');

        const response = await axios.get(
            proxyUrl,
            {headers: res.locals.headers}
        );
        const output = {
            threads: response.data.threads.map(thr => {
                return {
                    id: thr.id,
                    name: thr.name,
                    last_message_id: thr.last_message_id
                }
            })
        }
        res.send(stringifyUnicode(output));
    }
    catch (e) { handleError(res, e); }
})

// Get user's server list (lite) (cached with 5 minute TTL, may later include optional cache like in non-lite)
const userGuildsLite = new LRUCache({max: 200, ttl: 5*60*1000});

app.get(`${BASE_L}/users/@me/guilds`, getToken, async (req, res) => {
    try {
        if (userGuildsLite.has(res.locals.userID)) {
            res.send(userGuildsLite.get(res.locals.userID));
        } else {
            const response = await axios.get(
                `${DEST_BASE}/users/@me/guilds`,
                {headers: res.locals.headers}
            );
            const guilds = stringifyUnicode(response.data.map(g => [g.id, g.name]));
            userGuildsLite.set(res.locals.userID, guilds);
            res.send(guilds);
        }
    }
    catch (e) { handleError(res, e); }
});

// Get server channels (lite)
app.get(`${BASE_L}/guilds/:guild/channels`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/guilds/${req.params.guild}/channels`,
            {headers: res.locals.headers}
        )

        // Populate channel name cache
        response.data.forEach(ch => {
            channelCache.set(ch.id, ch.name);
        })

        let channels = response.data.filter(ch => ch.type == 0 || ch.type == 5);
        
        if (req.query?.t) {
            // Sort by latest first
            channels.sort((a, b) => {
                const a_id = BigInt(a.last_message_id ?? 0);
                const b_id = BigInt(b.last_message_id ?? 0);
                return (a_id < b_id ? 1 : a_id > b_id ? -1 : 0)
            });
            channels = channels.slice(0, 22);
        } else {
            channels.sort((a, b) => a.position - b.position)
        }

        const output = channels.map(ch => {
            const result = [ch.id, ch.name];
            if (req.query?.t) result.push((BigInt(ch.last_message_id ?? 0) >> 22n).toString(36));
            return result;
        })

        res.send(stringifyUnicode(output));
    }
    catch (e) { handleError(res, e); }
});

// Get DM channels (lite)
app.get(`${BASE_L}/users/@me/channels`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/users/@me/channels`,
            {headers: res.locals.headers}
        );
        
        // Show only normal DMs and group DMs
        const channels = response.data.filter(ch => ch.type == 1 || ch.type == 3);

        // Sort by latest first
        channels.sort((a, b) => {
            const a_id = BigInt(a.last_message_id ?? 0);
            const b_id = BigInt(b.last_message_id ?? 0);
            return (a_id < b_id ? 1 : a_id > b_id ? -1 : 0)
        });

        const output = channels.slice(0, req.query?.t ? 22 : 30)
            // Convert to [id, name] format
            .map(ch => {
                const result = [ch.id];

                if (ch.type == 3) {
                    // Add name for group DM
                    result.push(ch.name);
                } else {
                    // Add first recipient's name for normal DM
                    result.push(ch.recipients[0].global_name ?? ch.recipients[0].username);
                }
                if (req.query?.t) {
                    // timestamp of last message
                    result.push((BigInt(ch.last_message_id ?? 0) >> 22n).toString(36));
                }
                return result;
            })

        res.send(stringifyUnicode(output));
    }
    catch (e) { handleError(res, e); }
});

// Get messages (lite)
app.get(`${BASE_L}/channels/:channel/messages`, getToken, async (req, res) => {
    try {
        let proxyUrl = `${DEST_BASE}/channels/${req.params.channel}/messages`;
        let queryParam = [];
        if (req.query.limit) queryParam.push(`limit=${req.query.limit}`);
        if (req.query.before) queryParam.push(`before=${req.query.before}`);
        if (req.query.after) queryParam.push(`after=${req.query.after}`);
        if (queryParam.length) proxyUrl += '?' + queryParam.join('&');

        const response = await axios.get(proxyUrl, {headers: res.locals.headers});

        // Populate username cache
        response.data.forEach(msg => {
            userCache.set(msg.author.id, msg.author.username);
        })

        const messages = response.data.map(msg => {
            let content;
            if (msg.type >= 1 && msg.type <= 11) {
                // Content not used for status messages
                content = "";
            } else {
                // Parse content for normal messages
                content = (msg.content ?? '')
                    // try to convert <@12345...> format into @username
                    .replace(/<@(\d{15,})>/gm, (mention, id) => {
                        if (userCache.has(id)) return `@${userCache.get(id)}`;
                        else return mention;
                    })
                    // try to convert <#12345...> format into #channelname
                    .replace(/<#(\d{15,})>/gm, (mention, id) => {
                        if (channelCache.has(id)) return `#${channelCache.get(id)}`;
                        else return mention;
                    })
                    // replace <:name:12345...> emoji format with :name:
                    .replace(/<a?(:\w*:)\d{15,}>/gm, "$1")
            
                // Replace Unicode emojis with :name: textual representations
                emoji.colons_mode = true;
                content = emoji.replace_unified(content);

                if (msg.attachments?.length) {
                    msg.attachments.forEach(att => {
                        if (content.length) content += "\n";
                        content += `(file: ${att.filename})`;
                    })
                }
                if (msg.sticker_items?.length) {
                    if (content.length) content += "\n";
                    content += `(sticker: ${msg.sticker_items[0].name})`;
                }
                if (msg.embeds?.length) {
                    msg.embeds.forEach(emb => {
                        if (!emb.title) return;
                        if (content.length) content += "\n";
                        content += `(embed: ${emb.title})`;
                    })
                }
                if (content == '') content = "(unsupported message)";
            }

            let recipient = "";

            if ((msg.type == 1 || msg.type == 2) && msg.mentions.length) {
                // Recipient is used for the target of group DM add/remove notification messages (who was added/removed)
                recipient = msg.mentions[0].global_name ?? msg.mentions[0].username;
            }
            else if (msg.referenced_message?.author) {
                recipient = msg.referenced_message.author.global_name ??
                    msg.referenced_message.author.username;
            }

            return [
                msg.id,
                msg.author.global_name ?? msg.author.username,
                content,
                recipient,
                msg.type,
                // for old clients, show ID hash.
                // for new clients, show 1 or 0 (whether message can be edited/deleted by us)
                (req.query.m == 0 || req.query.m == 1) ?
                    Number(msg.author.id == res.locals.userID) :
                    generateLiteIDHash(msg.author.id)
            ];
        })
        res.send(stringifyUnicode(messages));

        // Mark latest message as read if client requested to do so and we are not reading an older page of messages
        if (req.query.m == 1 && !req.query.before && !req.query.after && response.data?.[0]?.id) {
            axios.post(
                `${DEST_BASE}/channels/${req.params.channel}/messages/${response.data[0].id}/ack`,
                {token: null},
                {headers: res.locals.headers}
            )
            .catch(e => {
                console.log(e);
            })
        }
    }
    catch (e) { handleError(res, e); }
});

// Get user info (lite)
// This returns a 1 to 5-character string generated based on the logged in user's ID, for example 'd5hz9'.
// Message contents also include a string generated in the same way from the message author's ID.
// This can be used to compare user IDs (to a reasonable enough accuracy) to check if the message was sent by the logged in user.
// If a message was sent by the logged in user, the Edit and Delete menu options are shown in the client when that message is highlighted.
// This route is no longer used by the newest 30KB client (now calculated client-side) but kept here for the sake of compatibility
app.get(`${BASE_L}/users/@me`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/users/@me`,
            {headers: res.locals.headers}
        );
        res.set('Content-Type', 'text/plain');
        res.send(generateLiteIDHash(response.data.id));
    }
    catch (e) { handleError(res, e); }
});

// Send/edit/delete message (lite; same behavior as non-lite)
app.post(`${BASE_L}/channels/:channel/messages`, getToken, sendMessage);
app.post(`${BASE_L}/channels/:channel/messages/:message/edit`, getToken, editMessage);
app.get(`${BASE_L}/channels/:channel/messages/:message/delete`, getToken, deleteMessage);

const gatewaySessions = new Map();

// gateway proxy for 30kB client (socket to http)
class GatewayProxy {
    constructor(token) {
        this.token = token;
        this.lastReceived = -1;
        this.messages = [];
        
        this.socket = new WebSocket(
            "wss://gateway.discord.gg/?v=9&encoding=json",
            {origin: "https://discord.com"}
        );

        this.updateExpiration();

        this.socket.on("message", (event) => {
            const data = JSON.parse(event.toString());

            console.log("received", data);

            if (data.s > this.lastReceived) {
                this.lastReceived = data.s;
            }

            if (data.op == 10) {
                const heartbeatInterval = data.d.heartbeat_interval;
                
                this.heartbeat = setInterval(() => {
                    if (!this.isValid()) {
                        clearInterval(this.heartbeat);
                        return;
                    }

                    this.send({
                        op: 1,
                        d: (this.lastReceived == -1) ? null : this.lastReceived
                    });
                }, heartbeatInterval);

                this.send({
                    op: 2,
                    d: {
                        token: this.token,
                        capabilities: 30717,
                        properties: {
                            os: "Android",
                            browser: "Discord Android",
                            device: ""
                        }
                    }
                });
            } else {
                switch (data.t) {
                    // case "GATEWAY_HELLO": {
                    //     this.send({
                    //         op: -1,
                    //         t: "GATEWAY_CONNECT",
                    //         d: {
                    //             supported_events: ["READY", "J2ME_MESSAGE_CREATE"],
                    //             url: "wss://gateway.discord.gg/?v=9&encoding=json"
                    //         }
                    //     });
                    //     break;
                    // }

                    case "READY": {
                        this.readyMessage = {guilds: data.d.guilds};
                        break;
                    }

                    case "MESSAGE_CREATE": {
                        if (data.d.guild_id) {
                            // add guild message event
                            const guild = this.readyMessage?.guilds.find(g => g.id === data.d.guild_id);
                            const guildName = guild?.properties.name;
                            const channelName = guild?.channels.find(ch => ch.id === data.d.channel_id)?.name;

                            this.messages.push([
                                0,
                                data.d.guild_id,
                                data.d.channel_id,
                                guildName,
                                channelName,
                                data.d.author.global_name ?? data.d.author.username
                            ]);
                        } else {
                            // add direct message event
                            this.messages.push([
                                1,
                                data.d.channel_id,
                                data.d.author.global_name ?? data.d.author.username
                            ]);
                        }
                        break;
                    }
                }
            }
        })

        this.socket.on("close", () => {
            console.log("Disconnected");

            this.socket = null;
        })
    }
    
    send(json) {
        console.log("sending", json);
        this.socket.send(JSON.stringify(json) + "\n");
    }

    isValid() {
        return Date.now() < this.expires && this.socket;
    }

    updateExpiration() {
        this.expires = Date.now() + 2*60*1000;
    }

    getMessages() {
        const messages = this.messages;
        this.messages = [];
        return messages;
    }
}

function getExistingSession(token) {
    if (!gatewaySessions.has(token)) return null;

    const session = gatewaySessions.get(token);
    if (session.isValid()) {
        session.updateExpiration();
        return session;
    }
    return null; 
}

function getSession(token) {
    const session = getExistingSession(token);
    if (session) return session;

    const newSession = new GatewayProxy(token);
    gatewaySessions.set(token, newSession);
    return newSession;
}

app.get(`${BASE_L}/gw`, getToken, (req, res) => {
    const session = getSession(res.locals.headers.Authorization);
    const messages = session.getMessages();

    console.log("sending", messages);
    res.send(messages);
});

app.use("/", require('./homepage'));

// for my personal server
if (process.env.DPFILEHOST) {
    app.use(express.static(path.join(__dirname, 'DPFileHost/static')));
    app.use("/", require('./DPFileHost/filehost'));
}

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
