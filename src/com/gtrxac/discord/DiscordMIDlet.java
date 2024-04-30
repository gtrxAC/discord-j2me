package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class DiscordMIDlet extends MIDlet {
    private boolean started = false;
    private static State s;

    public DiscordMIDlet() {
        started = false;
    }

    public void startApp() {
        if (!started) {
            s = new State();
            s.disp = Display.getDisplay(this);
            s.disp.setCurrent(new LoginForm(s));
            started = true;
        }
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}
}
