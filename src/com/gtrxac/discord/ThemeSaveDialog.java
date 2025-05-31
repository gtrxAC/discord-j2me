// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class ThemeSaveDialog extends Dialog implements Strings {
    private Command yesCommand;
    private Command noCommand;
    private Command cancelCommand;

    ThemeSaveDialog() {
        super(Locale.get(THEME_SAVE_TITLE), Locale.get(THEME_SAVE_DESCRIPTION));
        setCommandListener(this);

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        cancelCommand = Locale.createCommand(CANCEL, Command.BACK, 2);
        addCommand(yesCommand);
        addCommand(noCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cancelCommand) {
            // cancel -> go back to channel view so the theme can be previewed again
            App.openChannelView(false);
        }
        else {
            if (c == yesCommand) {
                // yes -> set theme to custom and save this theme's data
                RecordStore rms = null;
                try {
                    rms = RecordStore.openRecordStore("theme", true);
                    Util.setOrAddRecord(rms, 1, Util.stringToBytes(App.channelView.pendingTheme.build()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                Util.closeRecordStore(rms);

                Settings.theme = Theme.CUSTOM;
                Settings.save();
            } else {
                // no -> load previous theme
                Theme.load();
            }
            // yes or no -> close the channel view
            App.channelView.pendingTheme = null;
            App.channelView.commandAction(App.channelView.backCommand, App.channelView);
        }
    }
}
// endif