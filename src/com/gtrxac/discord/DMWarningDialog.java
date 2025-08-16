//#ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class DMWarningDialog extends Dialog implements Strings, CommandListener {
    private Command yesCommand;
    private Command noCommand;

    DMWarningDialog() {
        super(Locale.get(UPLOAD_WARNING_TITLE), Locale.get(DM_WARNING_DESCRIPTION));
        setCommandListener(this);

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 0);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            App.disp.setCurrent(new MessageBox());
        } else {
            App.openChannelView(false);
        }
    }
}
//#endif