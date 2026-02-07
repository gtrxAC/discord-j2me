package com.gtrxac.discord;

import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.io.*;

public class BackgroundFilePicker extends FilePicker implements Strings {
    private static final String[] imageFormats = {
        ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".wbmp", ".webp", ".otb"
    };

    public BackgroundFilePicker() {
        this("file:///");
    }

    protected BackgroundFilePicker(String currentPath) {
        super(Locale.get(BACKGROUND_FILE_PICKER_TITLE), currentPath);
    }

    protected void directorySelected(String selectedPath) {
        App.disp.setCurrent(new BackgroundFilePicker(selectedPath));
    }

    protected boolean fileFilter(String fileName) {
        return Util.indexOfAny(fileName, imageFormats, 0) != -1;
    }
    
    protected void fileSelected(FileConnection fc, String selected) {
        InputStream is = null;
        try {
            int size = (int) fc.fileSize();
            is = fc.openInputStream();
            byte[] data = Util.readBytes(is, size, 1024, 2048);

            RecordStore rms = null;
            try {
                rms = RecordStore.openRecordStore("bgimage", true);
                Util.setOrAddRecord(rms, 1, selected);
                Util.setOrAddRecord(rms, 2, data);
                close();
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