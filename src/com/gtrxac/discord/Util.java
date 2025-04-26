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

	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

    /**
     * Get array of text lines to draw (word wrap)
     * https://github.com/shinovon/JTube/blob/2.6.1/src/jtube/Util.java
     */
    public static String[] wordWrap(String text, int maxWidth, Font font) {
		if (text == null || text.length() == 0 || text.equals(" ") || maxWidth < font.charWidth('W') + 2) {
			return new String[0];
		}
		text = replace(text, "\r", "");
		Vector v = new Vector(3);
		char[] chars = text.toCharArray();
		if (text.indexOf('\n') > -1) {
			int j = 0;
			for (int i = 0; i < text.length(); i++) {
				if (chars[i] == '\n') {
					v.addElement(text.substring(j, i));
					j = i + 1;
				}
			}
			v.addElement(text.substring(j, text.length()));
		} else {
			v.addElement(text);
		}
		for (int i = 0; i < v.size(); i++) {
			String s = (String) v.elementAt(i);
			if(font.stringWidth(s) >= maxWidth) {
				int i1 = 0;
				for (int i2 = 0; i2 < s.length(); i2++) {
					if (font.stringWidth(s.substring(i1, i2+1)) >= maxWidth) {
						boolean space = false;
						for (int j = i2; j > i1; j--) {
							char c = s.charAt(j);
							if (c == ' ' || (c >= ',' && c <= '/')) {
								space = true;
								v.setElementAt(s.substring(i1, j + 1), i);
								v.insertElementAt(s.substring(j + 1), i + 1);
								i += 1;
								i2 = i1 = j + 1;
								break;
							}
						}
						if (!space) {
							i2 = i2 - 2;
							v.setElementAt(s.substring(i1, i2), i);
							v.insertElementAt(s.substring(i2), i + 1);
							i2 = i1 = i2 + 1;
							i += 1;
						}
					}
				}
			}
		}
		String[] arr = new String[v.size()];
		v.copyInto(arr);
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