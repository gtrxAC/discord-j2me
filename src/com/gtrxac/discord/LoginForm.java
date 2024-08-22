package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener, Strings {
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
        super(Locale.get(LOGIN_FORM_TITLE));
        setCommandListener(this); 
        this.s = s;

        LoginSettings.load(s);

        if (State.isBlackBerry()) {
            String[] wifiChoices = {Locale.get(USE_WIFI)};
            Image[] wifiImages = {null};
            wifiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, wifiChoices, wifiImages);
            wifiGroup.setSelectedIndex(0, s.bbWifi);
            append(wifiGroup);
        }

        apiField = new TextField(Locale.get(API_URL), s.api, 200, 0);
        cdnField = new TextField(Locale.get(CDN_URL), s.cdn, 200, 0);
        gatewayField = new TextField(Locale.get(GATEWAY_URL), s.gatewayUrl, 200, 0);
        tokenField = new TextField(Locale.get(TOKEN), s.token, 200, TextField.NON_PREDICTIVE);
        nextCommand = Locale.createCommand(LOG_IN, Command.OK, 0);
        quitCommand = Locale.createCommand(QUIT, Command.EXIT, 1);

        String[] gatewayChoices = {Locale.get(USE_GATEWAY)};
        Image[] gatewayImages = {null};
        gatewayGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, gatewayChoices, gatewayImages);
        gatewayGroup.setSelectedIndex(0, s.useGateway);

        String[] tokenChoices = {
            Locale.get(SEND_TOKEN_HEADER),
            Locale.get(SEND_TOKEN_JSON),
            Locale.get(SEND_TOKEN_QUERY)
        };
        Image[] tokenImages = {null, null, null};
        tokenGroup = new ChoiceGroup(Locale.get(SEND_TOKEN_AS), ChoiceGroup.EXCLUSIVE, tokenChoices, tokenImages);
        tokenGroup.setSelectedIndex(s.tokenType, true);

        append(new StringItem(null, Locale.get(LOGIN_FORM_WARNING)));
        append(apiField);
        append(cdnField);
        append(gatewayGroup);
        append(gatewayField);
        append(new StringItem(null, Locale.get(LOGIN_FORM_TOKEN_HELP)));
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
                s.error(Locale.get(LOGIN_ERROR_TOKEN));
                return;
            }
            if (s.api.length() == 0) {
                s.error(Locale.get(LOGIN_ERROR_API));
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
