package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;
import java.lang.*;

/**
 * Contains the app's state and methods related to the app's lifecycle, as well as some generic utility methods.
 */
public class App extends MIDlet {
	public static App instance;
	public static Display disp;

	public static boolean use12hTime;
	public static int messageLoadCount;
	public static boolean listTimestamps;
	public static boolean markAsRead;

	public static int authorFontSize;
	public static int messageFontSize;
	public static Font authorFont;
	public static Font timestampFont;
	public static Font messageFont;
	public static int theme;

	public static String api;
	public static String token;

	public static Vector guilds;
	public static DiscordObject selectedGuild;
	public static DiscordObject loadedGuild;
	public static GuildSelector guildSelector;

	public static Vector channels;
	public static DiscordObject selectedChannel;
	public static ChannelSelector channelSelector;

	public static Vector messages;
	public static ChannelView channelView;

	public static boolean isDM;

    public App() {
		instance = this;
    }

    public void startApp() {
        if (disp == null) {
            disp = Display.getDisplay(this);
            Settings.load();

            if (App.token == null || App.token.trim().length() == 0) {
                disp.setCurrent(new LoginForm());
            } else {
                login();
            }
        }
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

	public static void error(String message, Displayable next) {
		Displayable current = disp.getCurrent();

		if (current instanceof Alert) {
			((Alert) current).setString(message);
			if (next == null) {
				disp.setCurrent(current);
			} else {
				disp.setCurrent((Alert) current, next);
			}
			return;
		}

		Alert a = new Alert("Error", message, null, AlertType.ERROR);
		a.setTimeout(Alert.FOREVER);

		if (next == null) {
			disp.setCurrent(a);
		} else {
			disp.setCurrent(a, next);
		}
	}
	
	public static void error(Exception e, Displayable next) {
		e.printStackTrace();
		error(e.toString(), next);
	}
	
	public static void error(String message) {
		error(message, null);
	}

	public static void error(Exception e) {
		error(e, null);
	}

	public static void login() {
		final int[] fontSizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
		
		authorFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSizes[authorFontSize]);
		timestampFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[authorFontSize]);
		messageFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[messageFontSize]);

        ChannelViewItem.fontHeight = messageFont.getHeight();
        ChannelViewItem.authorFontHeight = authorFont.getHeight();
		Message.margin = Math.max(1, ChannelViewItem.fontHeight/8);
		Message.groupSpacing = Math.max(1, Math.min(ChannelViewItem.fontHeight/5, (App.screenHeight+2)/49));
		Message.screenMargin = Math.min(messageFont.stringWidth(" "), App.screenWidth/58);
		Message.timestampDistance = authorFont.stringWidth(" ")*4/3;
        Message.arrowStringWidth = timestampFont.stringWidth(" > ");

        Message.pseudoBold = (messageFontSize == authorFontSize) && !Settings.hasBoldFont();

		//                               Monochrome Dark      Light
		final int[] backgroundColors =   {0xFFFFFF, 0x313338, 0xFFFFFF};
		final int[] highlightColors =    {0x000000, 0x1E1F22, 0xCCCCDD};
		final int[] buttonColors =       {0xFFFFFF, 0x2B2D31, 0xEEEEEE};
		final int[] selButtonColors =    {0x000000, 0x17181A, 0xBBBBCC};
		final int[] messageColors =      {0x000000, 0xE8E8E8, 0x181818};
		final int[] selMessageColors =   {0xFFFFFF, 0xFFFFFF, 0x000000};  // selected author name uses the same color
		final int[] authorColors =       {0x000000, 0xFFFFFF, 0x000000};
		final int[] timestampColors =    {0x000000, 0xAAAAAA, 0x777788};
		final int[] selTimestampColors = {0xFFFFFF, 0xBBBBBB, 0x555566};
		
		ChannelViewItem.backgroundColor = backgroundColors[theme];
		ChannelViewItem.highlightColor = highlightColors[theme];
		ChannelViewItem.buttonColor = buttonColors[theme];
		ChannelViewItem.selButtonColor = selButtonColors[theme];
		ChannelViewItem.messageColor = messageColors[theme];
		ChannelViewItem.selMessageColor = selMessageColors[theme];
		ChannelViewItem.authorColor = authorColors[theme];
		ChannelViewItem.timestampColor = timestampColors[theme];
		ChannelViewItem.selTimestampColor = selTimestampColors[theme];
		
		ChannelViewItem.olderMessagesButton = new ChannelViewItem("Older messages");
		ChannelViewItem.newerMessagesButton = new ChannelViewItem("Newer messages");

		MainMenu.lastSelected = 0;
        disp.setCurrent(new MainMenu());
	}

	public static void openGuildSelector(boolean reload) {
		if (reload || guildSelector == null || guilds == null) {
			new HTTPThread(HTTPThread.FETCH_GUILDS).start();
		} else {
			if (guildSelector.isFavGuilds) {
				// Guild list is already loaded but current selector is showing favorite guilds - create new selector from full guild list
				guildSelector = new GuildSelector(guilds, false);
			}
			disp.setCurrent(guildSelector);
		}
	}

	public static void openChannelSelector(boolean reload) {
		if (reload || channelSelector == null || channels == null || selectedGuild != loadedGuild) {
			int action = isDM ? HTTPThread.FETCH_DM_CHANNELS : HTTPThread.FETCH_CHANNELS;
			new HTTPThread(action).start();
		} else {
			disp.setCurrent(channelSelector);
		}
	}

	public static void openChannelView(boolean reload) {
		if (reload || channelView == null || messages == null) {
			new HTTPThread(HTTPThread.FETCH_MESSAGES).start();
		} else {
			disp.setCurrent(channelView);
		}
	}

	// System info and generic utilities

    public static final boolean isNokia;
    public static final boolean isMidp2;
    public static final boolean isSejp1;  // Sony Ericsson Java Platform 1 (T610, T630, Z600)
    public static final int screenWidth;
    public static final int screenHeight;
    public static int charsPerItem = 0;

    static {
        String plat = System.getProperty("microedition.platform");
        if (plat == null) plat = "";
        isNokia = (plat.indexOf("Nokia") != -1);
        isMidp2 = hasClass("javax.microedition.lcdui.Spacer");
        isSejp1 = plat.indexOf("SonyEr") != -1 && !isMidp2;

        ChannelView canvas = new ChannelView(true);
        screenWidth = canvas.getWidth();
        screenHeight = canvas.getHeight();

        if (isNokia) {
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
                case 240:
                case 320: {
                    // if it's 240p s40 (not symbian), we need to do a font fix similar to series 60 like above, but shift the font upwards
                    boolean isSymbian = plat.indexOf("platform=S60") != -1 ||
                        System.getProperty("com.symbian.midp.serversocket.support") != null ||
                        System.getProperty("com.symbian.default.to.suite.icon") != null ||
                        hasClass("com.symbian.midp.io.protocol.http.Protocol") ||
                        hasClass("com.symbian.lcdjava.io.File");

                    if (!isSymbian) ChannelViewItem.fontYOffset = -1;
                    break;
                }
                case 128: {
                    // 128px wide -> could be midp1 or midp2
                    // if it's midp1, it's s40v1, so we must limit the chars per item (read trimItem comment)
                    if (!isMidp2) charsPerItem = 14;
                    break;
                }
                case 96: {
                    charsPerItem = 12;
                    break;
                }
            }
        }
        else if (screenWidth == 128 && "j2me".equals(plat)) {
            // probably a samsung with list line wrapping (read trimItem comment)
            // even if it isn't, limiting to 19 isn't a big deal
            // they all seem to have microedition platform = "j2me" but also some others do (like LG)
            charsPerItem = 19;
        }
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (Throwable e) {}
            
        return false;
    }

    // Trim list item's text to the maximum length that can fit on one line on the screen
    // Some phones (most notably Nokia S40v1 and certain 128x160 Samsungs, like E250) use line wrapping in List screens and we don't want that
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
        timestamp += DISCORD_EPOCH;  // remove this if using this function in a different app

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int todayDay = cal.get(Calendar.DAY_OF_MONTH);
        int todayMonth = cal.get(Calendar.MONTH);
        int todayYear = cal.get(Calendar.YEAR);

        cal.setTime(new Date(timestamp));
        int stampDay = cal.get(Calendar.DAY_OF_MONTH);
        int stampMonth = cal.get(Calendar.MONTH);
        int stampYear = cal.get(Calendar.YEAR);

        StringBuffer out = new StringBuffer();

        if (todayDay != stampDay || todayMonth != stampMonth || todayYear != stampYear) {
            // Timestamp does not point to today -> show date in day/month format
            // Leading zeros in numbers are optional, here I left them out for the sake of compactness

            out.append(stampDay).append('/').append(stampMonth + 1);

            // With leading zeros:
            // stampMonth++;
            // if (stampDay < 10) time.append('0');
            // out.append(stampDay).append('/');
            // if (stampMonth < 10) time.append('0');
            // time.append(stampMonth);
        } else {
            // Today -> show time in hour:minute format
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            if (App.use12hTime) {
                char period = hour < 12 ? 'A' : 'P';

                // Convert hours to 12-hour format
                hour = hour % 12;
                if (hour == 0) {
                    hour = 12; // 12 AM or 12 PM
                }

                out.append(hour).append(':');
                if (minute < 10) out.append('0');
                out.append(minute).append(period);
            } else {
                out.append(hour).append(':');
                if (minute < 10) out.append('0');
                out.append(minute);
            }
        }
        return out.toString();
    }
}
