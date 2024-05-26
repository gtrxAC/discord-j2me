package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class GatewayAlert extends Alert implements CommandListener {
    State s;
    Displayable next;
    Command yesCommand;
    Command noCommand;
    
    public GatewayAlert(State s, String message) {
        super("Disconnected");
        setString("Disconnected from gateway. Do you want to reconnect?");
        if (message != null && message.length() > 0) setString(getString() + " Reason: " + message);
        setCommandListener(this);

        this.s = s;
        this.next = s.disp.getCurrent();

        yesCommand = new Command("Yes", Command.OK, 0);
        noCommand = new Command("No", Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            s.gateway = new GatewayThread(s, s.gateway.gateway, s.gateway.token);
            s.gateway.start();
        }
        s.disp.setCurrent(next);
    }
}
