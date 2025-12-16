package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class SoundSelectorScreen extends ListScreen implements CommandListener, Strings {
    public SoundSettingsScreen lastScreen;
    private boolean haveRms = false;
    private int type;

    SoundSelectorScreen(int type) {
        super("Sounds", true, false, false);
        setCommandListener(this);
        lastScreen = (SoundSettingsScreen) App.disp.getCurrent();
        this.type = type;

        append("Off", null);
        append("Beep", null);
        append("Excellence", null);
        
        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore(SoundSettingsScreen.rmsNames[type], false);
            haveRms = true;
            append(Util.bytesToString(rms.getRecord(1)), null);
        }
        catch (Exception e) {}
    
        Util.closeRecordStore(rms);

        append("Import file", null);

        setSelectedIndex(Settings.soundModes[type], true);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            App.disp.setCurrent(lastScreen);
        }
        else if (c == SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (index > 2 && !haveRms) index++;

            if (index == 4) {
                App.disp.setCurrent(new SoundFilePicker(type));
            } else {
                Settings.soundModes[type] = index;
                App.disp.setCurrent(lastScreen);
            }
        }
    }
}