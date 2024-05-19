package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MainMenu extends List implements CommandListener {
    State s;
    private Command backCommand;

    public MainMenu(State s) {
        super(null, List.IMPLICIT);
        setCommandListener(this); 
        this.s = s;

        backCommand = new Command("Back", Command.BACK, 0);

        append("Servers", null);
        append("Direct messages", null);
        append("Settings", null);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            switch (getSelectedIndex()) {
                case 0: {
                    s.openGuildSelector(true);
                    break;
                }
                case 1: {
                    s.openDMSelector(true);
                    break;
                }
                case 2: {
                    s.disp.setCurrent(new SettingsForm(s));
                    break;
                }
            }
        }
        else if (c == backCommand) {
            if (s.gateway != null) s.gateway.stop = true;
            s.disp.setCurrent(new LoginForm(s));
        }
    }
}
