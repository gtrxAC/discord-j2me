package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class DeleteConfirmAlert extends Alert implements CommandListener {
    State s;
    Message msg;
    Command yesCommand;
    Command noCommand;
    
    public DeleteConfirmAlert(State s, Message msg) {
        super("Delete");
        setString("Delete this message?");

        String content = (msg.content.length() >= 30) ?
            msg.content.substring(0, 27) + "..." :
            msg.content;

        setString("Delete this message? \r\n\"" + content + '"');

        setCommandListener(this);
        this.s = s;
        this.msg = msg;

        yesCommand = new Command("Yes", Command.OK, 0);
        noCommand = new Command("No", Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        s.openChannelView(false);
        
        if (c == yesCommand) {
            HTTPThread h = new HTTPThread(s, HTTPThread.DELETE_MESSAGE);
            h.editMessage = msg;
            h.start();
        }
    }
}
