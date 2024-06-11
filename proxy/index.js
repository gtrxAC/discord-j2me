const express = require('express');
const axios = require('axios');

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

// Get servers
app.get(`${BASE}/users/@me/guilds`, async (req, res) => {
    try {
        delete req.headers.host;
        const response = await axios.get(
            `${DEST_BASE}/users/@me/guilds`,
            {headers: req.headers}
        );
        const guilds = response.data.map(g => {
            return {id: g.id, name: g.name};
        })
        res.send(stringifyUnicode(guilds));
    }
    catch (e) { handleError(res, e); }
});

// Get server channels
app.get(`${BASE}/guilds/:guild/channels`, async (req, res) => {
    try {
        delete req.headers.host;
        const response = await axios.get(
            `${DEST_BASE}/guilds/${req.params.guild}/channels`,
            {headers: req.headers}
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
                    position: ch.position
                }
            });
        res.send(stringifyUnicode(channels));
    }
    catch (e) { handleError(res, e); }
});

// Get DM channels
app.get(`${BASE}/users/@me/channels`, async (req, res) => {
    try {
        delete req.headers.host;
        const response = await axios.get(
            `${DEST_BASE}/users/@me/channels`,
            {headers: req.headers}
        );
        const channels = response.data
            .filter(ch => ch.type == 1 || ch.type == 3)
            .map(ch => {
                const result = {
                    id: ch.id,
                    type: ch.type,
                    last_message_id: ch.last_message_id
                }

                // Add name for group DMs, recipient name for normal DMs
                if (ch.type == 3) {
                    result.name = ch.name;
                } else {
                    result.recipients = [{global_name: ch.recipients[0].global_name}];

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
app.get(`${BASE}/channels/:channel/messages`, async (req, res) => {
    try {
        let proxyUrl = `${DEST_BASE}/channels/${req.params.channel}/messages`;
        let queryParam = [];
        if (req.query.limit) queryParam.push(`limit=${req.query.limit}`);
        if (req.query.before) queryParam.push(`before=${req.query.before}`);
        if (req.query.after) queryParam.push(`after=${req.query.after}`);
        if (queryParam.length) proxyUrl += '?' + queryParam.join('&');

        delete req.headers.host;
        const response = await axios.get(proxyUrl, {headers: req.headers});

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
                author: {global_name: msg.author.global_name}
            }
            if (msg.author.global_name == null) {
                result.author.username = msg.author.username;
            }

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
                    .filter(att => att.width !== undefined)
                    .map(att => {
                        return {
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

            return result;
        })
        res.send(stringifyUnicode(messages));
    }
    catch (e) { handleError(res, e); }
});

// Send message
app.post(`${BASE}/channels/:channel/messages`, async (req, res) => {
    try {
        delete req.headers.host;
        await axios.post(
            `${DEST_BASE}/channels/${req.params.channel}/messages`,
            req.body,
            {headers: req.headers}
        );
        res.send("ok");
    }
    catch (e) { handleError(res, e); }
});

// Mark message as read
app.post(`${BASE}/channels/:channel/messages/:message/ack`, async (req, res) => {
    try {
        delete req.headers.host;
        await axios.post(
            `${DEST_BASE}/channels/${req.params.channel}/messages/${req.params.message}/ack`,
            req.body,
            {headers: req.headers}
        );
        res.send("ok");
    }
    catch (e) { handleError(res, e); }
});

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
