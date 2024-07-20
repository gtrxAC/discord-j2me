package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ErrorAlert extends Alert implements CommandListener {
    private Display disp;
    Displayable next;
    
    public ErrorAlert(Display disp, String message, Displayable next) {
        super("Error");
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

    public void update(String message, Displayable next) {
        setString(message);
        if (next != null) this.next = next;
    }

    public void commandAction(Command c, Displayable d) {
        disp.setCurrent(next);
    }
}
