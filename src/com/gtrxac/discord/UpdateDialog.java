//#ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class UpdateDialog extends Dialog implements CommandListener, Strings {
    private Command updateCommand;
    private Command closeCommand;

    private boolean isBeta;
    
    public UpdateDialog(String latestVersion, String latestChangelog, boolean isBeta) {
        super(Locale.get(UPDATE_AVAILABLE_TITLE), "");
        setCommandListener(this);
        this.isBeta = isBeta;

        StringBuffer text = new StringBuffer();

        text.append(Locale.get(UPDATE_AVAILABLE))
            .append(App.VERSION_NAME)
            .append(Locale.get(UPDATE_AVAILABLE_LATEST))
            .append(latestVersion);

        if (latestChangelog != null) {
            text.append("\n\nWhat's new?\n")
                .append(latestChangelog);
        }

        setString(text.toString());

        updateCommand = Locale.createCommand(UPDATE, Command.OK, 1);
        closeCommand = Locale.createCommand(CLOSE, Command.BACK, 0);
        addCommand(updateCommand);
        addCommand(closeCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == updateCommand) {
            StringBuffer target = new StringBuffer();
            String format = ".jad";
//#ifdef KEMULATOR
            format = ".jar";
//#endif
//#ifdef J2ME_LOADER
            format = ".jar";
//#endif

            target.append(Settings.api)
                .append("/discord_")
                .append(App.VERSION_VARIANT);
            if (isBeta) target.append("_beta");
            target.append(format);

            App.platRequest(target.toString());
        }
        else if (c == closeCommand) {
            App.disp.setCurrent(MainMenu.get(false));
        }
    }
}
//#endif