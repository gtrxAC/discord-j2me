const express = require('express');
const axios = require('axios');
const FormData = require('form-data');
const multer = require('multer')

const storage = multer.memoryStorage()
const upload = multer({ storage: storage })

const EmojiConvertor = require('emoji-js');
const emoji = new EmojiConvertor();
emoji.replace_mode = 'unified';

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const PORT = 8080;
const BASE = "/api/v9";
const DEST_BASE = "https://discord.com/api/v9";

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

function getToken(req, res, next) {
    let token;

    if (req.headers?.authorization) {
        token = req.headers.authorization;
    }
    else if (req.body?.token) {
        token = req.body.token;
        delete req.body.token;
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
            .filter(ch => ch.type == 0 || ch.type == 5)
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

// File upload form
app.get(`/upload`, async (req, res) => {
    try {
        if (!req.query?.channel || !req.query?.token) {
            res.send(`<p>Token or destination channel not defined</p>`);
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
    <form method="post" enctype="multipart/form-data" action="${BASE}/channels/${req.query.channel}/upload">
        <input type="hidden" name="token" value="${req.query.token}" />

        <label for="file">File:</label><br />
        <input type="file" name="files" id="files"></input><br />

        <label for="content">Text:</label><br />
        <textarea name="content" id="content"></textarea><br />

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
            const result = {
                id: msg.id,
                author: {
                    id: msg.author.id,
                    avatar: msg.author.avatar,
                    global_name: msg.author.global_name
                }
            }
            if (msg.author.global_name == null) {
                result.author.username = msg.author.username;
            }
            if ([1, 2, 7, 8].includes(msg.type)) result.type = msg.type;

            // Parse content 
            if (msg.content) {
                result.content = msg.content
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
                result.content = emoji.replace_unified(result.content);
            }

            if (msg.referenced_message) {
                result.referenced_message = {
                    author: {
                        global_name: msg.referenced_message.author.global_name
                    }
                }
                if (msg.referenced_message.author.global_name == null) {
                    result.referenced_message.author.username =
                        msg.referenced_message.author.username;
                }
            }

            if (msg.attachments?.length) {
                result.attachments = msg.attachments
                    .map(att => {
                        return {
                            filename: att.filename,
                            size: att.size,
                            width: att.width,
                            height: att.height,
                            proxy_url: att.proxy_url
                        }
                    })
            }
            if (msg.sticker_items?.length) {
                result.sticker_items = [{name: msg.sticker_items[0].name}];
            }
            if (msg.embeds?.length && msg.embeds[0].title) {
                result.embeds = [{title: msg.embeds[0].title}];
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
        })
        res.send(stringifyUnicode(messages));
    }
    catch (e) { handleError(res, e); }
});

// Send message
app.post(`${BASE}/channels/:channel/messages`, getToken, async (req, res) => {
    try {
        await axios.post(
            `${DEST_BASE}/channels/${req.params.channel}/messages`,
            req.body,
            {headers: res.locals.headers}
        );
        res.send("ok");
    }
    catch (e) { handleError(res, e); }
});

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
        if (req.body) form.append('content', req.body.content);

        console.log("after:")
        console.log(res.locals.headers)

        await axios.post(
            `${DEST_BASE}/channels/${req.params.channel}/messages`,
            form,
            {headers: res.locals.headers}
        )

        res.send(
            `<p>${text}</p><a href="/upload?channel=${req.params.channel}&token=${res.locals.headers.authorization}">Send another</a>`
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

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
