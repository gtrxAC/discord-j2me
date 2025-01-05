package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class DiscordMIDlet extends MIDlet {
    private boolean started = false;
    private static State s;

    public DiscordMIDlet() {}

    public void startApp() {
        if (!started) {
            s = new State();
            s.midlet = this;
            s.disp = Display.getDisplay(this);
            LoginSettings.load(s);

            // If token was not found in save file, go to login screen, else login and go to main menu
            if (s.token.trim().length() == 0) {
                // Theme and fonts need to be loaded so Dialog screens can be shown as part of the LoginForm
                s.loadTheme();
                s.loadFonts();
                s.disp.setCurrent(new LoginForm(s));
            } else {
                s.login();
            }
            started = true;
        }
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}
}
