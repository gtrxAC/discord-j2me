package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReconnectDialog extends Dialog implements CommandListener, Strings {
    private State s;
    private Command yesCommand;
    private Command noCommand;
    private Displayable lastScreen;
    
    public ReconnectDialog(State s, String message) {
        super(s.disp, Locale.get(RECONNECT_FORM_TITLE), "");
        setCommandListener(this);
        this.s = s;
        lastScreen = s.disp.getCurrent();

        StringBuffer sb = new StringBuffer(Locale.get(s.autoReConnect ? AUTO_RECONNECT_FAILED : RECONNECT_FORM_TEXT));
        if (message != null && message.length() > 0) {
            sb.append("\n");
            sb.append(Locale.get(RECONNECT_FORM_MESSAGE));
            sb.append(":\n");
            sb.append(message);
        }
        setString(sb.toString());

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
