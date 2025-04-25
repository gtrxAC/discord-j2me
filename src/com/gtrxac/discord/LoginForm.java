package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener {
    private TextField apiField;
    private TextField tokenField;
    private Command nextCommand;
    private Command quitCommand;

    public LoginForm() {
        super("Log in");
        setCommandListener(this);

        Settings.load();

        apiField = new TextField("API URL", App.api, 200, 0);
        tokenField = new TextField("Token", App.token, 200, 0);
        nextCommand = new Command("Log in", Command.OK, 0);
        quitCommand = new Command("Quit", Command.EXIT, 1);

        append("Only use proxies that you trust!");
        append(apiField);
        append("The token can be found from your browser's dev tools (look online for help). Using an alt account is recommended.");
        append(tokenField);
        addCommand(nextCommand);
        addCommand(quitCommand);
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
        } else {
            // quit command
            App.instance.notifyDestroyed();
        }
    }
}
