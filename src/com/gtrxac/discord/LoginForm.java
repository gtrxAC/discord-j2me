package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener, ItemCommandListener, Strings {
    State s;

    private TextField apiField;
    // ifdef BLACKBERRY
    private ChoiceGroup wifiGroup;
    // endif
    private ChoiceGroup gatewayGroup;
    private TextField gatewayField;
    private TextField cdnField;
    private Command changeTokenCommand;
    private ChoiceGroup tokenGroup;
    private Command nextCommand;
    private Command quitCommand;

    private TextBox tokenBox;
    private Command tokenBoxOkCommand;
    private Command tokenBoxCancelCommand;

    public LoginForm(State s) {
        super(Locale.get(LOGIN_FORM_TITLE));
        setCommandListener(this); 
        this.s = s;

        LoginSettings.load(s);

        // ifdef BLACKBERRY
        String[] wifiChoices = {Locale.get(USE_WIFI)};
        wifiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, wifiChoices, null);
        wifiGroup.setSelectedIndex(0, s.bbWifi);
        append(wifiGroup);
        // endif

        apiField = new TextField(Locale.get(API_URL), s.api, 200, 0);
        cdnField = new TextField(Locale.get(CDN_URL), s.cdn, 200, 0);
        gatewayField = new TextField(Locale.get(GATEWAY_URL), s.gatewayUrl, 200, 0);

        boolean haveToken = (s.token != null && s.token.length() != 0);
        String tokenLabel = haveToken ? "Change token" : "Set token";
        String tokenLabelShort = haveToken ? "Change" : "Set";

        changeTokenCommand = new Command(tokenLabelShort, tokenLabel, Command.ITEM, 0); //Locale.createCommand(SHOW, Command.ITEM, i + 100);
        StringItem tokenButton = new StringItem(null, tokenLabel, Item.BUTTON);
        tokenButton.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
        tokenButton.setDefaultCommand(changeTokenCommand);
        tokenButton.setItemCommandListener(this);

        nextCommand = Locale.createCommand(LOG_IN, Command.OK, 1);
        quitCommand = Locale.createCommand(QUIT, Command.EXIT, 2);

        String[] gatewayChoices = {Locale.get(USE_GATEWAY)};
        gatewayGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, gatewayChoices, null);
        gatewayGroup.setSelectedIndex(0, s.useGateway);

        String[] tokenChoices = {
            Locale.get(SEND_TOKEN_HEADER),
            Locale.get(SEND_TOKEN_JSON),
            Locale.get(SEND_TOKEN_QUERY)
        };
        tokenGroup = new ChoiceGroup(Locale.get(SEND_TOKEN_AS), ChoiceGroup.EXCLUSIVE, tokenChoices, null);
        tokenGroup.setSelectedIndex(s.tokenType, true);

        append(new StringItem(null, Locale.get(LOGIN_FORM_WARNING)));
        append(apiField);
        append(cdnField);
        append(gatewayGroup);
        append(gatewayField);
        append(new StringItem(null, Locale.get(LOGIN_FORM_TOKEN_HELP)));
        append(tokenButton);
        append(tokenGroup);
        addCommand(nextCommand);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            s.api = apiField.getString();
            s.cdn = cdnField.getString();
            s.gatewayUrl = gatewayField.getString();

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

            // ifdef BLACKBERRY
            wifiGroup.getSelectedFlags(selected);
            s.bbWifi = selected[0];
            // endif

            s.tokenType = tokenGroup.getSelectedIndex();
            
            LoginSettings.save(s);
            s.login();
        }
        else if (c == quitCommand) {
            s.midlet.notifyDestroyed();
        }
        else {
            if (c == tokenBoxOkCommand) s.token = tokenBox.getString().trim();
            s.disp.setCurrent(this);
        }
    }

    public void commandAction(Command c, Item i) {
        // Only item command action is to change token
        tokenBox = new TextBox("Set token", s.token, 200, 0);
        tokenBoxOkCommand = Locale.createCommand(OK, Command.OK, 0);
        tokenBoxCancelCommand = Locale.createCommand(CANCEL, Command.BACK, 1);
        tokenBox.addCommand(tokenBoxOkCommand);
        tokenBox.addCommand(tokenBoxCancelCommand);
        tokenBox.setCommandListener(this);
        s.disp.setCurrent(tokenBox);
    }
}
