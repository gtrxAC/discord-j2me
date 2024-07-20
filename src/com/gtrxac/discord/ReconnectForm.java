package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReconnectForm extends Form implements CommandListener {
    private State s;
    private Command yesCommand;
    private Command noCommand;
    private Displayable lastScreen;
    
    public ReconnectForm(State s, String message) {
        super("Disconnected");
        setCommandListener(this);
        this.s = s;

        lastScreen = s.disp.getCurrent();
        if (lastScreen instanceof ErrorAlert) {
            lastScreen = ((ErrorAlert) lastScreen).next;
        }

        append(new StringItem(null, "Gateway error. Do you want to reconnect?"));

        if (message != null && message.length() > 0) {
            append(new StringItem("Message", message));
        }

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
        s.disp.setCurrent(lastScreen);
    }
}
