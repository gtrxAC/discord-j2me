package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class State {
	public static final long DISCORD_EPOCH = 1420070400000L;

	MIDlet midlet;
	Display disp;

	int theme;  // 0 = dark, 1 = light, 2 = black
	boolean oldUI;
	boolean use12hTime;
	boolean useGateway;
	boolean bbWifi;
	int messageLoadCount;

	int authorFontSize;
	int messageFontSize;
	Font authorFont;
	Font timestampFont;
	Font messageFont;

	HTTPThing http;
	GatewayThread gateway;

	Vector guilds;
	Guild selectedGuild;
	GuildSelector guildSelector;
	Vector subscribedGuilds;

	Vector channels;
	Channel selectedChannel;
	ChannelSelector channelSelector;

	Vector messages;
	ChannelView channelView;
	OldChannelView oldChannelView;
	Vector typingUsers;
	Vector typingUserIDs;

	AttachmentView attachmentView;

	// Parameters for message/reply sending
	String sendMessage;
	String sendReference;  // ID of the message the user is replying to
	boolean sendPing;

	boolean isDM;
	Vector dmChannels;
	DMChannel selectedDmChannel;
	DMSelector dmSelector;

	public State() {
		subscribedGuilds = new Vector();
	}

	private Alert createError(String message) {
		Alert error = new Alert("Error");
		error.setTimeout(Alert.FOREVER);
		error.setString(message);
		return error;
	}

	public void error(String message) {
		disp.setCurrent(createError(message));
	}

	public void error(String message, Displayable next) {
		disp.setCurrent(createError(message), next);
	}

	public void loadFonts() {
		final int[] fontSizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
		
		authorFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSizes[authorFontSize]);
		timestampFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[authorFontSize]);
		messageFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[messageFontSize]);
	}

	public static boolean isBlackBerry() {
		String p = System.getProperty("microedition.platform");
		if (p == null) p = "";
		return p.toLowerCase().startsWith("blackberry");
	}

	// Required for Wi-Fi support on BlackBerry
	// See https://github.com/shinovon/JTube/blob/670ea59a94d6b5be8af53d94d7804b2d35b64e52/src/jtube/Util.java#L521
	public String getPlatformSpecificUrl(String url) {
		if(isBlackBerry() && bbWifi) {
			return url + ";deviceside=true;interface=wifi";
		}
		return url;
	}

	public void openGuildSelector(boolean reload) {
		try {
			if (reload || guildSelector == null || guilds == null) {
				new HTTPThread(this, HTTPThread.FETCH_GUILDS).start();
			} else {
				disp.setCurrent(guildSelector);
			}
		}
		catch (Exception e) {
			error(e.toString());
		}
	}

	public void openChannelSelector(boolean reload) {
		try {
			if (reload || channelSelector == null || channels == null) {
				new HTTPThread(this, HTTPThread.FETCH_CHANNELS).start();
			} else {
				disp.setCurrent(channelSelector);
			}
		}
		catch (Exception e) {
			error(e.toString());
		}
	}

	public void openDMSelector(boolean reload) {
		try {
			if (reload || dmSelector == null || dmChannels == null) {
				new HTTPThread(this, HTTPThread.FETCH_DM_CHANNELS).start();
			} else {
				disp.setCurrent(dmSelector);
			}
		}
		catch (Exception e) {
			error(e.toString());
		}
	}

	public void openChannelView(boolean reload) {
		try {
			if (oldUI) {
				if (reload || oldChannelView == null || messages == null) {
					new HTTPThread(this, HTTPThread.FETCH_MESSAGES).start();
				} else {
					disp.setCurrent(oldChannelView);
				}
			} else {
				if (reload || channelView == null || messages == null) {
					new HTTPThread(this, HTTPThread.FETCH_MESSAGES).start();
				} else {
					disp.setCurrent(channelView);
				}
			}
		}
		catch (Exception e) {
			error(e.toString());
		}
	}

	public void openAttachmentView(boolean reload, Message msg) {
		try {
			if (reload || attachmentView == null || attachmentView.msg != msg) {
				attachmentView = new AttachmentView(this, msg);
			}
			disp.setCurrent(attachmentView);
		}
		catch (Exception e) {
			error(e.toString());
		}
	}
}
