package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReconnectForm extends Form implements CommandListener, Strings {
    private State s;
    private Command yesCommand;
    private Command noCommand;
    private Displayable lastScreen;
    
    public ReconnectForm(State s, String message) {
        super(Locale.get(RECONNECT_FORM_TITLE));
        setCommandListener(this);
        this.s = s;

        lastScreen = s.disp.getCurrent();
        if (lastScreen instanceof ErrorAlert) {
            lastScreen = ((ErrorAlert) lastScreen).next;
        }

        append(new StringItem(null, Locale.get(RECONNECT_FORM_TEXT)));

        if (message != null && message.length() > 0) {
            append(new StringItem(Locale.get(RECONNECT_FORM_MESSAGE), message));
        }

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            s.gateway = new GatewayThread(s);
            s.gateway.start();
        }
        s.disp.setCurrent(lastScreen);
    }
}
