package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class DeleteConfirmAlert extends Alert implements CommandListener, Strings {
    State s;
    Message msg;
    Command yesCommand;
    Command noCommand;
    
    public DeleteConfirmAlert(State s, Message msg) {
        super(Locale.get(DELETE_CONFIRM_TITLE));

        String content = Util.stringToLength(msg.content, 30);

        setString(Locale.get(DELETE_CONFIRM_TEXT) + " \r\n\"" + content + '"');

        setCommandListener(this);
        this.s = s;
        this.msg = msg;

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
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
