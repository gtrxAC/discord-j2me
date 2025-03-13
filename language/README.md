# Translations

This folder contains language translations for Discord J2ME.

A new translation can be created by copying en.jsonc, naming the new file after the two-letter code of your language (with two extra letters to indicate the region, if necessary), and translating each string in the new file. Ask on our support server if you need help or advice.

## Compact strings

Translations labeled `compact` are variants of the translation strings that are designed for smaller displays. Compact strings can be created for any language by copying its existing translation file and adding `-compact` to the file name, and then editing the strings as appropriate.

The compact strings should ideally fit on the screen of a 128x128 or 128x160 phone without any text cutting off with a "..." at the end (particularly things like the settings options and some softkey commands are prone to cutting off).

Strings can be shortened by, for example, abbreviating long words (like `message` -> `msg.`) or saying the same thing with different words (e.g. `keep channels loaded` -> `remember channels`). Longer strings (like full sentences used in pop-up messages, the login screen, etc) do not have to be shortened.

I use a Nokia Series 40 128x160 phone (with its default settings, like menu icons on, font size medium) as a reference point for checking which strings have to be shortened, but anything with a similar resolution and font size should work too (Sony Ericsson 128x160, Siemens 130x130/132x176, etc).