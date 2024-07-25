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
                s.disp.setCurrent(new LoginForm(s));
            } else {
                LoginSettings sett = new LoginSettings(s);
                s.login(sett.api, sett.gateway, sett.cdn, sett.token);
            }
            started = true;
        }
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}
}
