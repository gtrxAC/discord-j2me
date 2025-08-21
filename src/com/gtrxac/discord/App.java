package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import javax.microedition.io.file.*;
import java.util.*;
import cc.nnproject.json.*;

public class App implements Strings {
	public static final int VERSION_CODE = 21;
	public static final String VERSION_NAME = "5.1.0 beta2";

	// Should match the app's jar file name (used by auto update system)
	public static final String VERSION_VARIANT =
//#ifdef MIDP2_GENERIC
	"midp2";
//#endif
//#ifdef NOKIA_128PX_VERSION
	"nokia_128px";
//#endif
//#ifdef S40V2
	"s40v2";
//#endif
//#ifdef MIDP2_ALT
	"midp2_alt";
//#endif
//#ifdef S60V2
	"s60v2";
//#endif
//#ifdef BLACKBERRY
	"blackberry";
//#endif
//#ifdef SAMSUNG_FULL
	"samsung";
//#endif
//#ifdef SAMSUNG_100KB
	"samsung_100kb";
//#endif
//#ifdef LG
	"lg";
//#endif
//#ifdef J2ME_LOADER
	"jl";
//#endif

	public static final long DISCORD_EPOCH = 1420070400000L;

	static Display disp;

	static GatewayThread gateway;
	static String uploadToken;
	static String myUserId;
	static boolean isLiteProxy;

	static Vector guilds;
	static Guild selectedGuild;
	static GuildSelector guildSelector;
	static Vector subscribedGuilds;

	static Vector channels;
	static Channel selectedChannel;
	static ChannelSelector channelSelector;
	static boolean channelIsOpen;

	static Vector threads;
	static ThreadSelector threadSelector;
	static Channel selectedChannelForThreads;

	static Vector messages;
	static ChannelView channelView;
	static Vector typingUsers;
	static Vector typingUserIDs;

	static AttachmentView attachmentView;

	// set to true if loading screen shouldn't be shown for next HTTPThread call
	static boolean dontShowLoadScreen;

	static boolean isDM;
	static Vector dmChannels;
	static DMChannel selectedDmChannel;
	static DMSelector dmSelector;

	static Font authorFont;
	static Font timestampFont;
	static Font messageFont;
	static Font titleFont;

	static Icons ic;

    static {
		subscribedGuilds = new Vector();
		IconCache.init();
		NameColorCache.init();
		UnreadManager.init();
    }

    public static void login() {
		ic = null;
		ic = new Icons();

		guilds = null;
		dmChannels = null;

		Theme.load();
        loadFonts();
        disp.setCurrent(MainMenu.get(true));

        if (Settings.useGateway) {
            gateway = new GatewayThread();
            gateway.start();
        }
    }

	public static void error(String message, Displayable next) {
		disp.setCurrent(new Dialog(Locale.get(ERROR_TITLE), message, next));

		// clear banner text (e.g. hide "sending message" text if message sending fails)
		if (channelView != null) channelView.bannerText = null;
	}
	
	public static void error(Exception e, Displayable next) {
		e.printStackTrace();
		error(e.toString(), next);
	}
	
	public static void error(String message) {
		error(message, null);
	}

	public static void error(Exception e) {
		e.printStackTrace();
		error(e.toString());
	}

	public static boolean gatewayActive() {
		return gateway != null && gateway.isAlive();
	}

	public static void loadFonts() {
		final int[] fontSizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};

		authorFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, fontSizes[Settings.authorFontSize]);
		timestampFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, fontSizes[Settings.authorFontSize]);
		messageFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, fontSizes[Settings.messageFontSize]);
		titleFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, fontSizes[Settings.messageFontSize]);

//#ifdef TOUCH_SUPPORT
		KineticScrollingCanvas.scrollUnit = messageFont.getHeight();
//#endif

		ListScreen.setAppearance(messageFont, Settings.menuIconSize, Locale.get(SELECT), Locale.get(SELECT_L), Locale.get(BACK), Locale.get(BACK_L));
		ListScreen.noItemsString = Locale.get(LIST_EMPTY);
		Dialog.okLabel = Locale.get(OK);
		Dialog.okLabelLong = Locale.get(OK_L);
//#ifdef EMOJI_SUPPORT
		FormattedStringPartEmoji.loadEmoji(messageFont.getHeight());
//#endif

//#ifdef NOKIA_THEME_BACKGROUND
        if (Settings.theme != Theme.SYSTEM)
//#endif
        ChannelViewItem.drawUnreadIndicatorImage(null, 0, 0);
	}

	// Required for Wi-Fi support on BlackBerry
	// See https://github.com/shinovon/JTube/blob/670ea59a94d6b5be8af53d94d7804b2d35b64e52/src/jtube/Util.java#L521
	public static String getPlatformSpecificUrl(String url) {
//#ifdef BLACKBERRY
		if (Settings.bbWifi) {
			return url + ";deviceside=true;interface=wifi";
		}
//#endif
		return url;
	}

	public static void updateUnreadIndicators(boolean isDM, String chId) {
		if (isDM) {
			if (dmSelector != null) dmSelector.update(chId);
		} else {
			if (threadSelector != null) threadSelector.update(chId);
			if (channelSelector != null) channelSelector.update(chId);
			if (guildSelector != null) guildSelector.update();
		}
	}

	// The following few methods for opening certain screens take two arguments:
	// - reload: if the screen should be reloaded (its data re-fetched) when the "keep channels loaded" option is disabled
	// - forceReload: if it should be reloaded in any case, even when "keep channels loaded" is enabled

	public static void openGuildSelector(boolean reload, boolean forceReload) {
		if (Settings.highRamMode) reload = false;
		
		if (reload || forceReload || guildSelector == null || guilds == null) {
//#ifdef OVER_100KB
			HTTPThread h = new HTTPThread(HTTPThread.FETCH_GUILDS);
			h.forceReload = forceReload;
			h.start();
//#else
			new HTTPThread(HTTPThread.FETCH_GUILDS).start();
//#endif
		} else {
			try {
				if (guildSelector.isFavGuilds) {
					// Guild list is already loaded but current selector is showing favorite guilds - create new selector from full guild list
					guildSelector = new GuildSelector(guilds, false);
				}
				disp.setCurrent(guildSelector);
			}
			catch (Exception e) {
				error(e);
			}
		}
	}

	public static void openChannelSelector(boolean reload, boolean forceReload) {
		if (Settings.highRamMode) reload = false;
		boolean keepLoaded = !reload && !forceReload;

		if (keepLoaded && channelSelector != null && channels != null && channels == selectedGuild.channels) {
			disp.setCurrent(channelSelector);
		}
		else if (keepLoaded && selectedGuild.channels != null) {
			try {
				channels = selectedGuild.channels;
				channelSelector = new ChannelSelector();
				disp.setCurrent(channelSelector);
			}
			catch (Exception e) {
				error(e);
			}
		}
		else {
			new HTTPThread(HTTPThread.FETCH_CHANNELS).start();
		}
	}

	public static void openThreadSelector(boolean reload, boolean forceReload) {
		if (Settings.highRamMode) reload = false;
		boolean keepLoaded = !reload && !forceReload;

		if (keepLoaded && threadSelector != null && threads != null && threads == selectedChannelForThreads.threads) {
			disp.setCurrent(threadSelector);
		}
		else if (keepLoaded && selectedChannelForThreads.threads != null) {
			try {
				threads = selectedChannelForThreads.threads;
				threadSelector = new ThreadSelector();
				disp.setCurrent(threadSelector);
			}
			catch (Exception e) {
				error(e);
			}
		}
		else {
			new HTTPThread(HTTPThread.FETCH_THREADS).start();
		}
	}

	public static void openDMSelector(boolean reload, boolean forceReload) {
		if (Settings.highRamMode) reload = false;
		
		if (reload || forceReload || dmSelector == null || dmChannels == null) {
			new HTTPThread(HTTPThread.FETCH_DM_CHANNELS).start();
		} else {
			disp.setCurrent(dmSelector);
		}
	}

	public static void openChannelView(boolean reload) {
		if (reload || channelView == null || messages == null) {
			new HTTPThread(HTTPThread.FETCH_MESSAGES).start();
			// markCurrentChannelRead is called by the thread
		} else {
			disp.setCurrent(channelView);
			markCurrentChannelRead();
		}
	}

	public static void markCurrentChannelRead() {
		// Ensure that the channel gets marked as read even when gateway is disabled, by updating the channel's last message ID
		if (!gatewayActive() && messages != null) {
			Message lastMessage = (Message) messages.elementAt(0);
			long newLastMessageID = Long.parseLong(lastMessage.id);
			if (isDM) {
				if (selectedDmChannel.lastMessageID < newLastMessageID) {
					selectedDmChannel.lastMessageID = newLastMessageID;
				}
			} else {
				if (selectedChannel.lastMessageID < newLastMessageID) {
					selectedChannel.lastMessageID = newLastMessageID;
				}
			}
		}
		if (isDM) {
			selectedDmChannel.markRead();
			updateUnreadIndicators(true, selectedDmChannel.id);
		} else {
			selectedChannel.markRead();
			updateUnreadIndicators(false, selectedChannel.id);
		}
	}

	public static void openAttachmentView(boolean reload, Message msg) {
		if (reload || attachmentView == null || attachmentView.msg != msg) {
			attachmentView = new AttachmentView(msg);
			new HTTPThread(HTTPThread.FETCH_ATTACHMENTS).start();
		}
		disp.setCurrent(attachmentView);
	}

	public static void platRequest(String url) {
		try {
			if (DiscordMIDlet.instance.platformRequest(url)) {
//#ifdef OVER_100KB
				disp.setCurrent(new PlatformRequestDialog());
//#else
				error(Locale.get(PLAT_REQUEST_FAILED));
//#endif
			}
		}
		catch (Exception e) {
			String msg =
				Locale.get(PLAT_REQUEST_ERROR_PREFIX) +
				e.toString() +
				Locale.get(PLAT_REQUEST_ERROR_SUFFIX) +
				url;
			disp.setCurrent(new MessageCopyBox(Locale.get(ERROR_TITLE), msg));
		}
	}

//#ifdef EMOJI_SUPPORT
	public static void gatewayToggleGuildEmoji() {
		if (gatewayActive()) {
			JSONObject msg = new JSONObject();
			msg.put("op", -1);
			msg.put("t", "GATEWAY_SHOW_GUILD_EMOJI");
			msg.put("d", FormattedString.emojiMode == FormattedString.EMOJI_MODE_ALL);
			gateway.send(msg);
		}
	}
//#endif

//#ifdef OVER_100KB
	public static void gatewaySendTyping() {
		if (gatewayActive() && Settings.sendTyping) {
			JSONObject msg = new JSONObject();
			msg.put("op", -1);
			msg.put("t", "GATEWAY_SEND_TYPING");
			msg.put("d", isDM ? selectedDmChannel.id : selectedChannel.id);
			gateway.send(msg);
		}
	}
//#endif

    public static Displayable createTextEntryScreen(Message recipientMsg, String fileName, FileConnection fc) {
        if (recipientMsg != null) {
            return new ReplyForm(recipientMsg, fileName, fc);
        } else {
            return new MessageBox(fileName, fc);
        }
    }
}
