//#ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class UploadWarningDialog extends Dialog implements Strings, CommandListener {
    Object lastScreen;
    private Command okCommand;
    private Command hideCommand;
    private Command cancelCommand;
    Message recipientMsg;

    UploadWarningDialog(Message recipientMsg) {
        super(Locale.get(UPLOAD_WARNING_TITLE), Locale.get(UPLOAD_WARNING_DESCRIPTION));
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();
        this.recipientMsg = recipientMsg;

        okCommand = Locale.createCommand(OK, Command.OK, 0);
        hideCommand = Locale.createCommand(HIDE, Command.OK, 1);
        cancelCommand = Locale.createCommand(CANCEL, Command.BACK, 0);
        addCommand(okCommand);
        addCommand(hideCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cancelCommand) {
            App.disp.setCurrent(lastScreen);
        } else {
            if (c == hideCommand) {
                Settings.hasSeenUploadWarning = true;
                Settings.save();
            }
            if (!Settings.nativeFilePicker) {
                App.openChannelView(false);
            }
            App.channelView.uploadFile(recipientMsg);
        }
    }
}
//#endif