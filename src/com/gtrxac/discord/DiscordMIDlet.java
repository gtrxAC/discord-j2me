package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class DiscordMIDlet extends MIDlet {
    private boolean started = false;
    private static State s;

    public DiscordMIDlet() {
        started = false;
    }

    public void startApp() {
        if (!started) {
            s = new State();
            s.midlet = this;
            s.disp = Display.getDisplay(this);

            boolean haveLoginRms = true;
            try {
                RecordStore.openRecordStore("login", false).closeRecordStore();
            }
            catch (Exception e) {
                haveLoginRms = false;
            }

            if (!haveLoginRms && getAppProperty("Token") == null) {
                // Theme and fonts need to be loaded so Dialog screens can be shown as part of the LoginForm
                s.loadTheme();
                s.loadFonts();
                s.disp.setCurrent(new LoginForm(s));
            } else {
                LoginSettings.load(s);
                s.login();
            }
            started = true;
        }
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}
}
