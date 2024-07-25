package com.gtrxac.discord;

import javax.microedition.rms.*;

public class LoginSettings {
    static RecordStore loginRms;
    static int numRecords;

    public static void load(State s) {
        s.api = "http://146.59.80.3";
        s.gatewayUrl = "socket://146.59.80.3:8081";
        s.cdn = "http://146.59.80.3:8080";
        s.token = "";
        
        String manifestToken = s.midlet.getAppProperty("Token");
        if (manifestToken != null) s.token = manifestToken;

        if (RecordStore.listRecordStores() != null) {
            try {
                loginRms = RecordStore.openRecordStore("login", true);
                numRecords = loginRms.getNumRecords();

                s.api = getStringRecord(1, s.api);
                s.token = getStringRecord(2, s.token);
                s.theme = getByteRecord(3, 0);
                s.oldUI = getBoolRecord(4, false);
                s.gatewayUrl = getStringRecord(5, s.gatewayUrl);
                s.authorFontSize = getByteRecord(6, 0);
                s.messageFontSize = getByteRecord(7, 0);
                s.use12hTime = getBoolRecord(8, false);
                s.messageLoadCount = getByteRecord(9, 20);
                s.useGateway = getBoolRecord(10, true);
                s.bbWifi = getBoolRecord(11, true);
                s.useJpeg = getBoolRecord(12, true);
                s.cdn = getStringRecord(13, s.cdn);
                s.iconType = getByteRecord(14, State.ICON_TYPE_CIRCLE);
                s.attachmentSize = getIntRecord(15, 1000);
                s.iconSize = getByteRecord(16, 1); // default 16px
                s.nativeFilePicker = getBoolRecord(17, false);
                s.autoReConnect = getBoolRecord(18, false);
                s.showMenuIcons = getBoolRecord(19, true);
                s.tokenType = getByteRecord(20, State.TOKEN_TYPE_HEADER);
                s.useNameColors = getBoolRecord(21, true);
                s.sendHotkey = getIntRecord(22, 0);
                s.replyHotkey = getIntRecord(23, 0);
                s.copyHotkey = getIntRecord(24, 0);
                s.refreshHotkey = getIntRecord(25, 0);
                s.backHotkey = getIntRecord(26, 0);
                s.showRefMessage = getBoolRecord(27, true);
                s.defaultHotkeys = getBoolRecord(28, true);

                if (s.messageLoadCount < 1 || s.messageLoadCount > 100) s.messageLoadCount = 20;
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
            loginRms = RecordStore.openRecordStore("login", true);
            setStringRecord(1, s.api);
            setStringRecord(2, s.token);
            setByteRecord(3, s.theme);
            setBoolRecord(4, s.oldUI);
            setStringRecord(5, s.gatewayUrl);
            setByteRecord(6, s.authorFontSize);
            setByteRecord(7, s.messageFontSize);
            setBoolRecord(8, s.use12hTime);
            setByteRecord(9, s.messageLoadCount);
            setBoolRecord(10, s.useGateway);
            setBoolRecord(11, s.bbWifi);
            setBoolRecord(12, s.useJpeg);
            setStringRecord(13, s.cdn);
            setByteRecord(14, s.iconType);
            setIntRecord(15, s.attachmentSize);
            setByteRecord(16, s.iconSize);
            setBoolRecord(17, s.nativeFilePicker);
            setBoolRecord(18, s.autoReConnect);
            setBoolRecord(19, s.showMenuIcons);
            setByteRecord(20, s.tokenType);
            setBoolRecord(21, s.useNameColors);
            setIntRecord(22, s.sendHotkey);
            setIntRecord(23, s.replyHotkey);
            setIntRecord(24, s.copyHotkey);
            setIntRecord(25, s.refreshHotkey);
            setIntRecord(26, s.backHotkey);
            setBoolRecord(27, s.showRefMessage);
            setBoolRecord(28, s.defaultHotkeys);
            loginRms.closeRecordStore();
        }
        catch (Exception e) {
            s.error(e);
        }
    }

    private static String getStringRecord(int index, String def) throws Exception {
        if (numRecords >= index) {
            try {
                String result = new String(loginRms.getRecord(index));
                if (result.length() > 0) return result;
            }
            catch (Exception e) {}
        }
        return def;
    }
    
    private static int getIntRecord(int index, int def) {
        if (numRecords >= index) {
            try {
                return Integer.parseInt(new String(loginRms.getRecord(index)));
            }
            catch (Exception e) {
                return def;
            }
        }
        return def;
    }

    private static int getByteRecord(int index, int def) throws Exception {
        if (numRecords >= index) {
            return (int) loginRms.getRecord(index)[0];
        }
        return def;
    }

    private static boolean getBoolRecord(int index, boolean def) throws Exception {
        if (numRecords >= index) {
            return loginRms.getRecord(index)[0] != 0;
        }
        return def;
    }
    
    private static void setRecord(int index, byte[] value) throws Exception {
        if (loginRms.getNumRecords() >= index) {
            loginRms.setRecord(index, value, 0, value.length);
        } else {
            loginRms.addRecord(value, 0, value.length);
        }
    }

    private static void setByteRecord(int index, int value) throws Exception {
        byte[] record = {new Integer(value).byteValue()};
        setRecord(index, record);
    }

    private static void setBoolRecord(int index, boolean value) throws Exception {
        setByteRecord(index, value ? 1 : 0);
    }

    private static void setStringRecord(int index, String value) throws Exception {
        setRecord(index, value.getBytes());
    }

    private static void setIntRecord(int index, int value) throws Exception {
        setStringRecord(index, Integer.toString(value));
    }
}