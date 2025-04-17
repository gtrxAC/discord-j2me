package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class App extends MIDlet {
	public static App instance;
	public static Display disp;

	public static boolean use12hTime;
	public static int messageLoadCount;
	public static int tokenType;

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

    public static void login() {
        loadFonts();
        disp.setCurrent(new MainMenu());
    }

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

		Alert a = new Alert("Error");
		a.setString(message);
		a.setTimeout(Alert.FOREVER);

		if (next == null) {
			disp.setCurrent(a);
		} else {
			disp.setCurrent(a, next);
		}
	}
	
	public static void error(Exception e, Displayable next) {
		error(e.toString(), next);
	}
	
	public static void error(String message) {
		error(message, null);
	}

	public static void error(Exception e) {
		error(e.toString());
	}

	public static void loadFonts() {
		final int[] fontSizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
		
		authorFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSizes[authorFontSize]);
		timestampFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[authorFontSize]);
		messageFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[messageFontSize]);
	}

	public static void openGuildSelector(boolean reload) {
		if (reload || guildSelector == null || guilds == null) {
			new HTTPThread(HTTPThread.FETCH_GUILDS).start();
		} else {
			disp.setCurrent(guildSelector);
		}
	}

	public static void openChannelSelector(boolean reload) {
		if (reload || channelSelector == null || channels == null) {
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
