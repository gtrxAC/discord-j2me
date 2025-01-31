package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;

public class LoginSettings {
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

    public static void load(State s) {
        // Initial settings (will be used if there are no saved settings)
        s.api = "http://146.59.80.3";
        s.gatewayUrl = "socket://146.59.80.3:8081";
        s.cdn = "http://146.59.80.3:8080";
        s.token = "";

        // Check if token is supplied in JAD or manifest. If so, use that as the default.
        String manifestToken = s.midlet.getAppProperty("Token");
        if (manifestToken != null) s.token = manifestToken;

        // Check if save file in old format is available.
        // If so, load token from there and delete the old file.
        boolean foundOldToken = false;
        try {
            RecordStore oldRms = RecordStore.openRecordStore("login", false);
            String oldToken = Util.bytesToString(oldRms.getRecord(2)).trim();

            if (oldToken.length() > 0) {
                s.token = oldToken;
                foundOldToken = true;
            }
            Util.closeRecordStore(oldRms);
            // TODO: will later make it actually delete when the new save system is deemed to be stable enough
            // RecordStore.deleteRecordStore("login");
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
            jsonToState(s);
            if (foundOldToken) save(s);
        }
        catch (Exception e) {
            s.error(e);
        }
        close();
    }

    public static void save(State s) {
        try {
            stateToJson(s);
            write();
        }
        catch (Exception e) {
            s.error(e);
        }
        close();
    }

    /**
     * Load settings from JSON array ("loginData" static field) to state.
     * Applies default values to settings whose values are not found in the save data JSON array.
     * @param s Discord J2ME State object where to load the settings into.
     */
    private static void jsonToState(State s) throws Exception {
        index = 0;

        boolean isHighRam = false;
        // ifdef J2ME_LOADER
        isHighRam = true;
        // endif
        // ifdef MIDP2_GENERIC
        isHighRam = Util.isSymbian || Util.isKemulator;
        // endif

        int defaultFontSize = 0;
        // ifdef MIDP2_GENERIC
        if (Util.isNokia128x) defaultFontSize = 1;
        // endif

        s.api = getStringRecord(s.api);
        s.token = getStringRecord(s.token);
        s.theme = getIntRecord(0);
        s.gatewayUrl = getStringRecord(s.gatewayUrl);
        s.authorFontSize = getIntRecord(defaultFontSize);
        s.messageFontSize = getIntRecord(defaultFontSize);
        s.use12hTime = getBoolRecord(false);
        s.messageLoadCount = getIntRecord(20);
        s.useGateway = getBoolRecord(true);
        // ifdef BLACKBERRY
        s.bbWifi =
        // endif
        getBoolRecord(true);
        s.useJpeg = getBoolRecord(true);
        s.cdn = getStringRecord(s.cdn);
        s.pfpType = getIntRecord(State.PFP_TYPE_CIRCLE_HQ);
        s.attachmentSize = getIntRecord(1000);
        s.pfpSize = getIntRecord(State.ICON_SIZE_16);
        s.nativeFilePicker = getBoolRecord(false);
        s.autoReConnect = getBoolRecord(true);
        s.showMenuIcons = getBoolRecord(true);
        s.tokenType = getIntRecord(State.TOKEN_TYPE_HEADER);
        s.useNameColors = getBoolRecord(true);
        s.sendHotkey = getIntRecord(0);
        s.replyHotkey = getIntRecord(0);
        s.copyHotkey = getIntRecord(0);
        s.refreshHotkey = getIntRecord(0);
        s.backHotkey = getIntRecord(0);
        s.showRefMessage = getBoolRecord(true);
        s.defaultHotkeys = getBoolRecord(true);
        s.menuIconSize = getIntRecord(getBestMenuIconSize());
        s.language = getStringRecord(System.getProperty("microedition.locale"));
        s.fullscreenDefault = getBoolRecord(false);
        s.fullscreenHotkey = getIntRecord(0);
        s.showNotifsAll = getBoolRecord(false);
        s.showNotifsPings = getBoolRecord(true);
        s.showNotifsDMs = getBoolRecord(true);
        s.highRamMode = getBoolRecord(isHighRam);
        s.showNotifAlert = getBoolRecord(true);
        s.playNotifSound = getBoolRecord(true);
        // ifdef PIGLER_SUPPORT
        s.showNotifPigler =
        // endif
        getBoolRecord(
            // ifdef PIGLER_SUPPORT
            Util.supportsPigler
            // else
            false
            // endif
        );
        KineticScrollingCanvas.scrollBarMode = getIntRecord(
            // ifdef MIDP2_GENERIC
            Util.isKemulator ?
                KineticScrollingCanvas.SCROLL_BAR_VISIBLE :
            // endif
            KineticScrollingCanvas.SCROLL_BAR_HIDDEN
        );
        s.autoUpdate = getIntRecord(State.AUTO_UPDATE_RELEASE_ONLY);
        // ifdef NOKIA_UI_SUPPORT
        s.showNotifNokiaUI =
        // endif
        getBoolRecord(
            // ifdef NOKIA_UI_SUPPORT
            Util.supportsNokiaUINotifs
            // else
            false
            // endif
        );
        // ifdef OVER_100KB
        s.useFilePreview =
        // endif
        getBoolRecord(isHighRam);
        // ifdef OVER_100KB
        FormattedString.emojiMode =
        // endif
        getIntRecord(
            // ifdef OVER_100KB
            Util.fontSize > 14 ? FormattedString.EMOJI_MODE_ALL :
            // endif
            0
        );
        // ifdef OVER_100KB
        FormattedString.useMarkdown =
        // endif
        getBoolRecord(true);

        // Check that message load count is in the Discord API allowed range (default = 20)
        if (s.messageLoadCount < 1 || s.messageLoadCount > 100) s.messageLoadCount = 20;

        Locale.setLanguage(s);
    }

    /**
     * Serialize state to JSON array for saving in RMS. Result goes in "loginData" static field.
     */
    private static void stateToJson(State s) {
        loginData = new JSONArray();
        index = 0;

        setStringRecord(s.api);
        setStringRecord(s.token);
        setIntRecord(s.theme);
        setStringRecord(s.gatewayUrl);
        setIntRecord(s.authorFontSize);
        setIntRecord(s.messageFontSize);
        setBoolRecord(s.use12hTime);
        setIntRecord(s.messageLoadCount);
        setBoolRecord(s.useGateway);
        setBoolRecord(
            // ifdef BLACKBERRY
            s.bbWifi
            // else
            false
            // endif
        );
        setBoolRecord(s.useJpeg);
        setStringRecord(s.cdn);
        setIntRecord(s.pfpType);
        setIntRecord(s.attachmentSize);
        setIntRecord(s.pfpSize);
        setBoolRecord(s.nativeFilePicker);
        setBoolRecord(s.autoReConnect);
        setBoolRecord(s.showMenuIcons);
        setIntRecord(s.tokenType);
        setBoolRecord(s.useNameColors);
        setIntRecord(s.sendHotkey);
        setIntRecord(s.replyHotkey);
        setIntRecord(s.copyHotkey);
        setIntRecord(s.refreshHotkey);
        setIntRecord(s.backHotkey);
        setBoolRecord(s.showRefMessage);
        setBoolRecord(s.defaultHotkeys);
        setIntRecord(s.menuIconSize);
        setStringRecord(s.language);
        setBoolRecord(s.fullscreenDefault);
        setIntRecord(s.fullscreenHotkey);
        setBoolRecord(s.showNotifsAll);
        setBoolRecord(s.showNotifsPings);
        setBoolRecord(s.showNotifsDMs);
        setBoolRecord(s.highRamMode);
        setBoolRecord(s.showNotifAlert);
        setBoolRecord(s.playNotifSound);
        setBoolRecord(
            // ifdef PIGLER_SUPPORT
            s.showNotifPigler
            // else
            false
            // endif
        );
        setIntRecord(KineticScrollingCanvas.scrollBarMode);
        setIntRecord(s.autoUpdate);
        setBoolRecord(
            // ifdef NOKIA_UI_SUPPORT
            s.showNotifNokiaUI
            // else
            false
            // endif
        );
        setBoolRecord(
            // ifdef OVER_100KB
            s.useFilePreview
            // else
            false
            // endif
        );
        setIntRecord(
            // ifdef OVER_100KB
            FormattedString.emojiMode
            // else
            0
            // endif
        );
        setBoolRecord(
            // ifdef OVER_100KB
            FormattedString.useMarkdown
            // else
            false
            // endif
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