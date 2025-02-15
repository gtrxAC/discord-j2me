package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class DiscordMIDlet extends MIDlet {
    public static DiscordMIDlet instance;

    private boolean started = false;

    public DiscordMIDlet() {
        instance = this;
    }

    public void startApp() {
        if (!started) {
            App.disp = Display.getDisplay(this);
            Settings.load();

            // If token was not found in save file, go to login screen, else login and go to main menu
            if (Settings.token.trim().length() == 0) {
                // Theme and fonts need to be loaded so Dialog screens can be shown as part of the LoginForm
                App.loadTheme();
                App.loadFonts();
                App.disp.setCurrent(new LoginForm());
            } else {
                App.login();
            }
            started = true;
        }
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}
}
