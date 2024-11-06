package com.gtrxac.discord;

import javax.microedition.rms.*;

public class LoginSettings {
    private static RecordStore loginRms;
    private static int numRecords;
    private static int index;

    private static int getDefaultTokenType() {
		String profile = System.getProperty("microedition.profiles");
		String plat = System.getProperty("microedition.platform");

		// Nokia pre-S40v2 doesn't support HTTP header authentication
		if (profile != null && profile.startsWith("MIDP-1.0") && plat != null && plat.startsWith("Nokia")) {
			return State.TOKEN_TYPE_QUERY;
		} else {
			return State.TOKEN_TYPE_HEADER;
		}
    }

    private static void open() throws Exception {
        loginRms = RecordStore.openRecordStore("a", true);
        index = 1;
    }

    public static void load(State s) {
        s.token = "";
        String manifestToken = s.midlet.getAppProperty("Token");
        if (manifestToken != null) s.token = manifestToken;

        try {
            open();
            numRecords = loginRms.getNumRecords();

            s.api = getStringRecord("http://146.59.80.3");
            s.token = getStringRecord(s.token);
            s.authorFontSize = getByteRecord(0);
            s.messageFontSize = getByteRecord(0);
            s.use12hTime = getByteRecord(0) != 0;
            s.messageLoadCount = getByteRecord(15);
            s.tokenType = getByteRecord(getDefaultTokenType());
            // dark theme default for color screens, dedicated monochrome theme default for mono screens
            s.theme = getByteRecord(s.disp.isColor() ? 1 : 0);

            if (s.messageLoadCount < 1 || s.messageLoadCount > 100) s.messageLoadCount = 15;
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

    public static void save(State s) {
        try {
            open();
            setStringRecord(s.api);
            setStringRecord(s.token);
            setByteRecord(s.authorFontSize);
            setByteRecord(s.messageFontSize);
            setByteRecord(s.use12hTime ? 1 : 0);
            setByteRecord(s.messageLoadCount);
            setByteRecord(s.tokenType);
            setByteRecord(s.theme);
            loginRms.closeRecordStore();
        }
        catch (Exception e) {
            s.error(e);
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