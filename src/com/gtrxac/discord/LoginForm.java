package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener {
    State s;

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

        LoginSettings.load(s);

        if (State.isBlackBerry()) {
            String[] wifiChoices = {"Use Wi-Fi"};
            Image[] wifiImages = {null};
            wifiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, wifiChoices, wifiImages);
            wifiGroup.setSelectedIndex(0, s.bbWifi);
            append(wifiGroup);
        }

        apiField = new TextField("API URL", s.api, 200, 0);
        cdnField = new TextField("CDN URL", s.cdn, 200, 0);
        gatewayField = new TextField("Gateway URL", s.gatewayUrl, 200, 0);
        tokenField = new TextField("Token", s.token, 200, TextField.NON_PREDICTIVE);
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
            s.api = apiField.getString();
            s.cdn = cdnField.getString();
            s.gatewayUrl = gatewayField.getString();
            s.token = tokenField.getString().trim();

            if (s.token.length() == 0) {
                s.error("Please enter your token");
                return;
            }
            if (s.api.length() == 0) {
                s.error("Please specify an API URL");
                return;
            }

            boolean[] selected = {false};
            gatewayGroup.getSelectedFlags(selected);
            s.useGateway = selected[0];

            if (State.isBlackBerry()) {
                wifiGroup.getSelectedFlags(selected);
                s.bbWifi = selected[0];
            }

            s.tokenType = tokenGroup.getSelectedIndex();
            
            LoginSettings.save(s);
            s.login();
        }
        else if (c == quitCommand) {
            s.midlet.notifyDestroyed();
        }
    }
}
