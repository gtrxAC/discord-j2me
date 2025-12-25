//#ifdef PROXYLESS_SUPPORT
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class GatewayWarningDialog extends Dialog implements Strings, CommandListener, Runnable {
    Object lastScreen;
    private Command yesCommand;
    private Command hideCommand;
    private Command noCommand;

    GatewayWarningDialog() {
        super(Locale.get(UPLOAD_WARNING_TITLE), Locale.get(GATEWAY_WARNING_DESCRIPTION));
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        hideCommand = Locale.createCommand(HIDE, Command.OK, 1);
        noCommand = Locale.createCommand(NO, Command.BACK, 0);
        addCommand(yesCommand);
        addCommand(hideCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c != noCommand) {
            if (c == hideCommand) {
                Settings.hasSeenGatewayWarning = true;
                Settings.save();
            }
            App.hasSeenGatewayWarningTemp = true;
            App.startGateway();
        }
        App.disp.setCurrent(lastScreen);
    }

    public void run() {
        // Wait until main menu is shown, then show dialog
        while (!(App.disp.getCurrent() instanceof MainMenu)) {
            Util.sleep(10);
        }
        App.disp.setCurrent(this);
    }
}
//#endif