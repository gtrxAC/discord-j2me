package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;

public class Settings {
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

	static final int AUTO_UPDATE_OFF = 0;
	static final int AUTO_UPDATE_RELEASE_ONLY = 1;
	static final int AUTO_UPDATE_ALL = 2;
    
	static int theme;  // 0 = dark, 1 = light, 2 = black
	static boolean use12hTime;
	static boolean useGateway;
//#ifdef BLACKBERRY
	static boolean bbWifi;
//#endif
	static int messageLoadCount;
	static boolean useJpeg;
	static int attachmentSize;
	static int pfpType;
	static int pfpSize;
	static int menuIconSize;
	static boolean nativeFilePicker;
	static boolean autoReConnect;
	static boolean showMenuIcons;
	static int tokenType;
	static boolean useNameColors;
	static boolean showRefMessage;
	static boolean defaultHotkeys;
	static String language;
	static boolean fullscreenDefault;
	static boolean showNotifsAll;
	static boolean showNotifsPings;
	static boolean showNotifsDMs;
	static boolean showNotifAlert;
//#ifdef PIGLER_SUPPORT
	static boolean showNotifPigler;
//#endif
//#ifdef NOKIA_UI_SUPPORT
	static boolean showNotifNokiaUI;
//#endif
	static boolean playNotifSound;
	static boolean playNotifVibra;
	static boolean highRamMode;
	static int autoUpdate;
//#ifdef OVER_100KB
	static boolean useFilePreview;
	static boolean sendTyping;
    static boolean hasSeenUploadWarning;
//#endif
//#ifdef PROXYLESS_SUPPORT
    static boolean proxyless;
//#endif

	static int authorFontSize;
	static int messageFontSize;

	static String api;
	static String gatewayUrl;
	static String cdn;
	static String token;

	static int sendHotkey;
	static int replyHotkey;
	static int copyHotkey;
	static int refreshHotkey;
	static int backHotkey;
	static int fullscreenHotkey;
//#ifdef OVER_100KB
	static int scrollTopHotkey;
	static int scrollBottomHotkey;
//#endif

    private static RecordStore loginRms;
    private static JSONArray loginData;
    private static int index;

    public static int getBestMenuIconSize() {
        int height = Util.fontSize;
        int result = height/16*16;
        if (height - result >= 6) result += 16;
        if (result == 0) return 8;
        return result;
    }

//#ifdef EMOJI_SUPPORT
    private static boolean shouldShowEmoji() {
        boolean result = (Util.fontSize > 14);

//#ifdef NOKIA_128PX
        // On Nokia 128x160, the default font size (medium) is large enough to somewhat comfortably show emojis.
        // However, on low-end (DCT4) phones, there may not be enough RAM (usually around 600 kB), so disable emojis there just to stay safe.
        if (Runtime.getRuntime().totalMemory() >= 1000000) {
            result = true;
        }
//#endif

        return result;
    }
//#endif

    public static void load() {
        // Initial settings (will be used if there are no saved settings)
        api = "http://146.59.80.3";
        gatewayUrl = "socket://146.59.80.3:8081";
        cdn = "http://146.59.80.3:8080";
        token = "";

        // Check if token is supplied in JAD or manifest. If so, use that as the default.
        String manifestToken = App.instance.getAppProperty("Token");
        if (manifestToken != null) token = manifestToken;

        // Check if save file in old format is available.
        // If so, load token from there and delete the old file.
        boolean foundOldToken = false;
        try {
            RecordStore oldRms = RecordStore.openRecordStore("login", false);
            String oldToken = Util.bytesToString(oldRms.getRecord(2)).trim();

            if (oldToken.length() > 0) {
                token = oldToken;
                foundOldToken = true;
            }
            Util.closeRecordStore(oldRms);
            RecordStore.deleteRecordStore("login");
        }
        catch (Exception e) {}

        try {
            loginRms = RecordStore.openRecordStore("settings", false);
            loginData = JSON.getArray(Util.bytesToString(loginRms.getRecord(1)));
        }
        catch (Exception e) {
            loginData = new JSONArray();
        }
        try {
            jsonToState();
            if (foundOldToken) save();
        }
        catch (Exception e) {
            App.error(e);
        }
        close();
    }

    public static void save() {
        try {
            stateToJson();
            write();
        }
        catch (Exception e) {
            App.error(e);
        }
        close();
    }

    /**
     * Load settings from JSON array ("loginData" static field) to state.
     * Applies default values to settings whose values are not found in the save data JSON array.
     */
    private static void jsonToState() throws Exception {
        index = 0;

        boolean isHighRam = false;
//#ifdef J2ME_LOADER
        isHighRam = true;
//#endif
//#ifdef BLACKBERRY
        isHighRam = true;
//#endif
//#ifdef MIDP2_GENERIC
        isHighRam = Util.isSymbian || Util.isKemulator;
//#endif

        final int defaultFontSize =
//#ifdef NOKIA_128PX
            1;
//#else
            0;
//#endif

        api = getStringRecord(api);
        token = getStringRecord(token);
        theme = getIntRecord(0);
        gatewayUrl = getStringRecord(gatewayUrl);
        authorFontSize = getIntRecord(defaultFontSize);
        messageFontSize = getIntRecord(defaultFontSize);
        use12hTime = getBoolRecord(false);
        messageLoadCount = getIntRecord(20);
        useGateway = getBoolRecord(true);
//#ifdef BLACKBERRY
        bbWifi =
//#endif
        getBoolRecord(true);
        useJpeg = getBoolRecord(true);
        cdn = getStringRecord(cdn);
        pfpType = getIntRecord(Settings.PFP_TYPE_CIRCLE_HQ);
        attachmentSize = getIntRecord(1000);
        pfpSize = getIntRecord(Settings.ICON_SIZE_16);
        nativeFilePicker = getBoolRecord(false);
        autoReConnect = getBoolRecord(true);
        showMenuIcons = getBoolRecord(true);
        tokenType = getIntRecord(Settings.TOKEN_TYPE_HEADER);
        useNameColors = getBoolRecord(true);
        sendHotkey = getIntRecord(0);
        replyHotkey = getIntRecord(0);
        copyHotkey = getIntRecord(0);
        refreshHotkey = getIntRecord(0);
        backHotkey = getIntRecord(0);
        showRefMessage = getBoolRecord(true);
        defaultHotkeys = getBoolRecord(true);
        menuIconSize = getIntRecord(getBestMenuIconSize());
        language = getStringRecord(System.getProperty("microedition.locale"));
        fullscreenDefault = getBoolRecord(false);
        fullscreenHotkey = getIntRecord(0);
        showNotifsAll = getBoolRecord(false);
        showNotifsPings = getBoolRecord(true);
        showNotifsDMs = getBoolRecord(true);
        highRamMode = getBoolRecord(isHighRam);
        showNotifAlert = getBoolRecord(true);
        playNotifSound = getBoolRecord(true);
//#ifdef PIGLER_SUPPORT
        showNotifPigler =
//#endif
        getBoolRecord(
//#ifdef PIGLER_SUPPORT
            Util.supportsPigler
//#else
            false
//#endif
        );
        KineticScrollingCanvas.scrollBarMode = getIntRecord(
//#ifdef MIDP2_GENERIC
            Util.isKemulator ?
                KineticScrollingCanvas.SCROLL_BAR_VISIBLE :
//#endif
            KineticScrollingCanvas.SCROLL_BAR_HIDDEN
        );
        autoUpdate = getIntRecord(Settings.AUTO_UPDATE_RELEASE_ONLY);
//#ifdef NOKIA_UI_SUPPORT
        showNotifNokiaUI =
//#endif
        getBoolRecord(
//#ifdef NOKIA_UI_SUPPORT
            Util.supportsNokiaUINotifs
//#else
            false
//#endif
        );
//#ifdef OVER_100KB
        useFilePreview =
//#endif
        getBoolRecord(isHighRam);
//#ifdef EMOJI_SUPPORT
        FormattedString.emojiMode = getIntRecord(shouldShowEmoji() ? FormattedString.EMOJI_MODE_ALL : 0);
//#else
        getIntRecord(0);
//#endif
//#ifdef OVER_100KB
        FormattedString.useMarkdown =
//#endif
        getBoolRecord(true);
        playNotifVibra = getBoolRecord(false);
//#ifdef OVER_100KB
        scrollTopHotkey =
//#endif
        getIntRecord(0);
//#ifdef OVER_100KB
        scrollBottomHotkey =
//#endif
        getIntRecord(0);
//#ifdef OVER_100KB
        sendTyping =
//#endif
        getBoolRecord(true);
        KeyRepeatThread.toggle(getBoolRecord(false));
//#ifdef OVER_100KB
        hasSeenUploadWarning =
//#endif
        getBoolRecord(false);
//#ifdef PROXYLESS_SUPPORT
        proxyless =
//#endif
        getBoolRecord(false);

        // Check that message load count is in the Discord API allowed range (default = 20)
        if (messageLoadCount < 1 || messageLoadCount > 100) messageLoadCount = 20;

        Locale.setLanguage();
    }

    /**
     * Serialize state to JSON array for saving in RMS. Result goes in "loginData" static field.
     */
    private static void stateToJson() {
        loginData = new JSONArray();
        index = 0;

        setStringRecord(api);
        setStringRecord(token);
        setIntRecord(theme);
        setStringRecord(gatewayUrl);
        setIntRecord(authorFontSize);
        setIntRecord(messageFontSize);
        setBoolRecord(use12hTime);
        setIntRecord(messageLoadCount);
        setBoolRecord(useGateway);
        setBoolRecord(
//#ifdef BLACKBERRY
            bbWifi
//#else
            false
//#endif
        );
        setBoolRecord(useJpeg);
        setStringRecord(cdn);
        setIntRecord(pfpType);
        setIntRecord(attachmentSize);
        setIntRecord(pfpSize);
        setBoolRecord(nativeFilePicker);
        setBoolRecord(autoReConnect);
        setBoolRecord(showMenuIcons);
        setIntRecord(tokenType);
        setBoolRecord(useNameColors);
        setIntRecord(sendHotkey);
        setIntRecord(replyHotkey);
        setIntRecord(copyHotkey);
        setIntRecord(refreshHotkey);
        setIntRecord(backHotkey);
        setBoolRecord(showRefMessage);
        setBoolRecord(defaultHotkeys);
        setIntRecord(menuIconSize);
        setStringRecord(language);
        setBoolRecord(fullscreenDefault);
        setIntRecord(fullscreenHotkey);
        setBoolRecord(showNotifsAll);
        setBoolRecord(showNotifsPings);
        setBoolRecord(showNotifsDMs);
        setBoolRecord(highRamMode);
        setBoolRecord(showNotifAlert);
        setBoolRecord(playNotifSound);
        setBoolRecord(
//#ifdef PIGLER_SUPPORT
            showNotifPigler
//#else
            false
//#endif
        );
        setIntRecord(KineticScrollingCanvas.scrollBarMode);
        setIntRecord(autoUpdate);
        setBoolRecord(
//#ifdef NOKIA_UI_SUPPORT
            showNotifNokiaUI
//#else
            false
//#endif
        );
        setBoolRecord(
//#ifdef OVER_100KB
            useFilePreview
//#else
            false
//#endif
        );
        setIntRecord(
//#ifdef EMOJI_SUPPORT
            FormattedString.emojiMode
//#else
            0
//#endif
        );
        setBoolRecord(
//#ifdef OVER_100KB
            FormattedString.useMarkdown
//#else
            false
//#endif
        );
        setBoolRecord(playNotifVibra);
        setIntRecord(
//#ifdef OVER_100KB
            scrollTopHotkey
//#else
            0
//#endif
        );
        setIntRecord(
//#ifdef OVER_100KB
            scrollBottomHotkey
//#else
            0
//#endif
        );
        setBoolRecord(
//#ifdef OVER_100KB
            sendTyping
//#else
            false
//#endif
        );
        setBoolRecord(KeyRepeatThread.enabled);
        setBoolRecord(
//#ifdef OVER_100KB
            hasSeenUploadWarning
//#else
            false
//#endif
        );
        setBoolRecord(
//#ifdef PROXYLESS_SUPPORT
            proxyless
//#else
            false
//#endif
        );
    }

    private static void write() throws Exception {
        if (loginRms == null) {
            loginRms = RecordStore.openRecordStore("settings", true);
        }
        byte[] data = Util.stringToBytes(loginData.build());

        if (loginRms.getNumRecords() == 0) {
            loginRms.addRecord(data, 0, data.length);
        } else {
            loginRms.setRecord(1, data, 0, data.length);
        }
    }

    private static void close() {
        loginData = null;
        Util.closeRecordStore(loginRms);
        loginRms = null;
    }

    private static String getStringRecord(String def) throws Exception {
        String result = loginData.getString(index++, null);
        if (result == null || result.length() == 0) return def;
        return result;
    }
    
    private static int getIntRecord(int def) {
        return loginData.getInt(index++, def);
    }

    private static boolean getBoolRecord(boolean def) {
        return getIntRecord(def ? 1 : 0) != 0;
    }

    private static void setStringRecord(String value) {
        if (loginData.size() > index) {
            loginData.set(index, value);
        } else {
            loginData.add(value);
        }
        index++;
    }

    private static void setIntRecord(int value) {
        if (loginData.size() > index) {
            loginData.set(index, value);
        } else {
            loginData.add(value);
        }
        index++;
    }

    private static void setBoolRecord(boolean value) {
        setIntRecord(value ? 1 : 0);
    }
}