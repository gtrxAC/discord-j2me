package com.gtrxac.discord;

import javax.microedition.rms.*;

public class LoginSettings {
    static RecordStore loginRms;
    static int numRecords;
    static int index;

    private static void open() throws Exception {
        loginRms = RecordStore.openRecordStore("login", true);
        index = 1;
    }

    public static void load(State s) {
        s.api = "http://146.59.80.3";
        s.gatewayUrl = "socket://146.59.80.3:8081";
        s.cdn = "http://146.59.80.3:8080";
        s.token = "";
        
        String manifestToken = s.midlet.getAppProperty("Token");
        if (manifestToken != null) s.token = manifestToken;

        if (RecordStore.listRecordStores() != null) {
            try {
                open();
                numRecords = loginRms.getNumRecords();

                s.api = getStringRecord(s.api);
                s.token = getStringRecord(s.token);
                s.theme = getByteRecord(0);
                index++;  // skip removed option (old UI)
                s.gatewayUrl = getStringRecord(s.gatewayUrl);
                s.authorFontSize = getByteRecord(0);
                s.messageFontSize = getByteRecord(0);
                s.use12hTime = getBoolRecord(false);
                s.messageLoadCount = getByteRecord(20);
                s.useGateway = getBoolRecord(true);
                s.bbWifi = getBoolRecord(true);
                s.useJpeg = getBoolRecord(true);
                s.cdn = getStringRecord(s.cdn);
                s.pfpType = getByteRecord(State.PFP_TYPE_CIRCLE_HQ);
                s.attachmentSize = getIntRecord(1000);
                s.pfpSize = getByteRecord(State.ICON_SIZE_16);
                s.nativeFilePicker = getBoolRecord(false);
                s.autoReConnect = getBoolRecord(true);
                s.showMenuIcons = getBoolRecord(true);
                s.tokenType = getByteRecord(State.TOKEN_TYPE_HEADER);
                s.useNameColors = getBoolRecord(true);
                s.sendHotkey = getIntRecord(0);
                s.replyHotkey = getIntRecord(0);
                s.copyHotkey = getIntRecord(0);
                s.refreshHotkey = getIntRecord(0);
                s.backHotkey = getIntRecord(0);
                s.showRefMessage = getBoolRecord(true);
                s.defaultHotkeys = getBoolRecord(true);
                s.menuIconSize = getByteRecord(16);
                s.language = getStringRecord(System.getProperty("microedition.locale"));
                s.fullscreenDefault = getBoolRecord(false);
                s.fullscreenHotkey = getIntRecord(0);
                s.showNotifsAll = getBoolRecord(false);
                s.showNotifsPings = getBoolRecord(true);
                s.showNotifsDMs = getBoolRecord(true);

                if (s.menuIconSize == 1) s.menuIconSize = 16;
                else if (s.menuIconSize == 2) s.menuIconSize = 32;

                if (s.messageLoadCount < 1 || s.messageLoadCount > 100) s.messageLoadCount = 20;

                Locale.setLanguage(s);
            }
            catch (Exception e) {
                s.error(e);
            }
            finally {
                try {
                    if (loginRms != null) loginRms.closeRecordStore();
                }
                catch (Exception e) {}
            }
        }
        else s.messageLoadCount = 20;
    }

    public static void save(State s) {
        try {
            open();
            setStringRecord(s.api);
            setStringRecord(s.token);
            setByteRecord(s.theme);
            setBoolRecord(false);  // skip removed option (old UI)
            setStringRecord(s.gatewayUrl);
            setByteRecord(s.authorFontSize);
            setByteRecord(s.messageFontSize);
            setBoolRecord(s.use12hTime);
            setByteRecord(s.messageLoadCount);
            setBoolRecord(s.useGateway);
            setBoolRecord(s.bbWifi);
            setBoolRecord(s.useJpeg);
            setStringRecord(s.cdn);
            setByteRecord(s.pfpType);
            setIntRecord(s.attachmentSize);
            setByteRecord(s.pfpSize);
            setBoolRecord(s.nativeFilePicker);
            setBoolRecord(s.autoReConnect);
            setBoolRecord(s.showMenuIcons);
            setByteRecord(s.tokenType);
            setBoolRecord(s.useNameColors);
            setIntRecord(s.sendHotkey);
            setIntRecord(s.replyHotkey);
            setIntRecord(s.copyHotkey);
            setIntRecord(s.refreshHotkey);
            setIntRecord(s.backHotkey);
            setBoolRecord(s.showRefMessage);
            setBoolRecord(s.defaultHotkeys);
            setByteRecord(s.menuIconSize);
            setStringRecord(s.language);
            setBoolRecord(s.fullscreenDefault);
            setIntRecord(s.fullscreenHotkey);
            setBoolRecord(s.showNotifsAll);
            setBoolRecord(s.showNotifsPings);
            setBoolRecord(s.showNotifsDMs);
            loginRms.closeRecordStore();
        }
        catch (Exception e) {
            s.error(e);
        }
    }

    private static String getStringRecord(String def) throws Exception {
        int thisIndex = index;
        index++;
        if (numRecords >= thisIndex) {
            try {
                String result = new String(loginRms.getRecord(thisIndex));
                if (result.length() > 0) return result;
            }
            catch (Exception e) {}
        }
        return def;
    }
    
    private static int getIntRecord(int def) {
        int thisIndex = index;
        index++;
        if (numRecords >= thisIndex) {
            try {
                return Integer.parseInt(new String(loginRms.getRecord(thisIndex)));
            }
            catch (Exception e) {
                return def;
            }
        }
        return def;
    }

    private static int getByteRecord(int def) throws Exception {
        int thisIndex = index;
        index++;
        if (numRecords >= thisIndex) {
            return (int) loginRms.getRecord(thisIndex)[0];
        }
        return def;
    }

    private static boolean getBoolRecord(boolean def) throws Exception {
        int thisIndex = index;
        index++;
        if (numRecords >= thisIndex) {
            return loginRms.getRecord(thisIndex)[0] != 0;
        }
        return def;
    }
    
    private static void setRecord(byte[] value) throws Exception {
        if (loginRms.getNumRecords() >= index) {
            loginRms.setRecord(index, value, 0, value.length);
        } else {
            loginRms.addRecord(value, 0, value.length);
        }
        index++;
    }

    private static void setByteRecord(int value) throws Exception {
        byte[] record = {new Integer(value).byteValue()};
        setRecord(record);
    }

    private static void setBoolRecord(boolean value) throws Exception {
        setByteRecord(value ? 1 : 0);
    }

    private static void setStringRecord(String value) throws Exception {
        setRecord(value.getBytes());
    }

    private static void setIntRecord(int value) throws Exception {
        setStringRecord(Integer.toString(value));
    }
}