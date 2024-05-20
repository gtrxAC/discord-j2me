package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class LoginForm extends Form implements CommandListener {
    State s;
    private RecordStore loginRms;

    private TextField apiField;
    private ChoiceGroup wifiGroup;
    private TextField tokenField;
    private Command nextCommand;

    public LoginForm(State s) {
        super("Log in");
        setCommandListener(this); 
        this.s = s;

        String initialApi = "http://dscjava.uwmpr.online";
        String initialToken = "";
        
        if (RecordStore.listRecordStores() != null) {
            try {
                loginRms = RecordStore.openRecordStore("login", true);

                if (loginRms.getNumRecords() > 0) {
                    String savedApi = new String(loginRms.getRecord(1));
                    if (savedApi.length() > 0) initialApi = savedApi;
                    initialToken = new String(loginRms.getRecord(2));
                }
                if (loginRms.getNumRecords() >= 3) {
                    s.theme = loginRms.getRecord(3)[0];
                }
                if (loginRms.getNumRecords() >= 4) {
                    s.oldUI = loginRms.getRecord(4)[0] != 0;
                }
                if (loginRms.getNumRecords() >= 7) {
                    s.authorFontSize = loginRms.getRecord(6)[0];
                    s.messageFontSize = loginRms.getRecord(7)[0];
                }
                if (loginRms.getNumRecords() >= 8) {
                    s.use12hTime = loginRms.getRecord(8)[0] != 0;
                }
                if (loginRms.getNumRecords() >= 9) {
                    s.messageLoadCount = loginRms.getRecord(9)[0];
                    if (s.messageLoadCount < 1 || s.messageLoadCount > 100) s.messageLoadCount = 20;
                } else {
                    s.messageLoadCount = 20;
                }
                if (loginRms.getNumRecords() >= 11) {
                    s.bbWifi = loginRms.getRecord(11)[0] != 0;
                } else {
                    s.bbWifi = true;
                }
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

        if (State.isBlackBerry()) {
            String[] wifiChoices = {"Use Wi-Fi"};
            Image[] wifiImages = {null};
            wifiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, wifiChoices, wifiImages);
            wifiGroup.setSelectedIndex(0, s.bbWifi);
            append(wifiGroup);
        }

        apiField = new TextField("API URL", initialApi, 200, 0);
        tokenField = new TextField("Token", initialToken, 200, 0);
        nextCommand = new Command("Log in", Command.OK, 0);

        append(new StringItem(null, "Only use proxies that you trust!"));
        append(apiField);
        append(new StringItem(null, "The token can be found from your browser's dev tools (look online for help). Using an alt account is recommended."));
        append(tokenField);
        addCommand(nextCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            String api = apiField.getString();
            String gateway = "-";
            String token = tokenField.getString();

            boolean[] selected = {false};
            byte[] useGatewayRecord = {new Integer(0).byteValue()};

            if (State.isBlackBerry()) {
                wifiGroup.getSelectedFlags(selected);
                s.bbWifi = selected[0];
            }
            byte[] bbWifiRecord = {new Integer(s.bbWifi ? 1 : 0).byteValue()};
            
            try {
                loginRms = RecordStore.openRecordStore("login", true);
                if (loginRms.getNumRecords() > 0) {
                    loginRms.setRecord(1, api.getBytes(), 0, api.length());    
                    loginRms.setRecord(2, token.getBytes(), 0, token.length());    
                } else {
                    loginRms.addRecord(api.getBytes(), 0, api.length());
                    loginRms.addRecord(token.getBytes(), 0, token.length());
                }
                if (loginRms.getNumRecords() < 4) {
                    byte[] zeroByte = {0};
                    loginRms.addRecord(zeroByte, 0, 1);
                    loginRms.addRecord(zeroByte, 0, 1);
                }
                if (loginRms.getNumRecords() >= 5) {
                    loginRms.setRecord(5, gateway.getBytes(), 0, gateway.length());
                } else {
                    loginRms.addRecord(gateway.getBytes(), 0, gateway.length());
                }
                if (loginRms.getNumRecords() < 9) {
                    byte[] zeroByte = {0};
                    byte[] defaultMsgCount = {20};
                    loginRms.addRecord(zeroByte, 0, 1);
                    loginRms.addRecord(zeroByte, 0, 1);
                    loginRms.addRecord(zeroByte, 0, 1);
                    loginRms.addRecord(defaultMsgCount, 0, 1);
                }
                if (loginRms.getNumRecords() >= 10) {
                    loginRms.setRecord(10, useGatewayRecord, 0, 1);
                } else {
                    loginRms.addRecord(useGatewayRecord, 0, 1);
                }
                if (loginRms.getNumRecords() >= 11) {
                    loginRms.setRecord(11, bbWifiRecord, 0, 1);
                } else {
                    loginRms.addRecord(bbWifiRecord, 0, 1);
                }
                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            s.loadFonts();
            s.http = new HTTPThing(s, api, token);
            s.disp.setCurrent(new MainMenu(s));
        }
    }
}
