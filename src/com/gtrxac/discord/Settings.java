package com.gtrxac.discord;

import javax.microedition.rms.*;

public class Settings {
    private static RecordStore loginRms;
    private static int numRecords;
    private static int index;

    private static void open() throws Exception {
        loginRms = RecordStore.openRecordStore("a", true);
        index = 1;
    }

    public static boolean isAvailable() {
        try {
            RecordStore.openRecordStore("a", false).closeRecordStore();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void load() {
        App.token = "";
        String manifestToken = App.instance.getAppProperty("Token");
        if (manifestToken != null) App.token = manifestToken;

        try {
            open();
            numRecords = loginRms.getNumRecords();

            App.api = getStringRecord("http://146.59.80.3");
            App.token = getStringRecord(App.token);
            App.authorFontSize = getByteRecord(0);
            App.messageFontSize = getByteRecord(0);
            App.use12hTime = getByteRecord(0) != 0;
            App.messageLoadCount = getByteRecord(15);
            index++; // skip unused record
            // dark theme default for color screens, dedicated monochrome theme default for mono screens
            App.theme = getByteRecord(App.disp.isColor() ? 1 : 0);

            if (App.messageLoadCount < 1 || App.messageLoadCount > 100) App.messageLoadCount = 15;
        }
        catch (Exception e) {
            App.error(e);
        }
        finally {
            try {
                if (loginRms != null) loginRms.closeRecordStore();
            }
            catch (Exception e) {}
        }
    }

    public static void save() {
        try {
            open();
            setStringRecord(App.api);
            setStringRecord(App.token);
            setByteRecord(App.authorFontSize);
            setByteRecord(App.messageFontSize);
            setByteRecord(App.use12hTime ? 1 : 0);
            setByteRecord(App.messageLoadCount);
            setByteRecord(0);
            setByteRecord(App.theme);
            loginRms.closeRecordStore();
        }
        catch (Exception e) {
            App.error(e);
        }
    }

    private static String getStringRecord(String def) {
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

    private static int getByteRecord(int def) throws Exception {
        int thisIndex = index;
        index++;
        if (numRecords >= thisIndex) {
            return (int) loginRms.getRecord(thisIndex)[0];
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

    private static void setStringRecord(String value) throws Exception {
        setRecord(value.getBytes());
    }
}