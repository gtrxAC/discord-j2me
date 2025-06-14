package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MainMenu extends ListScreen implements CommandListener, Strings {
    private Command quitCommand;
    private static boolean hasFavorites;
    // ifdef SAMSUNG_FULL
    private static boolean hasDoneSamsungFontFix;
    // endif

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

    // ifdef SAMSUNG_FULL
    protected void showNotify() {
        // On Samsung Jet S8000 (tested with S800MCEIK1 firmware) the first canvas that is shown
        // in a Java app will have fonts that are way too small (approx 16px on a 480p display).
        // The solution is to reload the fonts and the main menu.
        // More about this in Util.java
        if (Util.hasSamsungFontBug && !hasDoneSamsungFontFix) {
            App.loadFonts();
            App.disp.setCurrent(get(true));
            hasDoneSamsungFontFix = true;
        } else {
            super.showNotify();
        }
    }
    // endif

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
                    App.disp.setCurrent(new AboutForm());
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
            DiscordMIDlet.instance.notifyDestroyed();
        }
    }
}
