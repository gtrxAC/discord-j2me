// ifdef OVER_100KB
package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class DataManagerScreen extends ListScreen implements CommandListener, Strings {
    private Command promptYesCommand;
    private Command promptNoCommand;

    private Displayable lastScreen;
    private State s;
    private Vector rmsNames;

    DataManagerScreen(State s) {
        super(Locale.get(DATA_MANAGER_TITLE), true, false, true);
        setCommandListener(this);
        lastScreen = s.disp.getCurrent();
        this.s = s;
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
            append(Locale.get(titleKey), info, s.ic.pfpNone, null);
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
    }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            s.disp.setCurrent(lastScreen);
        }
        else if (c == SELECT_COMMAND) {
            Dialog dialog = new Dialog(s.disp, Locale.get(DATA_MANAGER_PROMPT_TITLE), Locale.get(DATA_MANAGER_PROMPT) + getString(getSelectedIndex()));
            dialog.addCommand(promptYesCommand);
            dialog.addCommand(promptNoCommand);
            dialog.setCommandListener(this);
            s.disp.setCurrent(dialog);
        }
        else {
            if (c == promptYesCommand) {
                String rmsName = (String) rmsNames.elementAt(getSelectedIndex());
                try {
                    RecordStore.deleteRecordStore(rmsName);
                    refresh();

                    if ("lang".equals(rmsName)) {
                        s.language = "en";
                        Locale.setLanguage(s);
                        LoginSettings.save(s);
            
                        // Clear servers/DMs (so the lists get refreshed, which in turn updates the softkey labels)
                        s.guilds = null;
                        s.dmChannels = null;
                    }
                    else if ("unread".equals(rmsName)) {
                        UnreadManager.init(s);
                    }
                    else if ("emoji".equals(rmsName)) {
                        // Make sure the emojis get re-downloaded if they are needed again
                        s.myUserId = null;
                    }
                }
                catch (Exception e) {
                    s.error(e);
                    return;
                }
            }
            s.disp.setCurrent(this);
        }
    }
}
// endif