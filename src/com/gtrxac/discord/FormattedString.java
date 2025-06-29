// ifdef OVER_100KB
package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class FormattedString implements Strings {
    private FormattedStringPart[] parts;
    int height;

    // ifdef EMOJI_SUPPORT
    private boolean showLargeEmoji;

    public static final int EMOJI_MODE_OFF = 0;
    public static final int EMOJI_MODE_DEFAULT_ONLY = 1;
    public static final int EMOJI_MODE_ALL = 2;
    public static int emojiMode;
    // endif

    public static boolean useMarkdown;

    FormattedString(String src, Font font, int width, int xOffset, boolean singleLine, boolean isEdited, boolean isForwarded) {
        boolean isEmpty = (src == null || src.trim().length() == 0);

        if ((isEmpty && !isForwarded && !isEdited) || width < font.charWidth('W') + 2) {
            parts = new FormattedStringPart[0];
            return;
        }

        Vector tempParts;
        if (isEmpty) {
            tempParts = new Vector();
        } else {
            // Emojis are oversized in refmessages if using direct refmessage drawing (see channelviewitem), easiest is just to not show them there at all
            // ifdef EMOJI_SUPPORT
            boolean showEmoji = !singleLine || !ChannelViewItem.shouldUseDirectRefMessage();
            FormattedStringParser parser = new FormattedStringParser(src, font, showEmoji, singleLine);
            tempParts = parser.run();
            showLargeEmoji = parser.showLargeEmoji;
            // else
            FormattedStringParser parser = new FormattedStringParser(src, font, false, singleLine);
            tempParts = parser.run();
            // endif
        }

        Font editedFont = null;

        if (useMarkdown) {
            if (isForwarded) {
                tempParts.insertElementAt(createEditedOrForwardedPart(FORWARDED_MESSAGE, font, Theme.forwardedTextColor), 0);
                if (!isEmpty || isEdited) {
                    tempParts.insertElementAt(FormattedStringParser.NEWLINE, 1);
                }
            }
    
            if (isEdited) {
                if (!isEmpty) {
                    tempParts.addElement(new FormattedStringPartText(" ", font));
                }
                editedFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                tempParts.addElement(createEditedOrForwardedPart(EDITED_MESSAGE, editedFont, Theme.editedTextColor));
            }
        }

        if (!singleLine) {
            breakParts(tempParts, font, width);
            // ifdef EMOJI_SUPPORT
            if (showLargeEmoji) upscaleEmoji(tempParts, font);
            // endif
        }
        height = positionParts(tempParts, font, width, xOffset, singleLine);
        mergeParts(tempParts);

        // Fix vertical alignment of "(edited)" indicator item
        if (isEdited && useMarkdown && Settings.theme != Theme.SYSTEM) {
            ((FormattedStringPart) tempParts.lastElement()).y += font.getBaselinePosition() - editedFont.getBaselinePosition();
        }
        
        parts = new FormattedStringPart[tempParts.size()];
        tempParts.copyInto(parts);
    }

    private static FormattedStringPartText createEditedOrForwardedPart(int stringKey, Font font, int color) {
        return (Settings.theme != Theme.SYSTEM) ?
            new FormattedStringPartRichTextColor(Locale.get(stringKey), font, 0, color) :
            new FormattedStringPartText(Locale.get(stringKey), font);
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

            parts.insertElementAt(textPart.copy(curr), i);
            textPart.content = textPart.content.substring(curr.length());
        }
    }

    // ifdef EMOJI_SUPPORT
    private void upscaleEmoji(Vector parts, Font font) {
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
    // endif

    private int positionParts(Vector parts, Font font, int width, int xOffset, boolean singleLine) {
        int x = xOffset;
        int y = 0;
        int lineHeight = font.getHeight();
        // ifdef EMOJI_SUPPORT
        if (showLargeEmoji) lineHeight = FormattedStringPartEmoji.largeEmojiSize;
        // endif
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
            // ifdef EMOJI_SUPPORT
            int partWidth = (showLargeEmoji && !partIsText) ? lineHeight : part.getWidth();
            // else
            int partWidth = part.getWidth();
            // endif

            // Go to a new display line if not enough space left on the current line
            if (!singleLine && x + partWidth > xOffset + width) {
                // If a whitespace part ends up at the beginning of a display line, and it is not at the beginning of a line in the source text, discard it
                if (partIsText && ((FormattedStringPartText) part).isWhitespace()) {
                    parts.removeElementAt(i);
                    i--;
                    continue;
                }
                x = xOffset;
                y += lineHeight;
                lineCount++;
            }
            part.x = x;
            part.y = y;
            // Vertically center align emojis to the text
            // ifdef EMOJI_SUPPORT
            if (!partIsText && !showLargeEmoji) {
                part.y += FormattedStringPartEmoji.imageYOffset;
            }
            // endif
            x += partWidth;
        }
        return lineCount*lineHeight;
    }

    private static boolean canMerge(FormattedStringPartText a, FormattedStringPartText b) {
        // rich text cannot merge with non-rich text
        if ((a instanceof FormattedStringPartRichText) != (b instanceof FormattedStringPartRichText)) {
            return false;
        }
        try {
            // both are rich text: check if they have same formatting
            FormattedStringPartRichText ar = (FormattedStringPartRichText) a;
            FormattedStringPartRichText br = (FormattedStringPartRichText) b;
            if (ar.font.getStyle() != br.font.getStyle()) return false;
            if (ar.font.getFace() != br.font.getFace()) return false;

            // check if they have the same color
            if ((ar instanceof FormattedStringPartRichTextColor) != (br instanceof FormattedStringPartRichTextColor)) {
                return false;
            }
            FormattedStringPartRichTextColor arc = (FormattedStringPartRichTextColor) ar;
            FormattedStringPartRichTextColor brc = (FormattedStringPartRichTextColor) br;
            return arc.color == brc.color;
        }
        catch (Exception e) {
            // both are non-rich text or rich non-color text
            return true;
        }
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

            // Not on the same line or don't have the same rich formatting - cannot be merged, skip
            if (thisPart.y != nextPart.y || !canMerge(thisPart, nextPart)) {
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
// endif
