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

    private String src;
    private Font font;
    private Vector result = new Vector();
    private int partBeginPos = 0;
    private int pos = 0;

    public boolean isOnlyEmoji = true;

    FormattedStringParser(String src, Font font) {
        this.src = src;
        this.font = font;
    }

    private void addPreviousPart() {
        String substr = src.substring(partBeginPos, pos);
        if (substr.length() != 0) {
            result.addElement(new FormattedStringPartText(substr, font));
            isOnlyEmoji = false;
        }
    }

    public Vector run() {
		char[] chars = src.toCharArray();

        try {
            while (true) {
                char curr = chars[pos];

                // Discord does not send tabs or carriage returns but leaving
                // those here if anyone wants to use this code in another app
                if (curr == ' ' /* || curr == '\t' */) {
                    addPreviousPart();
                    partBeginPos = pos;
                    while (chars[++pos] == ' ');  // check for tab here too if needed
                    result.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
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
                emojiChecks: {
                    if (curr == ':' && FormattedString.emojiMode != FormattedString.EMOJI_MODE_OFF) {
                        int colon = src.indexOf(':', pos + 2);
                        if (colon == -1) break emojiChecks;

                        String id = src.substring(pos + 1, colon);

                        if (FormattedStringPartEmoji.emojiTable.containsKey(id)) {
                            addPreviousPart();
                            result.addElement(new FormattedStringPartEmoji(id));
                            pos = colon + 1;
                            partBeginPos = pos;
                            continue;
                        }
                    }
                    else if (curr == '<' && chars[pos + 1] == ':' && FormattedString.emojiMode == FormattedString.EMOJI_MODE_ALL) {
                        int checkPos = pos + 1;

                        // we only need the emoji ID, but do some checks on the emoji name:
                        // emoji name must consist of only the allowed characters
                        while (EMOJI_NAME_CHARS.indexOf(chars[++checkPos]) != -1);
                        // emoji name must be at least 2 chars
                        if (checkPos < pos + 4) break emojiChecks;
                        // emoji name must be followed by a colon
                        if (chars[checkPos] != ':') break emojiChecks;

                        int idBeginPos = checkPos + 1;
                        // emoji ID must consist of only digits
                        while (Character.isDigit(chars[++checkPos]));
                        // emoji ID must be at least 17 chars
                        if (checkPos - idBeginPos < 17) break emojiChecks;
                        // emoji ID must be followed by a '>'
                        if (chars[checkPos] != '>') break emojiChecks;
                        
                        addPreviousPart();
                        String id = src.substring(idBeginPos, checkPos);
                        result.addElement(new FormattedStringPartGuildEmoji(id));
                        pos = checkPos + 1;
                        partBeginPos = pos;
                        continue;
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
        return result;
    }
}
// endif