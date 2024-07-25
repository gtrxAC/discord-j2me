package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class State {
	public static final long DISCORD_EPOCH = 1420070400000L;

	static final int ICON_TYPE_NONE = 0;
	static final int ICON_TYPE_SQUARE = 1;
	static final int ICON_TYPE_CIRCLE = 2;
	static final int ICON_TYPE_CIRCLE_HQ = 3;

	static final int TOKEN_TYPE_HEADER = 0;
	static final int TOKEN_TYPE_JSON = 1;
	static final int TOKEN_TYPE_QUERY = 2;

	MIDlet midlet;
	Display disp;

	int theme;  // 0 = dark, 1 = light, 2 = black
	boolean oldUI;
	boolean use12hTime;
	boolean useGateway;
	boolean bbWifi;
	int messageLoadCount;
	boolean useJpeg;
	int attachmentSize;
	int iconType;
	int iconSize;  // 0 = draw placeholder pfp with initials, 1 = 16px, 2 = 32px
	boolean nativeFilePicker;
	boolean autoReConnect;
	boolean showMenuIcons;
	int tokenType;
	boolean useNameColors;
	boolean showRefMessage;
	boolean defaultHotkeys;

	int authorFontSize;
	int messageFontSize;
	Font authorFont;
	Font timestampFont;
	Font messageFont;
	Font titleFont;

	HTTPThing http;
	GatewayThread gateway;
	String api;
	String gatewayUrl;
	String cdn;
	String token;

	String myUserId;
	boolean isLiteProxy;

	IconCache iconCache;
	NameColorCache nameColorCache;
	UnreadManager unreads;

	Vector guilds;
	Guild selectedGuild;
	GuildSelector guildSelector;
	Vector subscribedGuilds;

	Vector channels;
	Channel selectedChannel;
	ChannelSelector channelSelector;
	boolean channelIsOpen;

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

	// set to true if loading screen shouldn't be shown for next HTTPThread call
	boolean dontShowLoadScreen;

	boolean isDM;
	Vector dmChannels;
	DMChannel selectedDmChannel;
	DMSelector dmSelector;

	int sendHotkey;
	int replyHotkey;
	int copyHotkey;
	int refreshHotkey;
	int backHotkey;

	Icons ic;

	public State() {
		subscribedGuilds = new Vector();
		iconCache = new IconCache(this);
		nameColorCache = new NameColorCache(this);
		unreads = new UnreadManager(this);
	}

    public void login() {
		ic = null;
		ic = new Icons(iconSize == 2);

        loadFonts();
        http = new HTTPThing(this);
        disp.setCurrent(new MainMenu(this));

        if (useGateway) {
            this.gateway = new GatewayThread(this);
            this.gateway.start();
        }
    }

	public void error(String message, Displayable next) {
		// J2ME has this stupid limitation where you cannot have two Alerts stacked
		// on top of each other. So we work around that by checking if there is an
		// existing alert. If so, change the text shown in the alert, instead of
		// creating a new alert.
		Displayable current = disp.getCurrent();

		if (current instanceof ErrorAlert) {
			((ErrorAlert) current).update(message, next);
			return;
		}

		// No existing alert - create a new one and show it.
		// Note: this might still sometimes fail if two errors occur at just the right time
		try {
			disp.setCurrent(new ErrorAlert(disp, message, next));
		}
		catch (Exception e) {}

		// clear banner text (e.g. hide "sending message" text if message sending fails)
		if (channelView != null) channelView.bannerText = null;
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
		titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSizes[messageFontSize]);
	}

	public boolean gatewayActive() {
		return gateway != null && gateway.isAlive();
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

	public void updateUnreadIndicators(boolean isDM, String chId) {
		if (isDM) {
			if (dmSelector != null) dmSelector.update(chId);
		} else {
			if (channelSelector != null) channelSelector.update(chId);
			if (guildSelector != null) guildSelector.update();
		}
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
			error(e);
		}
	}

	public void openChannelSelector(boolean reload) {
		try {
			if (!reload && channelSelector != null && channels != null && channels == selectedGuild.channels) {
				disp.setCurrent(channelSelector);
			}
			else if (!reload && selectedGuild.channels != null) {
				channels = selectedGuild.channels;
				channelSelector = new ChannelSelector(this);
				disp.setCurrent(channelSelector);
			}
			else {
				new HTTPThread(this, HTTPThread.FETCH_CHANNELS).start();
			}
		}
		catch (Exception e) {
			error(e);
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
			error(e);
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
			if (isDM) {
				unreads.markRead(selectedDmChannel);
				updateUnreadIndicators(true, selectedDmChannel.id);
			} else {
				unreads.markRead(selectedChannel);
				updateUnreadIndicators(false, selectedChannel.id);
			}
		}
		catch (Exception e) {
			error(e);
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
			error(e);
		}
	}

	public void platformRequest(String url) {
		try {
			if (midlet.platformRequest(url)) {
				error("The app must be closed before the URL can be opened.");
			}
		}
		catch (Exception e) {
			String msg = "The URL could not be opened (" + e.toString() + ")\n\nYou may try manually copying the URL into your device's browser: " + url;
			disp.setCurrent(new MessageCopyBox(this, "Error", msg));
		}
	}
}
