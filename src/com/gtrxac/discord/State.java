package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class State {
	public static final long DISCORD_EPOCH = 1420070400000L;

	static final int TOKEN_TYPE_HEADER = 0;
	static final int TOKEN_TYPE_JSON = 1;
	static final int TOKEN_TYPE_QUERY = 2;

	MIDlet midlet;
	Display disp;

	boolean use12hTime;
	int messageLoadCount;
	int tokenType;

	int authorFontSize;
	int messageFontSize;
	Font authorFont;
	Font timestampFont;
	Font messageFont;
	int theme;

	HTTPThing http;
	String api;
	String token;
	String myUserId;

	Vector guilds;
	DiscordObject selectedGuild;
	GuildSelector guildSelector;

	Vector channels;
	DiscordObject selectedChannel;
	ChannelSelector channelSelector;

	Vector messages;
	ChannelView channelView;

	boolean isDM;

	public State() { }

    public void login() {
        loadFonts();
        http = new HTTPThing(this);
        disp.setCurrent(new MainMenu(this));
    }

	public void error(String message, Displayable next) {
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
	
	public void error(Exception e, Displayable next) {
		error(e.toString(), next);
	}
	
	public void error(String message) {
		error(message, null);
	}

	public void error(Exception e) {
		error(e.toString());
	}

	public void loadFonts() {
		final int[] fontSizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
		
		authorFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSizes[authorFontSize]);
		timestampFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[authorFontSize]);
		messageFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[messageFontSize]);
	}

	public void openGuildSelector(boolean reload) {
		if (reload || guildSelector == null || guilds == null) {
			new HTTPThread(this, HTTPThread.FETCH_GUILDS).start();
		} else {
			disp.setCurrent(guildSelector);
		}
	}

	public void openChannelSelector(boolean reload) {
		if (reload || channelSelector == null || channels == null) {
			int action = isDM ? HTTPThread.FETCH_DM_CHANNELS : HTTPThread.FETCH_CHANNELS;
			new HTTPThread(this, action).start();
		} else {
			disp.setCurrent(channelSelector);
		}
	}

	public void openChannelView(boolean reload) {
		if (reload || channelView == null || messages == null) {
			new HTTPThread(this, HTTPThread.FETCH_MESSAGES).start();
		} else {
			disp.setCurrent(channelView);
		}
	}
}
