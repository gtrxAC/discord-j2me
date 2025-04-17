package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MainMenu extends List implements CommandListener {
    private Command quitCommand;

    public MainMenu() {
        super("Discord", List.IMPLICIT);
        setCommandListener(this);

        quitCommand = new Command("Quit", Command.EXIT, 0);

        append("Servers", null);
        append("Direct msgs.", null);
        append("Settings", null);
        append("Log out", null);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            switch (getSelectedIndex()) {
                case 0: {
                    App.openGuildSelector(true);
                    break;
                }
                case 1: {
                    App.isDM = true;
                    App.openChannelSelector(true);
                    break;
                }
                case 2: {
                    App.disp.setCurrent(new SettingsForm());
                    break;
                }
                case 3: {
                    App.disp.setCurrent(new LoginForm());
                    break;
                }
            }
        }
        else if (c == quitCommand) {
            App.instance.notifyDestroyed();
        }
    }
}
