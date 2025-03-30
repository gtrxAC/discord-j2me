// ifdef OVER_100KB
package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class DataManagerScreen extends ListScreen implements CommandListener, Strings {
    private Command promptYesCommand;
    private Command promptNoCommand;

    private Displayable lastScreen;
    private Vector rmsNames;

    DataManagerScreen() {
        super(Locale.get(DATA_MANAGER_TITLE), true, false, true);
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();
        rmsNames = new Vector();
        refresh();

        promptYesCommand = Locale.createCommand(YES, Command.ITEM, 0);
        promptNoCommand = Locale.createCommand(NO, Command.BACK, 1);
    }

    private void addItem(int titleKey, String rmsName, boolean showFirstRecord) {
        RecordStore rms = null;

        try {
            rms = RecordStore.openRecordStore(rmsName, false);
            String size = Attachment.fileSizeToString(rms.getSize());
            String info;
            if (showFirstRecord) {
                info = Util.bytesToString(rms.getRecord(1)) + Locale.get(LEFT_PAREN) + size + Locale.get(RIGHT_PAREN);
            } else {
                info = size;
            }
            append(Locale.get(titleKey), info, App.ic.pfpNone, null);
            rmsNames.addElement(rmsName);
        }
        catch (Exception e) {}
        
        Util.closeRecordStore(rms);
    }

    private void refresh() {
        deleteAll();
        addItem(DATA_MANAGER_EMOJI, "emoji", false);
        addItem(DATA_MANAGER_LAST_READ, "unread", false);
        addItem(DATA_MANAGER_NOTIF_SOUND, "notifsound", true);
        addItem(DATA_MANAGER_LANGUAGE, "lang", true);
        // addItem(DATA_MANAGER_LANGUAGE, "theme", false);
        addItem(0, "theme", false);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            App.disp.setCurrent(lastScreen);
        }
        else if (c == SELECT_COMMAND) {
            Dialog dialog = new Dialog(Locale.get(DATA_MANAGER_PROMPT_TITLE), Locale.get(DATA_MANAGER_PROMPT) + getString(getSelectedIndex()));
            dialog.addCommand(promptYesCommand);
            dialog.addCommand(promptNoCommand);
            dialog.setCommandListener(this);
            App.disp.setCurrent(dialog);
        }
        else {
            if (c == promptYesCommand) {
                String rmsName = (String) rmsNames.elementAt(getSelectedIndex());
                try {
                    RecordStore.deleteRecordStore(rmsName);
                    refresh();

                    if ("lang".equals(rmsName)) {
                        Settings.language = "en";
                        Locale.setLanguage();
                        Settings.save();
            
                        // Clear servers/DMs (so the lists get refreshed, which in turn updates the softkey labels)
                        App.guilds = null;
                        App.dmChannels = null;
                    }
                    else if ("unread".equals(rmsName)) {
                        UnreadManager.init();
                    }
                    else if ("emoji".equals(rmsName)) {
                        // Make sure the emojis get re-downloaded if they are needed again
                        App.myUserId = null;
                    }
                }
                catch (Exception e) {
                    App.error(e);
                    return;
                }
            }
            App.disp.setCurrent(this);
        }
    }
}
// endif