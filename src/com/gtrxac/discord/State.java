package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class State implements Strings {
	public static final long DISCORD_EPOCH = 1420070400000L;

	static final int PFP_TYPE_NONE = 0;
	static final int PFP_TYPE_SQUARE = 1;
	static final int PFP_TYPE_CIRCLE = 2;
	static final int PFP_TYPE_CIRCLE_HQ = 3;

	static final int TOKEN_TYPE_HEADER = 0;
	static final int TOKEN_TYPE_JSON = 1;
	static final int TOKEN_TYPE_QUERY = 2;

	static final int PFP_SIZE_PLACEHOLDER = 0;
	static final int ICON_SIZE_OFF = 0;
	static final int ICON_SIZE_16 = 1;
	static final int ICON_SIZE_32 = 2;

	MIDlet midlet;
	Display disp;

	int theme;  // 0 = dark, 1 = light, 2 = black
	boolean use12hTime;
	boolean useGateway;
	boolean bbWifi;
	int messageLoadCount;
	boolean useJpeg;
	int attachmentSize;
	int pfpType;
	int pfpSize;
	int menuIconSize;
	boolean nativeFilePicker;
	boolean autoReConnect;
	boolean showMenuIcons;
	int tokenType;
	boolean useNameColors;
	boolean showRefMessage;
	boolean defaultHotkeys;
	String language;
	boolean fullscreenDefault;
	boolean showNotifsAll;
	boolean showNotifsPings;
	boolean showNotifsDMs;
	boolean showNotifAlert;
	boolean showNotifPigler;
	boolean playNotifSound;
	boolean highRamMode;

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
	String uploadToken;

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
	Vector typingUsers;
	Vector typingUserIDs;

	AttachmentView attachmentView;

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
	int fullscreenHotkey;

	Icons ic;

	public State() {
		subscribedGuilds = new Vector();
		iconCache = new IconCache(this);
		nameColorCache = new NameColorCache(this);
		unreads = new UnreadManager(this);
	}

    public void login() {
		ic = null;
		ic = new Icons(this);

		guilds = null;
		dmChannels = null;

		loadTheme();
        loadFonts();
        http = new HTTPThing(this);
        disp.setCurrent(MainMenu.get(this));

        if (useGateway) {
            this.gateway = new GatewayThread(this);
            this.gateway.start();
        }
    }

	public void showAlert(String title, String message, Displayable next) {
		// J2ME has this stupid limitation where you cannot have two Alerts stacked
		// on top of each other. So we work around that by checking if there is an
		// existing alert. If so, change the text shown in the alert, instead of
		// creating a new alert.
		Displayable current = disp.getCurrent();

		if (current instanceof ErrorAlert) {
			((ErrorAlert) current).update(title, message, next);
			return;
		}

		// No existing alert - create a new one and show it.
		// Note: this might still sometimes fail if two alerts are shown at just the right time
		try {
			disp.setCurrent(new ErrorAlert(disp, title, message, next));
		}
		catch (Exception e) {}
	}

	public void error(String message, Displayable next) {
		showAlert(Locale.get(ERROR_TITLE), message, next);

		// clear banner text (e.g. hide "sending message" text if message sending fails)
		if (channelView != null) channelView.bannerText = null;
	}
	
	public void error(Exception e, Displayable next) {
		e.printStackTrace();
		error(e.toString(), next);
	}
	
	public void error(String message) {
		error(message, null);
	}

	public void error(Exception e) {
		e.printStackTrace();
		error(e.toString());
	}

	public void loadFonts() {
		final int[] fontSizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
		
		authorFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSizes[authorFontSize]);
		timestampFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[authorFontSize]);
		messageFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSizes[messageFontSize]);
		titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSizes[messageFontSize]);

		ListScreen.setAppearance(messageFont, menuIconSize, Locale.get(SELECT), Locale.get(SELECT_L), Locale.get(BACK), Locale.get(BACK_L));
		ListScreen.noItemsString = Locale.get(LIST_EMPTY);
	}

	public void loadTheme() {
		//                               Dark      Light     Black
		final int[] listBgColors      = {0x2b2d31, 0xffffff, 0x000000};
		final int[] listTextColors    = {0xdddddd, 0x222222, 0xdddddd};
		final int[] listSelTextColors = {0xffffff, 0x000000, 0xffffff};
		final int[] listHlColors      = {0x404249, 0xbbbbbb, 0x333333};

		ListScreen.backgroundColor = listBgColors[theme];
		ListScreen.textColor = listTextColors[theme];
		ListScreen.selectedTextColor = listSelTextColors[theme];
		ListScreen.highlightColor = listHlColors[theme];
		ListScreen.noItemsColor = ChannelView.timestampColors[theme];
	}

	public boolean gatewayActive() {
		return gateway != null && gateway.isAlive();
	}

	// Required for Wi-Fi support on BlackBerry
	// See https://github.com/shinovon/JTube/blob/670ea59a94d6b5be8af53d94d7804b2d35b64e52/src/jtube/Util.java#L521
	public String getPlatformSpecificUrl(String url) {
		if (Util.isBlackBerry && bbWifi) {
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

	public void openGuildSelector(boolean reload, boolean forceReload) {
		if (highRamMode) reload = false;
		
		if (reload || forceReload || guildSelector == null || guilds == null) {
			new HTTPThread(this, HTTPThread.FETCH_GUILDS).start();
		} else {
			try {
				if (guildSelector.isFavGuilds) {
					// Guild list is already loaded but current selector is showing favorite guilds - create new selector from full guild list
					guildSelector = new GuildSelector(this, guilds, false);
				}
				disp.setCurrent(guildSelector);
			}
			catch (Exception e) {
				error(e);
			}
		}
	}

	public void openChannelSelector(boolean reload, boolean forceReload) {
		if (highRamMode) reload = false;
		boolean keepLoaded = !reload && !forceReload;

		if (keepLoaded && channelSelector != null && channels != null && channels == selectedGuild.channels) {
			disp.setCurrent(channelSelector);
		}
		else if (keepLoaded && selectedGuild.channels != null) {
			try {
				channels = selectedGuild.channels;
				channelSelector = new ChannelSelector(this);
				disp.setCurrent(channelSelector);
			}
			catch (Exception e) {
				error(e);
			}
		}
		else {
			new HTTPThread(this, HTTPThread.FETCH_CHANNELS).start();
		}
	}

	public void openDMSelector(boolean reload, boolean forceReload) {
		if (highRamMode) reload = false;
		
		if (reload || forceReload || dmSelector == null || dmChannels == null) {
			new HTTPThread(this, HTTPThread.FETCH_DM_CHANNELS).start();
		} else {
			disp.setCurrent(dmSelector);
		}
	}

	public void openChannelView(boolean reload) {
		if (reload || channelView == null || messages == null) {
			new HTTPThread(this, HTTPThread.FETCH_MESSAGES).start();
		} else {
			disp.setCurrent(channelView);
		}
		if (isDM) {
			unreads.markRead(selectedDmChannel);
			updateUnreadIndicators(true, selectedDmChannel.id);
		} else {
			unreads.markRead(selectedChannel);
			updateUnreadIndicators(false, selectedChannel.id);
		}
	}

	public void openAttachmentView(boolean reload, Message msg) {
		if (reload || attachmentView == null || attachmentView.msg != msg) {
			attachmentView = new AttachmentView(this, msg);
			new HTTPThread(this, HTTPThread.FETCH_ATTACHMENTS).start();
		}
		disp.setCurrent(attachmentView);
	}

	public void platformRequest(String url) {
		try {
			if (midlet.platformRequest(url)) {
				error(Locale.get(PLAT_REQUEST_FAILED));
			}
		}
		catch (Exception e) {
			String msg =
				Locale.get(PLAT_REQUEST_ERROR_PREFIX) +
				e.toString() +
				Locale.get(PLAT_REQUEST_ERROR_SUFFIX) +
				url;
			disp.setCurrent(new MessageCopyBox(this, Locale.get(ERROR_TITLE), msg));
		}
	}
}
