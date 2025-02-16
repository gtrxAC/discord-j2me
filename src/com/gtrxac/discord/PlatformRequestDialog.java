// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class PlatformRequestDialog extends Dialog implements Strings {
    private Command yesCommand;
    private Command noCommand;

    public PlatformRequestDialog() {
        super(Locale.get(PLAT_REQUEST_DIALOG_TITLE), Locale.get(PLAT_REQUEST_DIALOG_TEXT));
        setCommandListener(this);

        yesCommand = Locale.createCommand(YES, Command.OK, 1);
        noCommand = Locale.createCommand(NO, Command.BACK, 0);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            DiscordMIDlet.instance.notifyDestroyed();
        } else {
            App.disp.setCurrent(lastScreen);
        }
    }
}
// endif