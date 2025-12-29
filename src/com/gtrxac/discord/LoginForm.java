package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener,
//#ifndef BLACKBERRY
ItemCommandListener,
//#endif
Strings
{
    private TextField apiField;
//#ifdef BLACKBERRY
    private ChoiceGroup wifiGroup;
//#endif
    private ChoiceGroup gatewayGroup;
//#ifdef PROXYLESS_SUPPORT
    private ChoiceGroup proxylessGroup;
    private Command proxylessInfoCommand;
//#endif
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
//#ifdef SYMBIAN
    private Command nnpLinkCommand;
//#endif
    private Command guideLinkCommand;
    private Command urlsBackCommand;
    private Command editUrlsCommand;

    public LoginForm() {
        super(Locale.get(LOGIN_FORM_TITLE_V2));
        setCommandListener(this); 

//#ifdef BLACKBERRY
        String[] wifiChoices = {Locale.get(USE_WIFI)};
        wifiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, wifiChoices, null);
        wifiGroup.setSelectedIndex(0, Settings.bbWifi);
        append(wifiGroup);
//#endif

//#ifdef PROXYLESS_SUPPORT
//#ifdef PROXYLESS_DEFAULT
        append(Locale.get(PROXYLESS_INFO));
//#else
//#ifdef SYMBIAN
        nnpLinkCommand = Locale.createCommand(GET_TLS, Command.ITEM, 0);
        StringItem nnpLinkItem = new StringItem(null, Locale.get(PROXYLESS_INFO_TLS), Item.HYPERLINK);
        nnpLinkItem.setDefaultCommand(nnpLinkCommand);
        nnpLinkItem.setItemCommandListener(this);

        append(Locale.get(PROXYLESS_INFO_TLS_PREFIX));
        append(nnpLinkItem);
        append(Locale.get(PROXYLESS_INFO_TLS_SUFFIX));
//#else
        append(Locale.get(PROXYLESS_INFO_TLS_PREFIX) + Locale.get(PROXYLESS_INFO_TLS) + Locale.get(PROXYLESS_INFO_TLS_SUFFIX));
//#endif

        proxylessInfoCommand = Locale.createCommand(OPEN, Command.ITEM, 1);
        StringItem proxylessInfoItem = new StringItem(null, Locale.get(PROXYLESS_LINK), Item.HYPERLINK);
        proxylessInfoItem.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER);
        proxylessInfoItem.setDefaultCommand(proxylessInfoCommand);
        proxylessInfoItem.setItemCommandListener(this);
        append(proxylessInfoItem);
//#endif

        addSpacer(2);

        String[] proxylessChoices = {Locale.get(PROXYLESS)};
        proxylessGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, proxylessChoices, null);
        proxylessGroup.setSelectedIndex(0, Settings.proxyless);
        append(proxylessGroup);

        addSpacer(4);

        append(Locale.get(PROXY_INFO) + Locale.get(PROXY_INFO_RECOMMEND));
//#else
        append(Locale.get(PROXY_INFO_RECOMMEND) + Locale.get(LOGIN_FORM_WARNING));
//#endif

        editUrlsCommand = Locale.createCommand(OPEN, Command.ITEM, 2);
//#ifdef BLACKBERRY
        append(new Spacer(getWidth(), 1));
        append(Locale.get(EDIT_URLS_BB_HINT));
        addCommand(editUrlsCommand);
//#else
        StringItem urlsButton = new StringItem(null, Locale.get(CONNECTION_URLS), Item.BUTTON);
        urlsButton.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
        urlsButton.setDefaultCommand(editUrlsCommand);
        urlsButton.setItemCommandListener(this);
        append(urlsButton);
//#endif

        addSpacer(4);

//#ifdef PROXYLESS_SUPPORT
        append(Locale.get(GATEWAY_INFO_WITH_PROXYLESS));
//#else
        append(Locale.get(GATEWAY_INFO));
//#endif

        addSpacer(2);

        String[] gatewayChoices = {Locale.get(USE_GATEWAY)};
        gatewayGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, gatewayChoices, null);
        gatewayGroup.setSelectedIndex(0, Settings.useGateway);
        append(gatewayGroup);
        
        addSpacer(4);

        append(Locale.get(LOGIN_FORM_TOKEN_HELP_V2) + Locale.get(LOGIN_FORM_TOKEN_HELP));
        
        boolean haveToken = (Settings.token != null && Settings.token.length() != 0);
        String tokenLabel = Locale.get(haveToken ? CHANGE_TOKEN_L : SET_TOKEN_L);
        String tokenLabelShort = Locale.get(haveToken ? CHANGE_TOKEN : SET_TOKEN);

        changeTokenCommand = new Command(tokenLabelShort, tokenLabel, Command.ITEM, 0);
        guideLinkCommand = Locale.createCommand(SETUP_GUIDE, Command.ITEM, 4);
//#ifndef BLACKBERRY
        StringItem tokenButton = new StringItem(null, tokenLabel, Item.BUTTON);
        tokenButton.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
        tokenButton.setDefaultCommand(changeTokenCommand);
        tokenButton.setItemCommandListener(this);

        StringItem guideLinkButton = new StringItem(null, Locale.get(SETUP_GUIDE_L), Item.BUTTON);
        guideLinkButton.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
        guideLinkButton.setDefaultCommand(guideLinkCommand);
        guideLinkButton.setItemCommandListener(this);
        
        StringItem importTokenButton = null;
//#endif
        
        if (Util.supportsFileConn) {
            importTokenCommand = Locale.createCommand(IMPORT_TOKEN, Command.ITEM, 3);
//#ifndef BLACKBERRY
            importTokenButton = new StringItem(null, Locale.get(IMPORT_TOKEN_L), Item.BUTTON);
            importTokenButton.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
            importTokenButton.setDefaultCommand(importTokenCommand);
            importTokenButton.setItemCommandListener(this);
//#endif
        }

//#ifdef BLACKBERRY
        append(new Spacer(getWidth(), 1));
        if (Util.supportsFileConn) {
            append(Locale.get(TOKEN_IMPORT_HELP_WITH_FC));
        } else {
            append(Locale.get(TOKEN_IMPORT_HELP));
        }
        append(new Spacer(getWidth(), 1));
        String tokenHint = Locale.get(haveToken ? LOGIN_FORM_CHANGE_TOKEN_BB_HINT : LOGIN_FORM_SET_TOKEN_BB_HINT);
        append(tokenHint);
        addCommand(changeTokenCommand);
        if (Util.supportsFileConn) addCommand(importTokenCommand);
        addCommand(guideLinkCommand);
//#else
        append(tokenButton);
        append(new Spacer(getWidth(), 1));
        if (Util.supportsFileConn) {
            append(Locale.get(TOKEN_IMPORT_HELP_WITH_FC));
            append(importTokenButton);
        } else {
            append(Locale.get(TOKEN_IMPORT_HELP));
        }
        append(guideLinkButton);
//#endif

        nextCommand = Locale.createCommand(LOG_IN, Command.OK, 1);
        quitCommand = Locale.createCommand(QUIT, Command.EXIT, 2);
        addCommand(nextCommand);
        addCommand(quitCommand);
    }

    private void addSpacer(int height) {
        Spacer s = new Spacer(getWidth(), Util.fontSize*height/10);
        s.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
        append(s);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            if (Settings.token.length() == 0) {
                App.error(Locale.get(LOGIN_ERROR_TOKEN));
                return;
            }
            if (Settings.api.length() == 0) {
                App.error(Locale.get(LOGIN_ERROR_API));
                return;
            }

            boolean[] selected = {false};
//#ifdef PROXYLESS_SUPPORT
            proxylessGroup.getSelectedFlags(selected);
            Settings.proxyless = selected[0];
//#endif
            gatewayGroup.getSelectedFlags(selected);
            Settings.useGateway = selected[0];

//#ifdef BLACKBERRY
            wifiGroup.getSelectedFlags(selected);
            Settings.bbWifi = selected[0];
//#endif
            
            Settings.save();
            App.login();
        }
        else if (c == quitCommand) {
            App.instance.notifyDestroyed();
        }
//#ifdef BLACKBERRY
        else if (c == changeTokenCommand) {
            showTokenEntry();
        }
        else if (c == importTokenCommand) {
            App.disp.setCurrent(new TokenFilePicker());
        }
        else if (c == guideLinkCommand) {
            App.platRequest(Settings.api + "/j2me/guide");
        }
        else if (c == editUrlsCommand) {
            showUrls();
        }
//#endif
        else if (c == tokenBoxUnderscoreCommand) {
            int caretPosition = tokenBox.getCaretPosition();
            String currentText = tokenBox.getString();
            String newText = currentText.substring(0, caretPosition) + "_" + currentText.substring(caretPosition);
            tokenBox.setString(newText);
        }
        else if (c == urlsBackCommand) {
            Settings.api = apiField.getString();
            Settings.cdn = cdnField.getString();
            Settings.gatewayUrl = gatewayField.getString();
            Settings.tokenType = tokenGroup.getSelectedIndex();
            App.disp.setCurrent(this);
        }
        else {
            if (c == tokenBoxOkCommand) Settings.token = tokenBox.getString().trim();
            App.disp.setCurrent(this);
        }
    }

//#ifndef BLACKBERRY
    public void commandAction(Command c, Item i) {
//#ifdef SYMBIAN
        if (c == nnpLinkCommand) {
            App.platRequest("http://nnproject.cc/tls");
        } else
//#endif
        if (c == guideLinkCommand) {
            App.platRequest(Settings.api + "/j2me/guide");
        }
        else if (c == changeTokenCommand) {
            showTokenEntry();
        }
        else if (c == editUrlsCommand) {
            showUrls();
        }
//#ifdef PROXYLESS_SUPPORT
        else if (c == proxylessInfoCommand) {
            App.platRequest(Settings.api + "/j2me/proxyless");
        }
//#endif
        else {
            // import token command
            App.disp.setCurrent(new TokenFilePicker());
        }
    }
//#endif

    private void showTokenEntry() {
        tokenBox = new TextBox(Locale.get(SET_TOKEN_L), Settings.token, 200, 0);
        tokenBoxOkCommand = Locale.createCommand(OK, Command.OK, 0);
        tokenBoxCancelCommand = Locale.createCommand(CANCEL, Command.BACK, 1);
        tokenBoxUnderscoreCommand = Locale.createCommand(INSERT_UNDERSCORE, Command.ITEM, 2);
        tokenBox.addCommand(tokenBoxOkCommand);
        tokenBox.addCommand(tokenBoxCancelCommand);
        tokenBox.addCommand(tokenBoxUnderscoreCommand);
        tokenBox.setCommandListener(this);
        App.disp.setCurrent(tokenBox);
    }

    private void showUrls() {
        Form f = new Form(Locale.get(CONNECTION_URLS));
        f.setCommandListener(this);

        apiField = new TextField(Locale.get(API_URL), Settings.api, 200, 0);
        cdnField = new TextField(Locale.get(CDN_URL), Settings.cdn, 200, 0);
        gatewayField = new TextField(Locale.get(GATEWAY_URL), Settings.gatewayUrl, 200, 0);
        f.append(apiField);
        f.append(cdnField);
        f.append(gatewayField);
        
        String[] tokenChoices = {
            Locale.get(SEND_TOKEN_HEADER),
            Locale.get(SEND_TOKEN_QUERY)
        };
        tokenGroup = new ChoiceGroup(Locale.get(SEND_TOKEN_AS), ChoiceGroup.EXCLUSIVE, tokenChoices, null);
        tokenGroup.setSelectedIndex(Settings.tokenType, true);
        f.append(tokenGroup);
        
        urlsBackCommand = Locale.createCommand(BACK, Command.BACK, 0);
        f.addCommand(urlsBackCommand);

        App.disp.setCurrent(f);
    }
}
