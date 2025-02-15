package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener,
// ifdef NOT_BLACKBERRY
ItemCommandListener,
// endif
Strings
{
    private TextField apiField;
    // ifdef BLACKBERRY
    private ChoiceGroup wifiGroup;
    // endif
    private ChoiceGroup gatewayGroup;
    private TextField gatewayField;
    private TextField cdnField;
    private Command changeTokenCommand;
    private Command importTokenCommand;
    private ChoiceGroup tokenGroup;
    private Command nextCommand;
    private Command quitCommand;

    private TextBox tokenBox;
    private Command tokenBoxOkCommand;
    private Command tokenBoxCancelCommand;
    private Command tokenBoxUnderscoreCommand;

    public LoginForm() {
        super(Locale.get(LOGIN_FORM_TITLE));
        setCommandListener(this); 

        // ifdef BLACKBERRY
        String[] wifiChoices = {Locale.get(USE_WIFI)};
        wifiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, wifiChoices, null);
        wifiGroup.setSelectedIndex(0, Settings.bbWifi);
        append(wifiGroup);
        // endif

        apiField = new TextField(Locale.get(API_URL), Settings.api, 200, 0);
        cdnField = new TextField(Locale.get(CDN_URL), Settings.cdn, 200, 0);
        gatewayField = new TextField(Locale.get(GATEWAY_URL), Settings.gatewayUrl, 200, 0);

        boolean haveToken = (Settings.token != null && Settings.token.length() != 0);
        String tokenLabel = Locale.get(haveToken ? CHANGE_TOKEN_L : SET_TOKEN_L);
        String tokenLabelShort = Locale.get(haveToken ? CHANGE_TOKEN : SET_TOKEN);

        changeTokenCommand = new Command(tokenLabelShort, tokenLabel, Command.ITEM, 0);
        // ifdef NOT_BLACKBERRY
        StringItem tokenButton = new StringItem(null, tokenLabel, Item.BUTTON);
        tokenButton.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
        tokenButton.setDefaultCommand(changeTokenCommand);
        tokenButton.setItemCommandListener(this);
        
        StringItem importTokenButton = null;
        // endif
        
        if (Util.supportsFileConn) {
            importTokenCommand = Locale.createCommand(IMPORT_TOKEN, Command.ITEM, 1);
            // ifdef NOT_BLACKBERRY
            importTokenButton = new StringItem(null, Locale.get(IMPORT_TOKEN_L), Item.BUTTON);
            importTokenButton.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
            importTokenButton.setDefaultCommand(importTokenCommand);
            importTokenButton.setItemCommandListener(this);
            // endif
        }

        nextCommand = Locale.createCommand(LOG_IN, Command.OK, 1);
        quitCommand = Locale.createCommand(QUIT, Command.EXIT, 2);

        String[] gatewayChoices = {Locale.get(USE_GATEWAY)};
        gatewayGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, gatewayChoices, null);
        gatewayGroup.setSelectedIndex(0, Settings.useGateway);

        String[] tokenChoices = {
            Locale.get(SEND_TOKEN_HEADER),
            Locale.get(SEND_TOKEN_JSON),
            Locale.get(SEND_TOKEN_QUERY)
        };
        tokenGroup = new ChoiceGroup(Locale.get(SEND_TOKEN_AS), ChoiceGroup.EXCLUSIVE, tokenChoices, null);
        tokenGroup.setSelectedIndex(Settings.tokenType, true);

        append(new StringItem(null, Locale.get(LOGIN_FORM_WARNING)));
        append(apiField);
        append(cdnField);
        append(gatewayGroup);
        append(gatewayField);
        append(new StringItem(null, Locale.get(LOGIN_FORM_TOKEN_HELP)));
        // ifdef BLACKBERRY
        append(new Spacer(getWidth(), 1));
        String tokenHint = Locale.get(haveToken ? LOGIN_FORM_CHANGE_TOKEN_BB_HINT : LOGIN_FORM_SET_TOKEN_BB_HINT);
        append(new StringItem(null, tokenHint));
        addCommand(changeTokenCommand);
        if (Util.supportsFileConn) addCommand(importTokenCommand);
        // else
        append(tokenButton);
        if (Util.supportsFileConn) append(importTokenButton);
        // endif
        append(tokenGroup);
        addCommand(nextCommand);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            Settings.api = apiField.getString();
            Settings.cdn = cdnField.getString();
            Settings.gatewayUrl = gatewayField.getString();

            if (Settings.token.length() == 0) {
                App.error(Locale.get(LOGIN_ERROR_TOKEN));
                return;
            }
            if (Settings.api.length() == 0) {
                App.error(Locale.get(LOGIN_ERROR_API));
                return;
            }

            boolean[] selected = {false};
            gatewayGroup.getSelectedFlags(selected);
            Settings.useGateway = selected[0];

            // ifdef BLACKBERRY
            wifiGroup.getSelectedFlags(selected);
            Settings.bbWifi = selected[0];
            // endif

            Settings.tokenType = tokenGroup.getSelectedIndex();
            
            Settings.save();
            App.login();
        }
        else if (c == quitCommand) {
            DiscordMIDlet.instance.notifyDestroyed();
        }
        // ifdef BLACKBERRY
        else if (c == changeTokenCommand) {
            showTokenEntry();
        }
        else if (c == importTokenCommand) {
            App.disp.setCurrent(new TokenFilePicker());
        }
        // endif
        else if (c == tokenBoxUnderscoreCommand) {
            int caretPosition = tokenBox.getCaretPosition();
            String currentText = tokenBox.getString();
            String newText = currentText.substring(0, caretPosition) + "_" + currentText.substring(caretPosition);
            tokenBox.setString(newText);
        }
        else {
            if (c == tokenBoxOkCommand) Settings.token = tokenBox.getString().trim();
            App.disp.setCurrent(this);
        }
    }

    // ifdef NOT_BLACKBERRY
    public void commandAction(Command c, Item i) {
        if (c == changeTokenCommand) {
            showTokenEntry();
        } else {
            // import token command
            App.disp.setCurrent(new TokenFilePicker());
        }
    }
    // endif

    private void showTokenEntry() {
        tokenBox = new TextBox("Set token", Settings.token, 200, 0);
        tokenBoxOkCommand = Locale.createCommand(OK, Command.OK, 0);
        tokenBoxCancelCommand = Locale.createCommand(CANCEL, Command.BACK, 1);
        tokenBoxUnderscoreCommand = Locale.createCommand(INSERT_UNDERSCORE, Command.ITEM, 2);
        tokenBox.addCommand(tokenBoxOkCommand);
        tokenBox.addCommand(tokenBoxCancelCommand);
        tokenBox.addCommand(tokenBoxUnderscoreCommand);
        tokenBox.setCommandListener(this);
        App.disp.setCurrent(tokenBox);
    }
}
