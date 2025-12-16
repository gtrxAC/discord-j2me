package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;

public class Settings {
    // How to add a new setting:
    // - create new field here
    // - add load/save commands to jsonToState/stateToJson methods (make sure they are the LAST get*Record() and set*Record() method uses in jsonToState/stateToJson, the order of loading/saving must be preserved)
    // - add an option to change that setting, see SettingsSectionScreen

    static final int PFP_TYPE_NONE = 0;
    static final int PFP_TYPE_SQUARE = 1;
    static final int PFP_TYPE_CIRCLE = 2;
    static final int PFP_TYPE_CIRCLE_HQ = 3;

    static final int TOKEN_TYPE_HEADER = 0;
    static final int TOKEN_TYPE_QUERY = 1;

    static final int PFP_SIZE_PLACEHOLDER = 0;
    static final int ICON_SIZE_OFF = 0;
    static final int ICON_SIZE_16 = 1;
    static final int ICON_SIZE_32 = 2;

    static final int AUTO_UPDATE_OFF = 0;
    static final int AUTO_UPDATE_RELEASE_ONLY = 1;
    static final int AUTO_UPDATE_ALL = 2;

    static final int MESSAGE_BAR_OFF = 0;
    static final int MESSAGE_BAR_AUTO = 1;
    static final int MESSAGE_BAR_ON = 2;

    static final int SOUND_OFF = 0;
    static final int SOUND_BEEP = 1;
    static final int SOUND_DEFAULT = 2;
    static final int SOUND_CUSTOM = 3;

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
    // static boolean playNotifSound;
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
//#ifdef PROXYLESS_SUPPORT
    static boolean hasFetchedProxylessEmojis;
    static boolean hasSeenEditError;
//#endif
    static int timeOffset;
//#ifdef PROXYLESS_SUPPORT
    static boolean hasSeenGatewayWarning;
//#endif
//#ifdef TOUCH_SUPPORT
    static int messageBarMode;
//#endif
//#ifdef J2ME_LOADER
    static boolean useModcon;
//#endif
    static int soundModes[] = new int[3];
    static boolean useBackgroundImage;

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

//#ifdef PROXYLESS_DEFAULT
        useGateway = false;
        proxyless = true;
//#else
        useGateway = true;
//#ifdef PROXYLESS_SUPPORT
        proxyless = false;
//#endif
//#endif

        // Check if token is supplied in JAD or manifest. If so, use that as the default.
        String manifestValue = App.instance.getAppProperty("Token");
        if (manifestValue != null) token = manifestValue;

        // Same for other connection settings.
        manifestValue = App.instance.getAppProperty("API-URL");
        if (manifestValue != null) api = manifestValue;
        manifestValue = App.instance.getAppProperty("Gateway-URL");
        if (manifestValue != null) gatewayUrl = manifestValue;
        manifestValue = App.instance.getAppProperty("CDN-URL");
        if (manifestValue != null) cdn = manifestValue;
        manifestValue = App.instance.getAppProperty("Use-gateway");
        if (manifestValue != null) useGateway = manifestValue.startsWith("Y") || manifestValue.startsWith("1");
//#ifdef PROXYLESS_SUPPORT
        manifestValue = App.instance.getAppProperty("Direct-connection");
        if (manifestValue != null) proxyless = manifestValue.startsWith("Y") || manifestValue.startsWith("1");
//#endif

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
//#ifdef SYMBIAN
        isHighRam = true;
//#endif
//#ifdef KEMULATOR
        isHighRam = true;
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

        useGateway = getBoolRecord(useGateway);
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
        showMenuIcons = getBoolRecord(!Util.isS40);  // server/DM icons dont load well on S40, disable by default
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
        // playNotifSound = getBoolRecord(true);
        getBoolRecord(true);
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
//#ifdef KEMULATOR
            KineticScrollingCanvas.SCROLL_BAR_VISIBLE
//#else
            KineticScrollingCanvas.SCROLL_BAR_HIDDEN
//#endif
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
        proxyless = getBoolRecord(proxyless);
//#else
        getBoolRecord(false);
//#endif
//#ifdef PROXYLESS_SUPPORT
        hasFetchedProxylessEmojis =
//#endif
        getBoolRecord(false);
//#ifdef PROXYLESS_SUPPORT
        hasSeenEditError =
//#endif
        getBoolRecord(false);
        timeOffset = getIntRecord(0);
//#ifdef PROXYLESS_SUPPORT
        hasSeenGatewayWarning =
//#endif
        getBoolRecord(false);
//#ifdef TOUCH_SUPPORT
        messageBarMode =
//#endif
        getIntRecord(
//#ifdef BLACKBERRY
            MESSAGE_BAR_ON
//#else
            MESSAGE_BAR_AUTO
//#endif
        );
//#ifdef J2ME_LOADER
        useModcon =
//#endif
        getBoolRecord(false);
        soundModes[SoundSettingsScreen.NOTIFICATION_SOUND] = getIntRecord(SOUND_DEFAULT);
        soundModes[SoundSettingsScreen.INCOMING_SOUND] = getIntRecord(SOUND_OFF);
        soundModes[SoundSettingsScreen.OUTGOING_SOUND] = getIntRecord(SOUND_OFF);
        useBackgroundImage = getBoolRecord(false);

        // Check that message load count is in the Discord API allowed range (default = 20)
        if (messageLoadCount < 1 || messageLoadCount > 100) messageLoadCount = 20;

        // discontinued "send token as JSON" option, use query parameter instead
        if (tokenType == 2) tokenType = TOKEN_TYPE_QUERY;

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
        setBoolRecord(true);//playNotifSound);
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
        setBoolRecord(
//#ifdef PROXYLESS_SUPPORT
            hasFetchedProxylessEmojis
//#else
            false
//#endif
        );
        setBoolRecord(
//#ifdef PROXYLESS_SUPPORT
            hasSeenEditError
//#else
            false
//#endif
        );
        setIntRecord(timeOffset);
        setBoolRecord(
//#ifdef PROXYLESS_SUPPORT
            hasSeenGatewayWarning
//#else
            false
//#endif
        );
        setIntRecord(
//#ifdef TOUCH_SUPPORT
            messageBarMode
//#else
            0
//#endif
        );
        setBoolRecord(
//#ifdef J2ME_LOADER
            useModcon
//#else
            false
//#endif
        );
        setIntRecord(soundModes[SoundSettingsScreen.NOTIFICATION_SOUND]);
        setIntRecord(soundModes[SoundSettingsScreen.INCOMING_SOUND]);
        setIntRecord(soundModes[SoundSettingsScreen.OUTGOING_SOUND]);
        setBoolRecord(useBackgroundImage);
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