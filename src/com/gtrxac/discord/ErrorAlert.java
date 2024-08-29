package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ErrorAlert extends Alert implements CommandListener, Strings {
    private Display disp;
    Displayable next;
    
    public ErrorAlert(Display disp, String title, String message, Displayable next) {
        super(title);
        this.disp = disp;

        setString(message);
        setCommandListener(this);
        setTimeout(Alert.FOREVER);

        if (next != null) {
            this.next = next;
        } else {
            this.next = disp.getCurrent();
        }
    }

    public void update(String title, String message, Displayable next) {
        setString(message);
        if (title != null) setTitle(title);
        if (next != null) this.next = next;
    }

    public void commandAction(Command c, Displayable d) {
        disp.setCurrent(next);
    }
}
