package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageEditBox extends TextBox implements CommandListener, Strings {
    private State s;
    private Message msg;
    private Command sendCommand;
    private Command backCommand;

    public MessageEditBox(State s, Message msg) {
        super(Locale.get(MESSAGE_EDIT_BOX_TITLE), msg.rawContent, 2000, 0);
        setCommandListener(this);
        this.s = s;
        this.msg = msg;

        sendCommand = Locale.createCommand(OK, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);

        addCommand(sendCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        s.openChannelView(false);
        
        if (c == sendCommand) {
            HTTPThread h = new HTTPThread(s, HTTPThread.EDIT_MESSAGE);
            h.editMessage = msg;
            h.editContent = getString();
            h.start();
        }
    }
}
