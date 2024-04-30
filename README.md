# Discord for J2ME
Discord client for Java ME (MIDP 2.0) devices, inspired by [Discord for Symbian](https://github.com/uwmpr/discord-symbian-fixed). Uses a [proxy server](https://github.com/uwmpr/discord-symbian-fixed/blob/master/dscproxysetup.md) (dsc.uwmpr.online is the default).

![Screenshots](img/screenshots.jpg)

## Status
### Working
* Server list
* Channel list (not correctly sorted yet)
* Message reading (newest message on top)
* Message sending

### Not implemented
* WebSocket (live message updates)
* Reading older messages
* Replying to messages
* Direct messages
* Ping/unread indicators
* Pretty much everything else

## Thanks
* [@uwmpr](https://github.com/uwmpr) for hosting the default proxy server
* [@shinovon](https://github.com/shinovon) for their Java ME [JSON library](https://github.com/shinovon/NNJSON)