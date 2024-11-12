package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class UpdateDialog extends Dialog implements CommandListener, Strings {
    private State s;
    private Command updateCommand;
    private Command closeCommand;

    private Notification notif;
    private boolean isBeta;
    
    public UpdateDialog(State s, String latestVersion, boolean isBeta) {
        super(s.disp, Locale.get(UPDATE_AVAILABLE_TITLE), "");
        setCommandListener(this);
        this.s = s;
        this.isBeta = isBeta;

        setString(
            Locale.get(UPDATE_AVAILABLE) +
            State.VERSION_NAME +
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
            StringBuffer target = new StringBuffer();
            String format = ".jad";
            // ifdef MIDP2_GENERIC
            if (Util.isKemulator) format = ".jar";
            // endif
            // ifdef J2ME_LOADER
            format = ".jar";
            // endif

            target.append(s.api);
            target.append("/discord_");
            target.append(State.VERSION_VARIANT);
            if (isBeta) target.append("_beta");
            target.append(format);

            s.platformRequest(target.toString());
        }
        else if (c == closeCommand) {
            s.disp.setCurrent(MainMenu.get(null));
        }
    }
}
