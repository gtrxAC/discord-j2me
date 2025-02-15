// ifdef OVER_100KB
package com.gtrxac.discord;

import java.util.Vector;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class FormattedStringParser {
    // temporary string part object to indicate a line break
    public static final Object NEWLINE = JSON.json_null;

    private static final String EMOJI_NAME_CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";

    private static final String URL_END_CHARS = " []()<>\"'\t\r\n";

    private static final String[] ASTERISKS = {null, "*", "**", "***"};
    private static final String[] UNDERSCORES = {null, "_", "__", "___"};
    
    private static final int[] ASTERISK_FORMAT_STYLES = {
        0,
        Font.STYLE_ITALIC,
        Font.STYLE_BOLD,
        (Font.STYLE_BOLD | Font.STYLE_ITALIC),
    };

    private static final int[] UNDERSCORE_FORMAT_STYLES = {
        0,
        Font.STYLE_ITALIC,
        Font.STYLE_UNDERLINED,
        (Font.STYLE_UNDERLINED | Font.STYLE_ITALIC)
    };

    private String src;
    private Font font;
    private Vector result = new Vector();
    private int partBeginPos = 0;
    private int pos = 0;
    private int curAsteriskCount = 0;
    private int curUnderscoreCount = 0;
    private int curColor = 0;
    private boolean isMonospaceMode = false;

    public boolean showLargeEmoji = true;

    FormattedStringParser(String src, Font font) {
        this.src = src;
        this.font = font;
    }

    private void addPreviousPart() {
        String substr = src.substring(partBeginPos, pos);
        if (substr.length() != 0) {
            Object newPart;
            if (isMonospaceMode) {
                newPart = new FormattedStringPartMonospace(substr, font);
            }
            else if (curAsteriskCount > 0 || curUnderscoreCount > 0 || curColor != 0) {
                int format = ASTERISK_FORMAT_STYLES[curAsteriskCount] | UNDERSCORE_FORMAT_STYLES[curUnderscoreCount];
                if (curColor != 0) {
                    newPart = new FormattedStringPartRichTextColor(substr, font, format, curColor);
                } else {
                    newPart = new FormattedStringPartRichText(substr, font, format);
                }
            }
            else {
                newPart = new FormattedStringPartText(substr, font);
            }
            result.addElement(newPart);
            showLargeEmoji = false;
        }
    }

    public Vector run() {
		char[] chars = src.toCharArray();
        int emojiCount = 0;

        try {
            while (true) {
                char curr = chars[pos];

                // Discord does not send tabs or carriage returns but leaving
                // those here if anyone wants to use this code in another app
                if (curr == ' ' /* || curr == '\t' */) {
                    addPreviousPart();
                    partBeginPos = pos;
                    while (chars[++pos] == ' ');  // check for tab here too if needed

                    if (isMonospaceMode) {
                        result.addElement(new FormattedStringPartMonospace(src.substring(partBeginPos, pos), font));
                    } else {
                        result.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                    }
                    partBeginPos = pos;
                    continue;
                }
                // if (curr == '\r') {
                //     pos++;
                //     continue;
                // }
                if (curr == '\n') {
                    addPreviousPart();
                    result.addElement(NEWLINE);
                    pos++;
                    partBeginPos = pos;
                    continue;
                }
                specialChecks: {
                    if (curr == ':' && FormattedString.emojiMode != FormattedString.EMOJI_MODE_OFF) {
                        int colon = src.indexOf(':', pos + 2);
                        if (colon == -1) break specialChecks;

                        String id = src.substring(pos + 1, colon);
                        
                        if (id.startsWith("skin-tone-")) {
                            // emoji is a skin tone modifier - don't show it at all
                            addPreviousPart();
                            pos = colon + 1;
                            partBeginPos = pos;
                            continue;
                        }

                        if (FormattedStringPartEmoji.emojiTable.containsKey(id)) {
                            addPreviousPart();
                            result.addElement(new FormattedStringPartEmoji(id));
                            pos = colon + 1;
                            partBeginPos = pos;
                            emojiCount++;
                            continue;
                        }
                    }
                    else if (curr == '<' && FormattedString.emojiMode == FormattedString.EMOJI_MODE_ALL) {
                        int checkPos = pos + 1;

                        // '<' must be followed by 'a:' or ':'
                        if (chars[checkPos] == 'a') checkPos++;
                        if (chars[checkPos] != ':') break specialChecks;

                        // we only need the emoji ID, but do some checks on the emoji name:
                        // emoji name must consist of only the allowed characters
                        while (EMOJI_NAME_CHARS.indexOf(chars[++checkPos]) != -1);
                        // emoji name must be at least 2 chars
                        if (checkPos < pos + 4) break specialChecks;
                        // emoji name must be followed by a colon
                        if (chars[checkPos] != ':') break specialChecks;

                        int idBeginPos = checkPos + 1;
                        // emoji ID must consist of only digits
                        while (Character.isDigit(chars[++checkPos]));
                        // emoji ID must be at least 17 chars
                        if (checkPos - idBeginPos < 17) break specialChecks;
                        // emoji ID must be followed by a '>'
                        if (chars[checkPos] != '>') break specialChecks;
                        
                        addPreviousPart();
                        String id = src.substring(idBeginPos, checkPos);
                        result.addElement(new FormattedStringPartGuildEmoji(id));
                        pos = checkPos + 1;
                        partBeginPos = pos;
                        emojiCount++;
                        continue;
                    }
                    else if (FormattedString.useMarkdown) {
                        if (!isMonospaceMode) {
                            if (curr == '*') {
                                int asteriskCount = 1;
                                try {
                                    if (chars[pos + 1] == '*') {
                                        asteriskCount++;
                                        if (chars[pos + 2] == '*') asteriskCount++;
                                    }
                                }
                                catch (ArrayIndexOutOfBoundsException e) {}
        
                                // If these are the starting asterisks
                                if (curAsteriskCount == 0) {
                                    // Before setting the formatting mode, make sure there is a matching set of closing asterisks upcoming somewhere in the text
                                    if (src.indexOf(ASTERISKS[asteriskCount], pos + asteriskCount) == -1) break specialChecks;
                                    addPreviousPart();
                                    curAsteriskCount = asteriskCount;
                                    pos += asteriskCount;
                                    partBeginPos = pos;
                                    continue;
                                }
        
                                // If a matching set of asterisks was previously encountered (or a smaller set, for example *text***), add a text part and go back to normal text mode
                                if (curAsteriskCount <= asteriskCount) {
                                    curAsteriskCount = Math.min(asteriskCount, curAsteriskCount);
                                    addPreviousPart();
                                    pos += curAsteriskCount;
                                    partBeginPos = pos;
                                    curAsteriskCount = 0;
                                    continue;
                                }
        
                                // Else these are the ending asterisks but there's less than the beginning ones (for example ***text*):
        
                                // First add the difference between the asterisk counts as normal text
                                // e.g. in the case of ***text*, add a normal text part containing **
                                int difference = curAsteriskCount - asteriskCount;
                                result.addElement(new FormattedStringPartText(ASTERISKS[difference], font));
        
                                // Add the actual text using the formatting style associated with the smallest asterisk count
                                // e.g. ***text* -> add "text" in italic.
                                curAsteriskCount = asteriskCount;
                                addPreviousPart();
                                pos += asteriskCount;
                                partBeginPos = pos;
                                curAsteriskCount = 0;
                                continue;
                            }
                            else if (curr == '_') {
                                // Same as the asterisk one above but different variables
                                int underscoreCount = 1;
                                try {
                                    if (chars[pos + 1] == '_') {
                                        underscoreCount++;
                                        if (chars[pos + 2] == '_') underscoreCount++;
                                    }
                                }
                                catch (ArrayIndexOutOfBoundsException e) {}
        
                                if (curUnderscoreCount == 0) {
                                    if (src.indexOf(UNDERSCORES[underscoreCount], pos + underscoreCount) == -1) break specialChecks;
                                    addPreviousPart();
                                    curUnderscoreCount = underscoreCount;
                                    pos += underscoreCount;
                                    partBeginPos = pos;
                                    continue;
                                }
        
                                if (curUnderscoreCount <= underscoreCount) {
                                    curUnderscoreCount = Math.min(underscoreCount, curUnderscoreCount);
                                    addPreviousPart();
                                    pos += curUnderscoreCount;
                                    partBeginPos = pos;
                                    curUnderscoreCount = 0;
                                    continue;
                                }
        
                                int difference = curUnderscoreCount - underscoreCount;
                                result.addElement(new FormattedStringPartText(UNDERSCORES[difference], font));
        
                                curUnderscoreCount = underscoreCount;
                                addPreviousPart();
                                pos += underscoreCount;
                                partBeginPos = pos;
                                curUnderscoreCount = 0;
                                continue;
                            }
                            // blue color text for URLs
                            else if (curr == 'h') {
                                if (!src.substring(pos).startsWith("http")) break specialChecks;
        
                                int checkPos = pos + 4;
                                if (chars[checkPos] == 's') checkPos++;
                                if (!src.substring(checkPos).startsWith("://")) break specialChecks;
                                checkPos += 2;
        
                                addPreviousPart();
                                curColor = 0x00aafc;
                                try {
                                    while (URL_END_CHARS.indexOf(chars[++checkPos]) == -1) {}
                                }
                                catch (ArrayIndexOutOfBoundsException e) {
                                    partBeginPos = pos;
                                    pos = chars.length;
                                    throw e;
                                }
        
                                // add the blue text part
                                partBeginPos = pos;
                                pos = checkPos;
                                addPreviousPart();
        
                                partBeginPos = pos;
                                curColor = 0;
                                continue;
                            }
                        }
                        if (curr == '`') {
                            // If we're not already in monospace mode, make sure there is a matching ` upcoming somewhere in the text
                            if (!isMonospaceMode && src.indexOf('`', pos + 1) == -1) break specialChecks;

                            addPreviousPart();
                            isMonospaceMode = !isMonospaceMode;
                            pos++;
                            partBeginPos = pos;
                            continue;
                        }
                    }
                }
                // Normal character, move on
                pos++;
            }
        }
        // reached end of text, or unexpected end of text while parsing emoji:
        // ignore and add last part as text if needed
        catch (ArrayIndexOutOfBoundsException e) {}

        // add last part (any remaining text)
        addPreviousPart();

        // check if we need to zoom in the emojis
        if (emojiCount > 10) showLargeEmoji = false;
        
        return result;
    }
}
// endif