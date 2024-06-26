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

    public LoginForm(State s) {
        super("Log in");
        setCommandListener(this); 
        this.s = s;

        String initialApi = "http://57.128.194.14:8087";
        String initialGateway = "socket://57.128.194.14:8085";
        String initialCdn = "http://media.discordapp.net";
        String initialToken = "";
        
        String manifestToken = s.midlet.getAppProperty("Token");
        if (manifestToken != null) initialToken = manifestToken;
        
        if (RecordStore.listRecordStores() != null) {
            try {
                loginRms = RecordStore.openRecordStore("login", true);

                if (loginRms.getNumRecords() > 0) {
                    String savedApi = new String(loginRms.getRecord(1));
                    if (savedApi.length() > 0) initialApi = savedApi;
                    String savedToken = new String(loginRms.getRecord(2));
                    if (savedToken.length() > 0) initialToken = savedToken;
                }
                if (loginRms.getNumRecords() >= 3) {
                    s.theme = loginRms.getRecord(3)[0];
                }
                if (loginRms.getNumRecords() >= 4) {
                    s.oldUI = loginRms.getRecord(4)[0] != 0;
                }
                if (loginRms.getNumRecords() >= 5) {
                    String savedGateway = new String(loginRms.getRecord(5));
                    if (savedGateway.length() > 0) initialGateway = savedGateway;
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
                if (loginRms.getNumRecords() >= 10) {
                    s.useGateway = loginRms.getRecord(10)[0] != 0;
                } else {
                    s.useGateway = true;
                }
                if (loginRms.getNumRecords() >= 11) {
                    s.bbWifi = loginRms.getRecord(11)[0] != 0;
                } else {
                    s.bbWifi = true;
                }
                if (loginRms.getNumRecords() >= 12) {
                    s.useJpeg = loginRms.getRecord(12)[0] != 0;
                } else {
                    s.useJpeg = true;
                }
                if (loginRms.getNumRecords() >= 13) {
                    String savedCdn = new String(loginRms.getRecord(13));
                    if (savedCdn.length() > 0) initialCdn = savedCdn;
                }
                if (loginRms.getNumRecords() >= 14) {
                    s.iconType = loginRms.getRecord(14)[0];
                } else {
                    s.iconType = State.ICON_TYPE_CIRCLE;
                }
                if (loginRms.getNumRecords() >= 15) {
                    try {
                        s.attachmentSize = Integer.parseInt(new String(loginRms.getRecord(15)));
                    }
                    catch (Exception e) {
                        s.attachmentSize = 1000;
                    }
                } else {
                    s.attachmentSize = 1000;
                }
                if (loginRms.getNumRecords() >= 16) {
                    s.iconSize = loginRms.getRecord(16)[0];
                } else {
                    s.iconSize = 1;  // 16px
                }
                if (loginRms.getNumRecords() >= 17) {
                    s.nativeFilePicker = loginRms.getRecord(17)[0] != 0;
                } else {
                    s.nativeFilePicker = false;
                }
                if (loginRms.getNumRecords() >= 18) {
                    s.autoReConnect = loginRms.getRecord(18)[0] != 0;
                } else {
                    s.autoReConnect = false;
                }
                if (loginRms.getNumRecords() >= 19) {
                    s.showMenuIcons = loginRms.getRecord(19)[0] != 0;
                } else {
                    s.showMenuIcons = true;
                }
                if (loginRms.getNumRecords() >= 20) {
                    s.tokenInJson = loginRms.getRecord(20)[0] != 0;
                } else {
                    s.tokenInJson = false;
                }
                if (loginRms.getNumRecords() >= 21) {
                    s.useNameColors = loginRms.getRecord(21)[0] != 0;
                } else {
                    s.useNameColors = true;
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
        cdnField = new TextField("CDN URL", initialCdn, 200, 0);
        gatewayField = new TextField("Gateway URL", initialGateway, 200, 0);
        tokenField = new TextField("Token", initialToken, 200, TextField.NON_PREDICTIVE);
        nextCommand = new Command("Log in", Command.OK, 0);

        String[] gatewayChoices = {"Use gateway"};
        Image[] gatewayImages = {null};
        gatewayGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, gatewayChoices, gatewayImages);
        gatewayGroup.setSelectedIndex(0, s.useGateway);

        String[] tokenChoices = {"Send token as JSON"};
        Image[] tokenImages = {null};
        tokenGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, tokenChoices, tokenImages);
        tokenGroup.setSelectedIndex(0, s.tokenInJson);

        append(new StringItem(null, "Only use proxies that you trust!"));
        append(apiField);
        append(cdnField);
        append(gatewayGroup);
        append(gatewayField);
        append(new StringItem(null, "The token can be found from your browser's dev tools (look online for help). Using an alt account is recommended."));
        append(tokenField);
        append(tokenGroup);
        addCommand(nextCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            String api = apiField.getString();
            String cdn = cdnField.getString();
            String gateway = gatewayField.getString();
            String token = tokenField.getString();

            boolean[] selected = {false};
            gatewayGroup.getSelectedFlags(selected);
            s.useGateway = selected[0];
            byte[] useGatewayRecord = {new Integer(s.useGateway ? 1 : 0).byteValue()};

            if (State.isBlackBerry()) {
                wifiGroup.getSelectedFlags(selected);
                s.bbWifi = selected[0];
            }
            byte[] bbWifiRecord = {new Integer(s.bbWifi ? 1 : 0).byteValue()};

            tokenGroup.getSelectedFlags(selected);
            s.tokenInJson = selected[0];
            byte[] tokenJsonRecord = {new Integer(s.tokenInJson ? 1 : 0).byteValue()};
            
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
                    loginRms.setRecord(20, tokenJsonRecord, 0, 1);
                } else {
                    loginRms.addRecord(tokenJsonRecord, 0, 1);
                }
                if (loginRms.getNumRecords() < 21) {
                    byte[] oneByte = {1};
                    loginRms.addRecord(oneByte, 0, 1);
                }
                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (token.length() == 0) {
                s.error("Please enter your token");
                return;
            }
            if (api.length() == 0) {
                s.error("Please specify an API URL");
                return;
            }

            s.loadFonts();
            s.cdn = cdn; 
            s.http = new HTTPThing(s, api, token);
            s.disp.setCurrent(new MainMenu(s));

            if (s.useGateway) {
                s.gateway = new GatewayThread(s, gateway, token);
                s.gateway.start();
            }
        }
    }
}
