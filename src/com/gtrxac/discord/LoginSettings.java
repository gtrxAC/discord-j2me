package com.gtrxac.discord;

import javax.microedition.rms.*;

public class LoginSettings {
    static RecordStore loginRms;
    static int numRecords;
    
    String api;
    String gateway;
    String cdn;
    String token;

    LoginSettings(State s) {
        api = "http://146.59.80.3";
        gateway = "socket://146.59.80.3:8081";
        cdn = "http://146.59.80.3:8080";
        token = "";
        
        String manifestToken = s.midlet.getAppProperty("Token");
        if (manifestToken != null) token = manifestToken;

        if (RecordStore.listRecordStores() != null) {
            try {
                loginRms = RecordStore.openRecordStore("login", true);
                numRecords = loginRms.getNumRecords();

                if (numRecords > 0) {
                    String savedApi = new String(loginRms.getRecord(1));
                    if (savedApi.length() > 0) api = savedApi;
                    String savedToken = new String(loginRms.getRecord(2));
                    if (savedToken.length() > 0) token = savedToken;
                }

                s.theme = getByteRecord(3, 0);
                s.oldUI = getBoolRecord(4, false);
                
                if (numRecords >= 5) {
                    String savedGateway = new String(loginRms.getRecord(5));
                    if (savedGateway.length() > 0) gateway = savedGateway;
                }

                s.authorFontSize = getByteRecord(6, 0);
                s.messageFontSize = getByteRecord(7, 0);
                s.use12hTime = getBoolRecord(8, false);

                if (numRecords >= 9) {
                    s.messageLoadCount = loginRms.getRecord(9)[0];
                    if (s.messageLoadCount < 1 || s.messageLoadCount > 100) s.messageLoadCount = 20;
                } else {
                    s.messageLoadCount = 20;
                }

                s.useGateway = getBoolRecord(10, true);
                s.bbWifi = getBoolRecord(11, true);
                s.useJpeg = getBoolRecord(12, true);

                if (numRecords >= 13) {
                    String savedCdn = new String(loginRms.getRecord(13));
                    if (savedCdn.length() > 0) cdn = savedCdn;
                }

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
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (loginRms != null) loginRms.closeRecordStore();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else s.messageLoadCount = 20;
    }
    
    private int getIntRecord(int index, int def) throws Exception {
        if (numRecords >= index) {
            try {
                return Integer.parseInt(new String(loginRms.getRecord(index)));
            }
            catch (Exception e) {
                return def;
            }
        } else return def;
    }

    private int getByteRecord(int index, int def) throws Exception {
        if (numRecords >= index) {
            return (int) loginRms.getRecord(index)[0];
        } else {
            return def;
        }
    }

    private boolean getBoolRecord(int index, boolean def) throws Exception {
        if (numRecords >= index) {
            return loginRms.getRecord(index)[0] != 0;
        } else {
            return def;
        }
    }
}