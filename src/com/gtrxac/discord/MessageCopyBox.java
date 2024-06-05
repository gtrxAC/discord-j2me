package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageCopyBox extends TextBox implements CommandListener {
    State s;
    private Command backCommand;

    public MessageCopyBox(State s, String content) {
        super("Copy message", content, 2000, 0);
        setCommandListener(this);
        this.s = s;

        backCommand = new Command("Back", Command.BACK, 0);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.openChannelView(false);
        }
    }
}
