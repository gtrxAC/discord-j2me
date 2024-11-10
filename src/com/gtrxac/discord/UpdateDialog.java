package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class UpdateDialog extends Dialog implements CommandListener, Strings {
    private State s;
    private Command updateCommand;
    private Command closeCommand;

    private Notification notif;
    
    public UpdateDialog(State s, String latestVersion) {
        super(s.disp, Locale.get(UPDATE_AVAILABLE_TITLE), "");
        setCommandListener(this);
        this.s = s;

        setString(
            Locale.get(UPDATE_AVAILABLE) +
            s.midlet.getAppProperty("MIDlet-Version") +
            Locale.get(UPDATE_AVAILABLE_LATEST) +
            latestVersion
        );

        updateCommand = Locale.createCommand(UPDATE, Command.OK, 1);
        closeCommand = Locale.createCommand(CLOSE, Command.BACK, 0);
        addCommand(updateCommand);
        addCommand(closeCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == updateCommand) {
            char format = (Util.isJ2MELoader || Util.isKemulator) ? 'r' : 'd';
            s.platformRequest(s.api + "/discord_midp2.ja" + format);
        }
        else if (c == closeCommand) {
            s.disp.setCurrent(MainMenu.get(null));
        }
    }
}
