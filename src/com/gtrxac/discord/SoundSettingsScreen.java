package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class SoundSettingsScreen extends ListScreen implements CommandListener, Strings {
    public static SoundSettingsScreen instance;
    private Object lastScreen;

    public static final String[] rmsNames = {
        "notifsound", "insound", "outsound"
    };

    public static final int NOTIFICATION_SOUND = 0;
    public static final int INCOMING_SOUND = 1;
    public static final int OUTGOING_SOUND = 2;

    SoundSettingsScreen() {
        super("Sounds", true, false, true);
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();
        instance = this;
    }

    private void add(int index, String title) {
        String rightItem = null;

        switch (Settings.soundModes[index]) {
            case Settings.SOUND_OFF: {
                rightItem = Locale.get(SETTING_VALUE_OFF);
                break;
            }
            case Settings.SOUND_BEEP: {
                rightItem = "Beep";
                break;
            }
            case Settings.SOUND_DEFAULT: {
                rightItem = "Excellence";
                break;
            }
            case Settings.SOUND_CUSTOM: {
                RecordStore rms = null;
                try {
                    rms = RecordStore.openRecordStore(rmsNames[index], false);
                    rightItem = Util.bytesToString(rms.getRecord(1));
                }
                catch (Exception e) {
                    rightItem = Locale.get(THEME_CUSTOM);
                }
            
                Util.closeRecordStore(rms);
                break;
            }
        }
        append(title, rightItem, App.ic.notifySound, null);
    }

    public void showNotify() {
        deleteAll();
        add(NOTIFICATION_SOUND, "Notification");
        add(INCOMING_SOUND, "Incoming message");
        add(OUTGOING_SOUND, "Outgoing message");
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            App.disp.setCurrent(lastScreen);
            instance = null;
        }
        else if (c == SELECT_COMMAND) {
            App.disp.setCurrent(new SoundSelectorScreen(getSelectedIndex()));
        }
    }
}