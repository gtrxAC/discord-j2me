package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener {
    private TextField apiField;
    private TextField tokenField;
    private Command nextCommand;
    private Command quitCommand;
    private Command underscoreCommand;

    public LoginForm() {
        super("Log in");
        setCommandListener(this);

        Settings.load();

        apiField = new TextField("API URL", App.api, 200, 0);
        tokenField = new TextField("Token", App.token, 200, 0);

        append("Only use proxies that you trust!");
        append(apiField);
        append("The token can be found from your browser's dev tools (look online for help). Using an alt account is recommended.");
        append(tokenField);

        nextCommand = new Command("Log in", Command.OK, 0);
        quitCommand = new Command("Quit", Command.EXIT, 1);
        addCommand(nextCommand);
        addCommand(quitCommand);

        if (!Util.isNokia) {
            underscoreCommand = new Command("Underscore", Command.SCREEN, 2);
            addCommand(underscoreCommand);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
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
        }
        else if (c == underscoreCommand) {
            int caret = tokenField.getCaretPosition();
            String str = tokenField.getString();
            if (caret == 0) caret = str.length();
            tokenField.setString(str.substring(0, caret) + "_" + str.substring(caret));
        }
        else {
            // quit command
            App.instance.notifyDestroyed();
        }
    }
}
