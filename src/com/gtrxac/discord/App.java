package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class App extends MIDlet {
	public static App instance;
	public static Display disp;

	public static boolean use12hTime;
	public static int messageLoadCount;
	public static boolean listTimestamps;

	public static int authorFontSize;
	public static int messageFontSize;
	public static Font authorFont;
	public static Font timestampFont;
	public static Font messageFont;
	public static int theme;

	public static String api;
	public static String token;
	public static String myUserId;

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

            if (!Settings.isAvailable() && getAppProperty("Token") == null) {
                disp.setCurrent(new LoginForm());
            } else {
                Settings.load();
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
		Message.groupSpacing = Math.max(1, Math.min(ChannelViewItem.fontHeight/5, (Util.screenHeight+2)/49));
		Message.screenMargin = Math.min(messageFont.stringWidth(" "), Util.screenWidth/58);
		Message.timestampDistance = authorFont.stringWidth(" ")*4/3;
        Message.arrowStringWidth = timestampFont.stringWidth(" > ");

		//                               Monochrome Dark      Light
		final int[] backgroundColors =   {0xFFFFFF, 0x313338, 0xFFFFFF};
		final int[] highlightColors =    {0x000000, 0x1E1F22, 0xBBBBBB};
		final int[] buttonColors =       {0xFFFFFF, 0x2B2D31, 0xCCCCCC};
		final int[] selButtonColors =    {0x000000, 0x1E1F22, 0xAAAAAA};
		final int[] messageColors =      {0x000000, 0xE8E8E8, 0x181818};
		final int[] selMessageColors =   {0xFFFFFF, 0xFFFFFF, 0x000000};
		final int[] authorColors =       {0x000000, 0xFFFFFF, 0x000000};
		final int[] timestampColors =    {0x000000, 0xAAAAAA, 0x777777};
		final int[] selTimestampColors = {0xFFFFFF, 0xBBBBBB, 0x555555};
		
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
}
