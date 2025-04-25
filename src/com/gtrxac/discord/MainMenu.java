package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MainMenu extends List implements CommandListener {
    private Command quitCommand;

    public MainMenu() {
        super("Discord", List.IMPLICIT);
        setCommandListener(this);

        quitCommand = new Command("Quit", Command.EXIT, 0);

        append("Servers", null);
        if (!FavoriteGuilds.empty()) append(FavoriteGuilds.label2, null);
        append((Util.screenWidth <= 96) ? "Direct msgs." : "Direct messages", null);
        append("Settings", null);
        append("Log out", null);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (FavoriteGuilds.empty() && index > 0) index++;

            switch (index) {
                case 0: {
                    App.openGuildSelector(true);
                    break;
                }
                case 1: {
                    FavoriteGuilds.openSelector();
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
