package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageCopyBox extends TextBox implements CommandListener, Strings {
    private Command backCommand;
    public Displayable lastScreen;

    public MessageCopyBox(String content) {
        this(Locale.get(MESSAGE_COPY_BOX_TITLE), content);
    }

    public MessageCopyBox(String title, String content) {
        super(title, content, content.length(), 0);
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();

        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            App.disp.setCurrent(lastScreen);
        }
    }
}
