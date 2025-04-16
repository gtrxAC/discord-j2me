// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class UpdateDialog extends Dialog implements CommandListener, Strings {
    private Command updateCommand;
    private Command closeCommand;

    private boolean isBeta;
    
    public UpdateDialog(String latestVersion, boolean isBeta) {
        super(Locale.get(UPDATE_AVAILABLE_TITLE), "");
        setCommandListener(this);
        this.isBeta = isBeta;

        setString(
            Locale.get(UPDATE_AVAILABLE) +
            App.VERSION_NAME +
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

            target.append(Settings.api);
            target.append("/discord_");
            target.append(App.VERSION_VARIANT);
            if (isBeta) target.append("_beta");
            target.append(format);

            App.platRequest(target.toString());
        }
        else if (c == closeCommand) {
            App.disp.setCurrent(MainMenu.get(false));
        }
    }
}
// endif