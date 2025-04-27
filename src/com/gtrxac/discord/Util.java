package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import java.lang.Math;

public class Util {
    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static int charsPerItem = 0;

    static {
        ChannelView canvas = new ChannelView(true);
        screenWidth = canvas.getWidth();
        screenHeight = canvas.getHeight();

        if (System.getProperty("microedition.platform").indexOf("Nokia") != -1) {
            switch (screenWidth) {
                // fix vertically offset canvas font on series 60 (176x208 and 352x416)
                case 352: {
                    ChannelViewItem.fontYOffset = 2;
                    break;
                }
                case 176: {
                    ChannelViewItem.fontYOffset = 1;
                    break;
                }
                case 128: {
                    // 128px wide -> could be midp1 or midp2
                    // if it's midp1, it's s40v1, so we must limit the chars per item (read trimItem comment)
                    try {
                        Class.forName("javax.microedition.lcdui.game.GameCanvas");
                    }
                    catch (Throwable e) {
                        charsPerItem = 14;
                    }
                    break;
                }
                case 96: {
                    charsPerItem = 12;
                    break;
                }
            }
        }
    }

    // Trim list item's text to the maximum length that can fit on one line on the screen
    // Nokia S40v1 uses line wrapping in List screens and we don't want that
    public static String trimItem(String str) {
        if (charsPerItem == 0 || str.length() <= charsPerItem) return str;
        return str.substring(0, charsPerItem - 1) + "..";
    }
    
    private static Font cachedFont;
    private static int cachedMinWidth;
    private static int cachedSpaceWidth;

    /**
     * Get array of text lines to draw (word wrap)
     * 
     * This algorithm assumes that the input string does not:
     *  - begin or end with a space or line break (you can trim the string if needed)
     *  - contain any tabs (you can replace them with four spaces, two spaces, or similar)
     *  - contain any carriage returns (if your string may contain those, uncomment the "replace" line)
     */
    public static String[] wordWrap(String text, int maxWidth, Font font) {
        // if (text == null || text.length() == 0 || text.equals(" ")) {
        //     return new String[0];
        // }
        if (cachedFont != font) {
            cachedFont = font;
            cachedMinWidth = font.charWidth('W') + 2;
            cachedSpaceWidth = font.charWidth(' ');
        }
        if (maxWidth < cachedMinWidth) {
            return new String[0];
        }
        
        // text = replace(text, "\r", "");
        Vector lines = new Vector();

        int lineEnd = text.indexOf('\n');
        if (lineEnd != -1) {
            int lineBegin = 0;
            do {
                lines.addElement(text.substring(lineBegin, lineEnd));
                lineBegin = lineEnd + 1;
                lineEnd = text.indexOf('\n', lineBegin);
            }
            while (lineEnd != -1);

            lines.addElement(text.substring(lineBegin));
        } else {
            lines.addElement(text);
        }

        Vector out = new Vector();
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            int lineLength = line.length();

            if (font.stringWidth(line) > maxWidth) {
                // this line is too long for one screen line, so split it into multiple lines based on word boundaries
                int pos = 0;
                int availableWidth = maxWidth;
                StringBuffer outLine = new StringBuffer();

                loop: while (true) {
                    // get the next word (from cursor position to the next space character, or to the end of the line)
                    int nextSpace = line.indexOf(' ', pos);
                    if (nextSpace == -1) {
                        nextSpace = lineLength;
                    }
                    String thisWord = line.substring(pos, nextSpace);
                    int thisWordWidth = font.stringWidth(thisWord);

                    if (thisWordWidth < availableWidth) {
                        // word fits on the current line
                        outLine.append(thisWord);
                        availableWidth -= thisWordWidth;
                    } else {
                        // word doesn't fit on current line -> finish this line
                        if (outLine.length() != 0) {
                            out.addElement(outLine.toString());
                            outLine.setLength(0);
                        }

                        if (thisWordWidth < maxWidth) {
                            // word fits on one line -> add the word to the next line
                            outLine.append(thisWord);
                            availableWidth = maxWidth - thisWordWidth;
                        } else {
                            // word is too long to fit on one line -> split the word
                            for (int c = thisWord.length() - 1; c >= 0; c--) {
                                String splitWord = thisWord.substring(0, c);
    
                                if (font.stringWidth(splitWord) < maxWidth) {
                                    out.addElement(splitWord);
                                    pos += c;
                                    break;
                                }
                            }
                            availableWidth = maxWidth;
                            continue;
                        }
                    }

                    // skip past this word
                    pos += thisWord.length();

                    while (true) {
                        if (pos >= lineLength) break loop;

                        // add space(s) to the end of the current line (a line will never begin with a space)
                        if (line.charAt(pos) != ' ') break;
                        outLine.append(' ');
                        availableWidth -= cachedSpaceWidth;
                        pos++;
                    }
                }
                // add the last remaining line to the output if needed
                if (outLine.length() != 0) {
                    out.addElement(outLine.toString());
                }
            } else {
                // this whole line fits on one screen line, so add it as-is
                out.addElement(line);
            }
        }
        String[] arr = new String[out.size()];
        out.copyInto(arr);
        return arr;
    }
    
    public static final long DISCORD_EPOCH = 1420070400000L;

    public static String formatTimestamp(long timestamp) {
        timestamp += DISCORD_EPOCH;
        long now = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timestamp));

        StringBuffer time = new StringBuffer();

        // Message was sent more than 24 hours ago? (or in the future if the phone's date isn't set)
        if (now - timestamp > 24*60*60*1000 || now < timestamp) {
            // Show date in day/month format
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;

            // if (day < 10) time.append('0');
            time.append(day);
            time.append('/');
            // if (month < 10) time.append('0');
            time.append(month);
        } else {
            // Show time in hour:minute format
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            if (App.use12hTime) {
                char period = hour < 12 ? 'A' : 'P';

                // Convert hours to 12-hour format
                hour = hour % 12;
                if (hour == 0) {
                    hour = 12; // 12 AM or 12 PM
                }

                time.append(hour);
                time.append(':');
                if (minute < 10) time.append('0');
                time.append(minute);
                time.append(period);
            } else {
                time.append(hour);
                time.append(':');
                if (minute < 10) time.append('0');
                time.append(minute);
            }
        }
        return time.toString();
    }
}