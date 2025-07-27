// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReconnectDialog extends Dialog implements CommandListener, Strings {
    private Command yesCommand;
    private Command noCommand;
    private Displayable lastScreen;
    
    public ReconnectDialog(String message) {
        super(Locale.get(RECONNECT_FORM_TITLE), "");
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();
        // ifdef J2ME_LOADER
        // On J2ME Loader, the current screen may be null if the gateway disconnect occurred when the app was in the background
        if (lastScreen == null) lastScreen = MainMenu.get(false);
        // endif

        StringBuffer sb = new StringBuffer(Locale.get(Settings.autoReConnect ? AUTO_RECONNECT_FAILED : RECONNECT_FORM_TEXT));
        if (message != null && message.length() > 0) {
            sb.append("\n");
            sb.append(Locale.get(RECONNECT_FORM_MESSAGE));
            sb.append(":\n");
            sb.append(message);
        }
        setString(sb.toString());

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            App.gateway = new GatewayThread();
            App.gateway.start();
        }
        App.disp.setCurrent(lastScreen);
    }
}
// endif
