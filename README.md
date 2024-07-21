# Discord for J2ME
Discord client for Java ME (MIDP 1.0 and 2.0) devices, inspired by [Discord for Symbian](https://github.com/uwmpr/discord-symbian-fixed). Uses proxy servers for the [HTTP](/proxy/) and [gateway](https://github.com/gtrxAC/discord-j2me-server) connection.

Also see [Droidcord](https://github.com/leap0x7b/Droidcord), a Discord client for old Android devices.

![Screenshots](img/screenshots.png)

* [Download](https://github.com/gtrxAC/discord-j2me/releases/latest)
* [FAQ](https://github.com/gtrxAC/discord-j2me/wiki/FAQ)
* [Discord server](https://discord.gg/2GKuJjQagp) (#discord-j2me)
* [Telegram group](https://t.me/dscforsymbian)

## Status
### Working
* Server and channel lists
* Message reading, sending, editing, <abbr title="Only your own messages">deleting</abbr>
* Replying to messages
* Reading older messages
* Direct messages and group DMs
* Attachment viewing
* Attachment sending (<abbr title="Requires FileConnection API or HTML browser with file uploading support">device dependent</abbr>)
* Gateway/live message updates (<abbr title="Not supported on MIDP 1.0 and some low-end Samsung devices">device dependent</abbr>)
* <abbr title="Not in sync with official clients">Unread message indicators</abbr>

### Not implemented
* Jumping to messages (e.g. replies)
* Initiating DM conversations
* Ping indicators
* Reactions and emojis

## How to build
1. Install Sun Java Wireless Toolkit 2.5.2 on your computer.
    * If you haven't already, install an older JDK version (e.g. 1.6.0_45).
2. Open Wireless Toolkit and create a new project named `Discord`.
3. Copy the contents of this repository into the project's folder.
    * On Windows, it should be in `C:\Users\yourname\j2mewtk\2.5.2\apps\Discord`
4. Build the project.
    * To create a JAR, go to `Options` -> `Package` -> `Create package`.
    * Optional: to create an obfuscated JAR, go to `Options` -> `Package` -> `Create obfuscated package`.
        * For this, you'll need Proguard installed as part of your WTK.
        * Download one of the older versions [here](https://sourceforge.net/projects/proguard/files/proguard/) (e.g. 3.4).
        * Extract the ZIP and copy the `proguard.jar` file from the `lib` folder into the `bin` folder of your WTK installation.

## Thanks
* [@uwmpr](https://github.com/uwmpr) for formerly hosting the default proxy server
* [@WunderWungiel](https://github.com/WunderWungiel) for formerly hosting the CDN proxy
* [@shinovon](https://github.com/shinovon) for their Java ME [JSON library](https://github.com/shinovon/NNJSON)
* [@saetta06](https://github.com/saetta06) for creating the menu graphics and loading animation