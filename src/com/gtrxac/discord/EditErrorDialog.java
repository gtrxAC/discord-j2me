//#ifdef PROXYLESS_SUPPORT
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class EditErrorDialog extends Dialog implements Strings, CommandListener {
    Displayable lastScreen;
    private Command yesCommand;
    private Command noCommand;
    private Command hideCommand;

    Message editMessage;
    String editContent;

    // for editing message (with editcontent)
    EditErrorDialog(Message editMessage, String editContent) {
        this(editMessage);
        setString("Your device does not support editing messages in Direct connection mode.\nDo you want to edit this message via the proxy?\nSelect 'Hide' to always use the proxy.");
        this.editContent = editContent;
    }

    // for deleting message (no editcontent)
    EditErrorDialog(Message editMessage) {
        super(Locale.get(ERROR_TITLE), "Your device does not support deleting messages in Direct connection mode.\nDo you want to delete this message via the proxy?\nSelect 'Hide' to always use the proxy.");
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();
        this.editMessage = editMessage;

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 0);
        hideCommand = Locale.createCommand(HIDE, Command.OK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
        addCommand(hideCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c != noCommand) {
            if (c == hideCommand) {
                Settings.hasSeenEditError = true;
                Settings.save();
            }
            HTTPThread h = new HTTPThread((editContent != null) ? HTTPThread.EDIT_MESSAGE : HTTPThread.DELETE_MESSAGE);
            h.editMessage = editMessage;
            h.editContent = editContent;
            h.forceProxy = true;
            h.start();
            App.openChannelView(false);
        } else {
            App.disp.setCurrent(lastScreen);
        }
    }
}
//#endif