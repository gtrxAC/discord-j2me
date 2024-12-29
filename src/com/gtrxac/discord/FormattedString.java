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

        FormattedStringParser parser = new FormattedStringParser(src, font);
        Vector tempParts = parser.run();
        isOnlyEmoji = parser.isOnlyEmoji;

        if (!singleLine) {
            breakParts(tempParts, font, width);
            if (isOnlyEmoji) upscaleEmoji(tempParts, font);
        }
        height = positionParts(tempParts, font, width, xOffset, singleLine);
        mergeParts(tempParts);
        
        parts = new FormattedStringPart[tempParts.size()];
        tempParts.copyInto(parts);
    }

    private static void breakParts(Vector parts, Font font, int width) {
        for (int i = 0; i < parts.size(); i++) {
            Object part = parts.elementAt(i);
            if (!(part instanceof FormattedStringPartText)) continue;
            FormattedStringPartText textPart = (FormattedStringPartText) part;

            if (textPart.getWidth() <= width) continue;

            String curr = textPart.content;
            while (font.stringWidth(curr) > width) {
                curr = curr.substring(0, curr.length() - 1);
            }

            parts.insertElementAt(new FormattedStringPartText(curr, font), i);
            textPart.content = textPart.content.substring(curr.length());
        }
    }

    private void upscaleEmoji(Vector parts, Font font) {
        // Only upscale if message has no more than 10 emoji total
        if (parts.size() > 10) {
            isOnlyEmoji = false;
            return;
        }
        int newSize = FormattedStringPartEmoji.largeEmojiSize;

        for (int i = 0; i < parts.size(); i++) {
            Object curr = parts.elementAt(i);
            if (curr instanceof FormattedStringPartEmoji) {
                FormattedStringPartEmoji part = (FormattedStringPartEmoji) curr;
                part.image = Util.resizeImage(part.image, newSize, newSize);
            }
            else if (curr instanceof FormattedStringPartGuildEmoji) {
                ((FormattedStringPartGuildEmoji) curr).size = newSize;
            }
        }
    }

    private int positionParts(Vector parts, Font font, int width, int xOffset, boolean singleLine) {
        int x = xOffset;
        int y = 0;
        int lineHeight = font.getHeight();
        if (isOnlyEmoji) lineHeight = FormattedStringPartEmoji.largeEmojiSize;
        int lineCount = 1;

        for (int i = 0; i < parts.size(); i++) {
            Object curr = parts.elementAt(i);
            if (curr == FormattedStringParser.NEWLINE) {
                x = xOffset;
                y += lineHeight;
                lineCount++;
                parts.removeElementAt(i);
                i--;
                continue;
            }
            FormattedStringPart part = (FormattedStringPart) curr;
            boolean partIsText = (part instanceof FormattedStringPartText);
            
            // Note: formatted strings with "only emoji" (large emoji rendering) can still have whitespace text parts, so also check for those
            int partWidth = (isOnlyEmoji && !partIsText) ? lineHeight : part.getWidth();

            // Go to a new display line if not enough space left on the current line
            if (!singleLine && x + partWidth >= xOffset + width) {
                x = xOffset;
                y += lineHeight;
                lineCount++;
            }
            // If a whitespace part ends up at the beginning of a display line, and it is not at the beginning of a line in the source text, discard it
            if (
                x == xOffset && i != 0 && partIsText &&
                ((FormattedStringPartText) part).isWhitespace()
            ) {
                parts.removeElementAt(i);
                i--;
                continue;
            }
            part.x = x;
            part.y = y;
            // Vertically center align emojis to the text
            if (!partIsText) {
                part.y += (isOnlyEmoji ? FormattedStringPartEmoji.largeImageYOffset : FormattedStringPartEmoji.imageYOffset);
            }
            x += partWidth;
        }
        return lineCount*lineHeight;
    }

    private void mergeParts(Vector parts) {
        for (int i = 0; i < parts.size() - 1;) {
            Object curr = parts.elementAt(i);
            if (!(curr instanceof FormattedStringPartText)) {
                i++;
                continue;
            }
            FormattedStringPartText thisPart = (FormattedStringPartText) curr;

            if (thisPart.isWhitespace()) {
                parts.removeElementAt(i);
                continue;
            }

            // Next part is not a text part - cannot be merged, skip
            Object next = parts.elementAt(i + 1);
            if (!(next instanceof FormattedStringPartText)) {
                i++;
                continue;
            }
            FormattedStringPartText nextPart = (FormattedStringPartText) next;

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