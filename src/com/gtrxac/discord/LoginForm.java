package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener {
    private TextField apiField;
    private TextField tokenField;

    public LoginForm() {
        super("Welcome");
        setCommandListener(this);

        Settings.load();

        apiField = new TextField("API URL", App.api, 200, 0);
        tokenField = new TextField("Token", App.token, 200, 0);

        append("Only use proxies that you trust! Hosting your own proxy or using an alt account is recommended.");
        append(apiField);
        append("The token can be found from your browser's dev tools (look online for help).");
        append(tokenField);

        addCommand(new Command("Log in", Command.OK, 0));
        addCommand(new Command("Quit", Command.EXIT, 1));

        if (!App.isNokia) {
            addCommand(new Command("Underscore", Command.SCREEN, 2));
        }
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case 0: {  // login
                App.api = apiField.getString();
                App.token = tokenField.getString().trim();

                if (App.token.length() == 0) {
                    App.error("Please enter your token");
                    return;
                }
                if (App.api.length() == 0) {
                    App.error("Please specify an API URL");
                    return;
                }
                Settings.save();
                App.login();
                break;
            }

            case 1: {  // quit
                App.instance.notifyDestroyed();
                break;
            }

            case 2: {  // insert underscore
                int caret = tokenField.getCaretPosition();
                String str = tokenField.getString();
                if (caret == 0) caret = str.length();
                tokenField.setString(str.substring(0, caret) + "_" + str.substring(caret));
                break;
            }
        }
    }
}
