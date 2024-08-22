package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageCopyBox extends TextBox implements CommandListener, Strings {
    private State s;
    private Command backCommand;
    public Displayable lastScreen;

    public MessageCopyBox(State s, String content) {
        this(s, Locale.get(MESSAGE_COPY_BOX_TITLE), content);
    }

    public MessageCopyBox(State s, String title, String content) {
        super(title, content, content.length(), 0);
        setCommandListener(this);
        this.s = s;
        lastScreen = s.disp.getCurrent();

        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(lastScreen);
        }
    }
}
