//#ifdef OVER_100KB
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
                    RecordStore.deleteRecordStore("theme");
                }
                catch (Exception e) {}
                try {
//#ifdef S60V2
                    try {
//#endif
                        rms = RecordStore.openRecordStore("theme", true, RecordStore.AUTHMODE_ANY, true);
//#ifdef S60V2
                    }
                    // Apparently S60v2 has a bug with public RMSes, though I didn't encounter this with my 6630 (V 2.39.15) or 3230 (V 5.0604.0)
                    // https://web.archive.org/web/20130706182721/http://www.developer.nokia.com/Community/Wiki/Archived:RecordStore.AUTHMODE_ANY_throws_NullPointerException_on_S60_2nd_Edition_(Known_Issue)
                    catch (NullPointerException e) {
                        rms = RecordStore.openRecordStore("theme", true);
                    }
//#endif
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
//#endif