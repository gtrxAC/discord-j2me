package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MainMenu extends List implements CommandListener {
    State s;
    private Command quitCommand;

    public MainMenu(State s) {
        super("Discord", List.IMPLICIT);
        setCommandListener(this); 
        this.s = s;

        quitCommand = new Command("Quit", Command.EXIT, 0);

        append("Servers", s.ic.guilds);
        if (!FavoriteGuilds.empty()) append("Favorites", s.ic.favorites);
        append("Direct messages", s.ic.dms);
        append("Settings", s.ic.settings);
        append("Log out", s.ic.logout);
        addCommand(quitCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (FavoriteGuilds.empty() && index >= 1) index++;

            switch (index) {
                case 0: {
                    s.openGuildSelector(true);
                    break;
                }
                case 1: {
                    FavoriteGuilds.openSelector(s, true);
                    break;
                }
                case 2: {
                    s.openDMSelector(true);
                    break;
                }
                case 3: {
                    s.disp.setCurrent(new SettingsForm(s));
                    break;
                }
                case 4: {
                    if (s.gateway != null) s.gateway.stop = true;
                    s.myUserId = null;
                    s.disp.setCurrent(new LoginForm(s));
                }
            }
        }
        else if (c == quitCommand) {
            s.midlet.notifyDestroyed();
        }
    }
}
