package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MainMenu extends ListScreen implements CommandListener, Strings {
    private Command quitCommand;
    private static boolean hasFavorites;
    private static MainMenu instance;

    public static MainMenu get(boolean reload) {
        if (instance == null || reload) {
            instance = new MainMenu();
            hasFavorites = false;
        }
        // Determine if favorites option should be shown:
        // Add if needed
        if (!FavoriteGuilds.empty() && !hasFavorites) {
            instance.insert(1, Locale.get(MAIN_MENU_FAVORITES), App.ic.favorites);
            hasFavorites = true;
        }
        // Remove if needed
        else if (FavoriteGuilds.empty() && hasFavorites) {
            instance.delete(1);
            hasFavorites = false;
        }
        return instance;
    }

    private MainMenu() {
        super("Discord", false, false, false);
        setCommandListener(this); 

        quitCommand = Locale.createCommand(QUIT, Command.EXIT, 0);
        addCommand(quitCommand);

        append(Locale.get(MAIN_MENU_GUILDS), App.ic.guilds);
        append(Locale.get(MAIN_MENU_DMS), App.ic.dms);
        append(Locale.get(MAIN_MENU_SETTINGS), App.ic.settings);
        append(Locale.get(MAIN_MENU_ABOUT), App.ic.about);
        append(Locale.get(MAIN_MENU_LOG_OUT), App.ic.logout);
    }

//#ifdef SAMSUNG_FULL
    // recreate this screen for 480p font fix (see MyCanvas and Util)
    protected MyCanvas reload() {
        return get(true);
    }
//#endif

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (!hasFavorites && index >= 1) index++;

            switch (index) {
                case 0: {
                    App.openGuildSelector(true, false);
                    break;
                }
                case 1: {
                    FavoriteGuilds.openSelector(true, false);
                    break;
                }
                case 2: {
                    App.openDMSelector(true, false);
                    break;
                }
                case 3: {
                    App.disp.setCurrent(new SettingsScreen());
                    break;
                }
                case 4: {
//#ifdef OLD_ABOUT_SCREEN
                    App.disp.setCurrent(new AboutForm());
//#else
                    App.disp.setCurrent(new AboutScreen());
//#endif
                    break;
                }
                case 5: {
                    if (App.gateway != null) App.gateway.stop = true;
                    App.myUserId = null;
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
