const express = require('express');
const axios = require('axios');
const FormData = require('form-data');
const multer = require('multer')
const path = require('path');
const sanitizeHtml = require('sanitize-html');
const crypto = require('crypto').webcrypto;

const storage = multer.memoryStorage()
const upload = multer({ storage: storage })

const EmojiConvertor = require('emoji-js');
const emoji = new EmojiConvertor();
emoji.replace_mode = 'unified';

const app = express();
app.use(express.static(path.join(__dirname, 'static')));
app.use(defaultContentType);
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const PORT = 8080;
const BASE = "/api/v9";
const BASE_L = "/api/l";
const DEST_BASE = "https://discord.com/api/v9";

const uploadTokens = new Map();

// ID -> username mapping cache (used for parsing mentions)
const userCache = new Map();
const channelCache = new Map();
const CACHE_SIZE = 10000;

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

    if (req.route.path == `${BASE}/channels/:channel/upload`) {
        res.locals.uploadToken = token;
        token = getTokenFromUploadToken(token);
    }

    res.locals.headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0",
        "Accept": "*/*",
        "Accept-Language": "en-US,en;q=0.5",
        "Authorization": token,
        "X-Discord-Locale": "en-GB",
        "X-Debug-Options": "bugReporterEnabled",
        "Sec-Fetch-Dest": "empty",
        "Sec-Fetch-Mode": "cors",
        "Sec-Fetch-Site": "same-origin"
    };
    next();
}

function parseMessageContent(content) {
    let result = content
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
    result = emoji.replace_unified(result);

    return result;
}

function parseMessageObject(msg, req) {
    const result = {
        id: msg.id
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
        result.content = parseMessageContent(msg.content);
        if (result.content != msg.content) result._rc = msg.content;
    }

    if (msg.referenced_message) {
        let content = parseMessageContent(msg.referenced_message.content);

        // Replace newlines with spaces (reply is shown as one line)
        content = content.replace(/\r\n|\r|\n/gm, "  ");

        if (content && content.length > 50) {
            content = content.slice(0, 47).trim() + '...';
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
                title: emb.title,
                description: emb.description
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

// Get servers
app.get(`${BASE}/users/@me/guilds`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/users/@me/guilds`,
            {headers: res.locals.headers}
        );
        const guilds = response.data.map(g => {
            const result = {id: g.id, name: g.name};
            if (g.icon != null) result.icon = g.icon;
            return result;
        })
        res.send(stringifyUnicode(guilds));
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

            // If max size exceeded, remove the oldest item
            if (channelCache.size > CACHE_SIZE) {
                channelCache.delete(channelCache.keys().next().value);
            }
        })

        const channels = response.data
            .filter(ch => [0, 5, 15, 16].includes(ch.type))
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
        if (req.query.limit) queryParam.push(`limit=${req.query.limit}`);
        if (req.query.before) queryParam.push(`before=${req.query.before}`);
        if (req.query.after) queryParam.push(`after=${req.query.after}`);
        if (queryParam.length) proxyUrl += '?' + queryParam.join('&');

        const response = await axios.get(proxyUrl, {headers: res.locals.headers});

        // Populate username cache
        response.data.forEach(msg => {
            userCache.set(msg.author.id, msg.author.username);

            // If max size exceeded, remove the oldest item
            if (userCache.size > CACHE_SIZE) {
                userCache.delete(userCache.keys().next().value);
            }
        })

        const messages = response.data.map(msg => {
            const result = parseMessageObject(msg, req);

            // Content from forwarded message
            if (msg.message_snapshots) {
                result.message_snapshots = [{
                    message: parseMessageObject(msg.message_snapshots[0].message, req)
                }]
            }
            return result;
        })
        res.send(stringifyUnicode(messages));
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
            `<p>${text}</p><a href="/upload?channel=${req.params.channel}&token=${res.locals.uploadToken ?? res.locals.headers.Authorization}">Send another</a>`
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
        const response = await axios.get(
            `${DEST_BASE}/users/@me`,
            {headers: res.locals.headers}
        );
        res.send(JSON.stringify({
            id: response.data.id,
            _uploadtoken: generateUploadToken(res.locals.headers.Authorization),
            _liteproxy: true,
            _latest: 6,
            _latestname: "4.0.0",
            _latestbeta: 10,
            _latestbetaname: "4.1.0 beta4",
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

// Get role list
app.get(`${BASE}/guilds/:guild/roles`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/guilds/${req.params.guild}/roles`,
            {headers: res.locals.headers}
        );
        const roles = response.data
            .sort((a, b) => a.position - b.position)
            .map(r => {
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
        
        res.send(stringifyUnicode(roles))
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

// Get servers (lite)
app.get(`${BASE_L}/users/@me/guilds`, getToken, async (req, res) => {
    try {
        const response = await axios.get(
            `${DEST_BASE}/users/@me/guilds`,
            {headers: res.locals.headers}
        );
        const guilds = response.data.map(g => [g.id, g.name])
        res.send(stringifyUnicode(guilds));
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

            // If max size exceeded, remove the oldest item
            if (channelCache.size > CACHE_SIZE) {
                channelCache.delete(channelCache.keys().next().value);
            }
        })

        const channels = response.data
            .filter(ch => ch.type == 0 || ch.type == 5)
            .sort((a, b) => a.position - b.position)
            .map(ch => [ch.id, ch.name])

        res.send(stringifyUnicode(channels));
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

        const output = channels.slice(0, 30)
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

            // If max size exceeded, remove the oldest item
            if (userCache.size > CACHE_SIZE) {
                userCache.delete(userCache.keys().next().value);
            }
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
                generateLiteIDHash(msg.author.id)
            ];
        })
        res.send(stringifyUnicode(messages));
    }
    catch (e) { handleError(res, e); }
});

// Get user info (lite)
// This returns a 1 to 5-character string generated based on the logged in user's ID, for example 'd5hz9'.
// Message contents also include a string generated in the same way from the message author's ID.
// This can be used to compare user IDs (to a reasonable enough accuracy) to check if the message was sent by the logged in user.
// If a message was sent by the logged in user, the Edit and Delete menu options are shown in the client when that message is highlighted.
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

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
