package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AttachmentView extends Form implements CommandListener {
    State s;
    Message msg;

    private Command backCommand;

    public AttachmentView(State s, Message msg) {
        super("Attachments");
        this.s = s;
        this.msg = msg;
        setCommandListener(this);

        backCommand = new Command("Back", Command.BACK, 0);
        addCommand(backCommand);

        new HTTPThread(s, HTTPThread.FETCH_ATTACHMENTS).start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.openChannelView(false);
        }
    }
}
