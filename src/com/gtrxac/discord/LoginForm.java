package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class LoginForm extends Form implements CommandListener {
    State s;
    private RecordStore loginRms;

    private TextField apiField;
    private ChoiceGroup wifiGroup;
    private ChoiceGroup gatewayGroup;
    private TextField gatewayField;
    private TextField cdnField;
    private TextField tokenField;
    private ChoiceGroup tokenGroup;
    private Command nextCommand;
    private Command quitCommand;

    public LoginForm(State s) {
        super("Log in");
        setCommandListener(this); 
        this.s = s;

        LoginSettings settings = new LoginSettings(s);

        if (State.isBlackBerry()) {
            String[] wifiChoices = {"Use Wi-Fi"};
            Image[] wifiImages = {null};
            wifiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, wifiChoices, wifiImages);
            wifiGroup.setSelectedIndex(0, s.bbWifi);
            append(wifiGroup);
        }

        apiField = new TextField("API URL", settings.api, 200, 0);
        cdnField = new TextField("CDN URL", settings.cdn, 200, 0);
        gatewayField = new TextField("Gateway URL", settings.gateway, 200, 0);
        tokenField = new TextField("Token", settings.token, 200, TextField.NON_PREDICTIVE);
        nextCommand = new Command("Log in", Command.OK, 0);
        quitCommand = new Command("Quit", Command.EXIT, 1);

        String[] gatewayChoices = {"Use gateway"};
        Image[] gatewayImages = {null};
        gatewayGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, gatewayChoices, gatewayImages);
        gatewayGroup.setSelectedIndex(0, s.useGateway);

        String[] tokenChoices = {"Header (default)", "JSON", "Query parameter"};
        Image[] tokenImages = {null, null, null};
        tokenGroup = new ChoiceGroup("Send token as", ChoiceGroup.EXCLUSIVE, tokenChoices, tokenImages);
        tokenGroup.setSelectedIndex(s.tokenType, true);

        append(new StringItem(null, "Only use proxies that you trust!"));
        append(apiField);
        append(cdnField);
        append(gatewayGroup);
        append(gatewayField);
        append(new StringItem(null, "The token can be found from your browser's dev tools (look online for help). Using an alt account is recommended."));
        append(tokenField);
        append(tokenGroup);
        addCommand(nextCommand);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            String api = apiField.getString();
            String cdn = cdnField.getString();
            String gateway = gatewayField.getString();
            String token = tokenField.getString();

            if (token.length() == 0) {
                s.error("Please enter your token");
                return;
            }
            if (api.length() == 0) {
                s.error("Please specify an API URL");
                return;
            }

            boolean[] selected = {false};
            gatewayGroup.getSelectedFlags(selected);
            s.useGateway = selected[0];
            byte[] useGatewayRecord = {new Integer(s.useGateway ? 1 : 0).byteValue()};

            if (State.isBlackBerry()) {
                wifiGroup.getSelectedFlags(selected);
                s.bbWifi = selected[0];
            }
            byte[] bbWifiRecord = {new Integer(s.bbWifi ? 1 : 0).byteValue()};

            s.tokenType = tokenGroup.getSelectedIndex();
            byte[] tokenTypeRecord = {new Integer(s.tokenType).byteValue()};
            
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
                if (loginRms.getNumRecords() < 12) {
                    byte[] oneByte = {1};
                    loginRms.addRecord(oneByte, 0, 1);
                }
                if (loginRms.getNumRecords() >= 13) {
                    loginRms.setRecord(13, cdn.getBytes(), 0, cdn.length());
                } else {
                    loginRms.addRecord(cdn.getBytes(), 0, cdn.length());
                }
                if (loginRms.getNumRecords() < 14) {
                    byte[] iconTypeByte = {State.ICON_TYPE_CIRCLE};
                    loginRms.addRecord(iconTypeByte, 0, 1);
                }
                if (loginRms.getNumRecords() < 15) {
                    byte[] attachSize = "1000".getBytes();
                    loginRms.addRecord(attachSize, 0, attachSize.length);
                }
                if (loginRms.getNumRecords() < 16) {
                    byte[] oneByte = {1};
                    loginRms.addRecord(oneByte, 0, 1);
                }
                if (loginRms.getNumRecords() < 17) {
                    byte[] zeroByte = {0};
                    loginRms.addRecord(zeroByte, 0, 1);
                }
                if (loginRms.getNumRecords() < 18) {
                    byte[] zeroByte = {0};
                    loginRms.addRecord(zeroByte, 0, 1);
                }
                if (loginRms.getNumRecords() < 19) {
                    byte[] oneByte = {1};
                    loginRms.addRecord(oneByte, 0, 1);
                }
                if (loginRms.getNumRecords() >= 20) {
                    loginRms.setRecord(20, tokenTypeRecord, 0, 1);
                } else {
                    loginRms.addRecord(tokenTypeRecord, 0, 1);
                }
                if (loginRms.getNumRecords() < 21) {
                    byte[] oneByte = {1};
                    loginRms.addRecord(oneByte, 0, 1);
                }
                if (loginRms.getNumRecords() < 22) {
                    byte[] zeroStrBytes = "0".getBytes();
                    for (int i = 0; i < 5; i++) {
                        loginRms.addRecord(zeroStrBytes, 0, zeroStrBytes.length);
                    }
                }
                if (loginRms.getNumRecords() < 27) {
                    byte[] oneByte = {1};
                    loginRms.addRecord(oneByte, 0, 1);
                }
                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            s.login(api, gateway, cdn, token);
        }
        else if (c == quitCommand) {
            s.midlet.notifyDestroyed();
        }
    }
}
