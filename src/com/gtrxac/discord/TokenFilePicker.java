package com.gtrxac.discord;

import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import java.io.*;

public class TokenFilePicker extends FilePicker {
    public TokenFilePicker() {
        this("file:///");
    }

    // ifdef SAMSUNG_FULL
    // recreate this screen for 480p font fix (see MyCanvas and Util)
    protected MyCanvas reload() {
        TokenFilePicker result = new TokenFilePicker();
        result.lastScreen = lastScreen;
        return result;
    }
    // endif

    protected TokenFilePicker(String currentPath) {
        super(Locale.get(TOKEN_PICKER_TITLE), currentPath);
    }

    protected void directorySelected(String selectedPath) {
        App.disp.setCurrent(new TokenFilePicker(selectedPath));
    }

    protected boolean fileFilter(String fileName) {
        return Util.indexOfAny(fileName, Attachment.nonTextFormats, 0) == -1 &&
               Util.indexOfAny(fileName, Attachment.audioFormats, 0) == -1;
    }
    
    protected void fileSelected(FileConnection fc, String selected) {
        InputStream is = null;
        try {
            int size = (int) fc.fileSize();
            is = fc.openInputStream();
            Settings.token = Util.bytesToString(Util.readBytes(is, size, 1024, 2048)).trim();
            Settings.token = Util.stringToLength(Settings.token, 200);
            close();
        }
        catch (Exception e) {
            App.error(e);
        }
        try { is.close(); } catch (Exception e) {}
        try { fc.close(); } catch (Exception e) {}
    }
}