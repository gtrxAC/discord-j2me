package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class FormattedString {
    private FormattedStringPart[] parts;
    int height;
    private boolean isOnlyEmoji;

    FormattedString(String src, Font font, int width, int xOffset) {
        this(src, font, width, xOffset, false);
    }

    FormattedString(String src, Font font, int width, int xOffset, boolean singleLine) {
        if (
            src == null || src.length() == 0 || src.equals(" ") ||
            width < font.charWidth('W') + 2
        ) {
            parts = new FormattedStringPart[0];
            return;
        }

        isOnlyEmoji = true;  // set to false by parseParts if message has any text parts in it
        Vector tempParts = parseParts(src, font);
        if (!singleLine) {
            breakParts(tempParts, font, width);
            if (isOnlyEmoji) upscaleEmoji(tempParts);
        }
        int lineCount = positionParts(tempParts, font, width, xOffset, singleLine);
        tempParts = flattenParts(tempParts);
        mergeParts(tempParts);
        
        parts = new FormattedStringPart[tempParts.size()];
        tempParts.copyInto(parts);

        height = lineCount*font.getHeight();
        if (isOnlyEmoji) height *= 2;
    }

    /**
     * Parse string into string parts. Each part consists of either a word, a block of one or more whitespace characters, or an emoji.
     * @param src Text to be parsed.
     * @return Two-dimensional vector of FormattedStringParts. The string parts do not have x and y position values filled out.
     */
    private Vector parseParts(String src, Font font) {
        int pos = 0;
        int partBeginPos = 0;
		char[] chars = src.toCharArray();

        final int MODE_UNDETERMINED = -1;
        final int MODE_WORD = 0;
        final int MODE_WHITESPACE = 1;
        final int MODE_EMOJI = 2;
        final int MODE_GUILD_EMOJI = 3;
        int mode = MODE_UNDETERMINED;

        Vector result = new Vector();
        result.addElement(new Vector());  // first line

        while (pos < chars.length) {
            switch (chars[pos]) {
                case '\r': break; // ignore
                case '\n': {
                    // Newline: end current part by adding new line (new inner vector)
                    Vector line = (Vector) result.lastElement();
                    line.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                    partBeginPos = pos + 1;  // Don't include the newline character in the actual part contents
                    result.addElement(new Vector());
                    mode = MODE_UNDETERMINED;
                    break;
                }
                case ' ': {
                    if (mode != MODE_WHITESPACE) {
                        if (mode == MODE_WORD || mode == MODE_EMOJI) {
                            Vector line = (Vector) result.lastElement();
                            line.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                            partBeginPos = pos;
                        }
                        mode = MODE_WHITESPACE;
                    }
                    break;
                }
                case '<': {
                    // Possibly the beginning of a guild emoji.
                    // Check if next character is ':' and if there are enough chars left for there to possibly be a guild emoji
                    if (pos + 5 < chars.length && chars[pos + 1] == ':') {
                        // Add previous part as text
                        if (mode != MODE_UNDETERMINED) {
                            Vector line = (Vector) result.lastElement();
                            line.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                        }
                        mode = MODE_GUILD_EMOJI;
                        partBeginPos = pos;
                    } else {
                        // Not a guild emoji - treat as normal text
                        if (mode == MODE_WHITESPACE || mode == MODE_UNDETERMINED) {
                            if (mode == MODE_WHITESPACE) {
                                Vector line = (Vector) result.lastElement();
                                line.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                                partBeginPos = pos;
                            }
                            mode = MODE_WORD;
                            isOnlyEmoji = false;
                        }
                    }
                    break;
                }
                case '>': {
                    if (mode == MODE_GUILD_EMOJI) {
                        // End of what is likely a guild emoji.
                        // Check that the emoji is formatted correctly (two colons and numeric ID), and add it. If it's invalid, it's normal text.
                        String content = src.substring(partBeginPos, pos);
                        int colonIndex = content.indexOf(':');
                        int secondColonIndex = content.indexOf(':', 2);  // begin index of ID, i.e. ignore the beginning '<:' and get index of next ':'

                        if (colonIndex != -1 && secondColonIndex != -1) {
                            String emojiID = content.substring(secondColonIndex + 1);
                            boolean emojiIDIsNumeric = true;
                            try {
                                Long.parseLong(emojiID);
                            }
                            catch (NumberFormatException e) {
                                emojiIDIsNumeric = false;
                            }

                            if (emojiIDIsNumeric && emojiID.length() > 10) {
                                Vector line = (Vector) result.lastElement();
                                line.addElement(new FormattedStringPartGuildEmoji(emojiID, font));
                                mode = MODE_UNDETERMINED;
                                partBeginPos = pos + 1;
                                break;
                            }
                        }
                    }
                    // Not a guild emoji - treat as normal text
                    if (mode == MODE_WHITESPACE || mode == MODE_UNDETERMINED) {
                        if (mode == MODE_WHITESPACE) {
                            Vector line = (Vector) result.lastElement();
                            line.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                            partBeginPos = pos;
                        }
                        mode = MODE_WORD;
                        isOnlyEmoji = false;
                    }
                    break;
                }
                case ':': {
                    if (mode == MODE_EMOJI) {
                        // End of an emoji, check if this emoji is recognized.
                        Vector line = (Vector) result.lastElement();
                        FormattedStringPart newPart = FormattedStringPartEmoji.create(src.substring(partBeginPos + 1, pos), font);
                        if (newPart instanceof FormattedStringPartText) {
                            // not recognized, will show as text - don't do emoji upscaling for this message
                            isOnlyEmoji = false;
                        }
                        line.addElement(newPart);
                        mode = MODE_UNDETERMINED;
                        partBeginPos = pos + 1;
                        break;
                    }
                    // Possibly the beginning of an emoji.
                    // Check if the next character is a word character. If so, this is the beginning of an emoji. If not, treat this ':' like any other non-whitespace character.
                    else if (mode != MODE_GUILD_EMOJI && pos + 1 < chars.length && chars[pos + 1] != ' ') {
                        Vector line = (Vector) result.lastElement();
                        line.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                        mode = MODE_EMOJI;
                        partBeginPos = pos;  // Don't include this colon character in the actual part contents
                        break;
                    }
                    // else include the colon character that was previously skipped, and fall through
                }
                default: {
                    if (mode == MODE_WHITESPACE || mode == MODE_UNDETERMINED) {
                        if (mode == MODE_WHITESPACE) {
                            Vector line = (Vector) result.lastElement();
                            line.addElement(new FormattedStringPartText(src.substring(partBeginPos, pos), font));
                            partBeginPos = pos;
                        }
                        mode = MODE_WORD;
                        isOnlyEmoji = false;
                    }
                    break;
                }
            }
            pos++;
        }
        // Add last remaining part
        Vector line = (Vector) result.lastElement();
        String lastStr = src.substring(partBeginPos);
        if (lastStr.length() > 0) {
            line.addElement(new FormattedStringPartText(lastStr, font));
            isOnlyEmoji = false;
        }

        return result;
    }

    private static void breakParts(Vector lines, Font font, int width) {
        for (int l = 0; l < lines.size(); l++) {
            Vector line = (Vector) lines.elementAt(l);
            for (int i = 0; i < line.size(); i++) {
                if (!(line.elementAt(i) instanceof FormattedStringPartText)) continue;
                FormattedStringPartText part = (FormattedStringPartText) line.elementAt(i);

                if (part.getWidth() <= width) continue;

                String curr = part.content;
                while (font.stringWidth(curr) > width) {
                    curr = curr.substring(0, curr.length() - 1);
                }

                line.insertElementAt(new FormattedStringPartText(curr, font), i);
                part.content = part.content.substring(curr.length());
            }
        }
    }

    private void upscaleEmoji(Vector lines) {
        int newSize = FormattedStringPartEmoji.emojiSize*2;

        // Only upscale if message has less than 10 emoji total
        int emojiCount = 0;
        for (int l = 0; l < lines.size(); l++) {
            Vector line = (Vector) lines.elementAt(l);
            emojiCount += line.size();
            if (emojiCount > 10) {
                isOnlyEmoji = false;
                return;
            }
        }

        for (int l = 0; l < lines.size(); l++) {
            Vector line = (Vector) lines.elementAt(l);
            for (int i = 0; i < line.size(); i++) {
                if (line.elementAt(i) instanceof FormattedStringPartEmoji) {
                    FormattedStringPartEmoji part = (FormattedStringPartEmoji) line.elementAt(i);
                    part.image = Util.resizeImage(part.image, newSize, newSize);
                }
                else if (line.elementAt(i) instanceof FormattedStringPartGuildEmoji) {
                    FormattedStringPartGuildEmoji part = (FormattedStringPartGuildEmoji) line.elementAt(i);
                    part.size *= 2;
                }
            }
        }
    }

    private int positionParts(Vector lines, Font font, int width, int xOffset, boolean singleLine) {
        int x = xOffset;
        int y = 0;
        int lineHeight = font.getHeight();
        if (isOnlyEmoji) lineHeight *= 2;
        int lineCount = 0;

        for (int l = 0; l < lines.size(); l++) {
            Vector line = (Vector) lines.elementAt(l);
            for (int i = 0; i < line.size(); i++) {
                FormattedStringPart part = (FormattedStringPart) line.elementAt(i);
                int partWidth = part.getWidth();
                if (isOnlyEmoji) partWidth *= 2;

                // Go to a new display line if not enough space left on the current line
                if (!singleLine && x + partWidth >= xOffset + width) {
                    x = xOffset;
                    y += lineHeight;
                    lineCount++;
                }
                // If a whitespace part ends up at the beginning of a display line, and it is not at the beginning of a line in the source text, discard it
                if (
                    x == xOffset && i != 0 &&
                    part instanceof FormattedStringPartText &&
                    ((FormattedStringPartText) part).isWhitespace()
                ) {
                    line.removeElementAt(i);
                    i--;
                    continue;
                }
                part.x = x;
                part.y = y;
                x += partWidth;
            }
            x = xOffset;
            y += lineHeight;
            lineCount++;
        }
        return lineCount;
    }

    private static Vector flattenParts(Vector lines) {
        Vector result = new Vector();
        for (int l = 0; l < lines.size(); l++) {
            Vector line = (Vector) lines.elementAt(l);
            for (int i = 0; i < line.size(); i++) {
                result.addElement(line.elementAt(i));
            }
        }
        return result;
    }

    private void mergeParts(Vector parts) {
        for (int i = 0; i < parts.size() - 1;) {
            if (!(parts.elementAt(i) instanceof FormattedStringPartText)) {
                i++;
                continue;
            }
            FormattedStringPartText thisPart = (FormattedStringPartText) parts.elementAt(i);

            if (thisPart.isWhitespace()) {
                parts.removeElementAt(i);
                continue;
            }

            // Next part is not a text part - cannot be merged, skip
            if (!(parts.elementAt(i + 1) instanceof FormattedStringPartText)) {
                i++;
                continue;
            }
            FormattedStringPartText nextPart = (FormattedStringPartText) parts.elementAt(i + 1);

            // Not on the same line - cannot be merged, skip
            if (thisPart.y != nextPart.y) {
                i++;
                continue;
            }
            // Merge parts and remove the next part which was merged into the current one
            thisPart.content += nextPart.content;
            parts.removeElementAt(i + 1);
        }
    }

    public void draw(Graphics g, int yOffset) {
        for (int i = 0; i < parts.length; i++) {
            parts[i].draw(g, yOffset);
        }
    }
}