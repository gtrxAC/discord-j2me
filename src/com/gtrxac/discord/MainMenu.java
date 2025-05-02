package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MainMenu extends List implements CommandListener {
    private Command quitCommand;
    public static int lastSelected;

    public MainMenu() {
        super("Discord", List.IMPLICIT);
        setCommandListener(this);

        append("Servers", null);
        if (!Settings.favEmpty()) append(Settings.favLabel2, null);
        append((App.screenWidth <= 96) ? "Direct msgs." : "Direct messages", null);
        append("Settings", null);
        append("Log out", null);
        setSelectedIndex(lastSelected, true);

        quitCommand = new Command("Quit", Command.EXIT, 0);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            int index = getSelectedIndex();
            lastSelected = index;
            if (Settings.favEmpty() && index > 0) index++;

            switch (index) {
                case 0: {
                    App.openGuildSelector(true);
                    break;
                }
                case 1: {
                    Settings.favOpenSelector();
                    break;
                }
                case 2: {
                    App.isDM = true;
                    App.openChannelSelector(true);
                    break;
                }
                case 3: {
                    App.disp.setCurrent(new SettingsForm());
                    break;
                }
                case 4: {
                    App.disp.setCurrent(new LoginForm());
                    break;
                }
            }
        } else {
            // quit command
            App.instance.notifyDestroyed();
        }
    }
}
