package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener {
    State s;

    private TextField apiField;
    private ChoiceGroup wifiGroup;
    private TextField tokenField;
    private ChoiceGroup tokenGroup;
    private Command nextCommand;
    private Command quitCommand;

    public LoginForm(State s) {
        super("Log in");
        setCommandListener(this); 
        this.s = s;

        LoginSettings.load(s);

        apiField = new TextField("API URL", s.api, 200, 0);
        tokenField = new TextField("Token", s.token, 200, 0);
        nextCommand = new Command("Log in", Command.OK, 0);
        quitCommand = new Command("Quit", Command.EXIT, 1);

        String[] tokenChoices = {"HTTP header", "JSON", "Query parameter"};
        tokenGroup = new ChoiceGroup("Send token as", ChoiceGroup.EXCLUSIVE, tokenChoices, null);
        tokenGroup.setSelectedIndex(s.tokenType, true);

        append(new StringItem(null, "Only use proxies that you trust!"));
        append(apiField);
        append(new StringItem(null, "The token can be found from your browser's dev tools (look online for help). Using an alt account is recommended."));
        append(tokenField);
        append(tokenGroup);
        addCommand(nextCommand);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            s.api = apiField.getString();
            s.token = tokenField.getString().trim();

            if (s.token.length() == 0) {
                s.error("Please enter your token");
                return;
            }
            if (s.api.length() == 0) {
                s.error("Please specify an API URL");
                return;
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
