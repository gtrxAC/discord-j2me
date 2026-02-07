package com.gtrxac.discord;

import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.io.*;

public class SoundFilePicker extends FilePicker {
    int type; 

    public SoundFilePicker(int type) {
        this("file:///", type);
    }

    protected SoundFilePicker(String currentPath, int type) {
        super("Select sound", currentPath);
        this.type = type;
    }

    protected void directorySelected(String selectedPath) {
        App.disp.setCurrent(new SoundFilePicker(selectedPath, type));
    }

    protected boolean fileFilter(String fileName) {
        return Util.indexOfAny(fileName, Attachment.audioFormats, 0) != -1;
    }
    
    protected void fileSelected(FileConnection fc, String selected) {
        InputStream is = null;
        try {
            int size = (int) fc.fileSize();
            is = fc.openInputStream();
            byte[] data = Util.readBytes(is, size, 1024, 2048);

            RecordStore rms = null;
            try {
                rms = RecordStore.openRecordStore(SoundSettingsScreen.rmsNames[type], true);
                Util.setOrAddRecord(rms, 1, selected);
                Util.setOrAddRecord(rms, 2, data);

                Settings.soundModes[type] = Settings.SOUND_CUSTOM;

                App.disp.setCurrent(SoundSettingsScreen.instance);
            }
            catch (Exception e) {
                App.error(e);
            }
            Util.closeRecordStore(rms);
        }
        catch (Exception e) {
            App.error(e);
        }
        try { is.close(); } catch (Exception e) {}
        try { fc.close(); } catch (Exception e) {}
    }
}