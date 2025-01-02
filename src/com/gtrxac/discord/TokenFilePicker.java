package com.gtrxac.discord;

import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import java.io.*;

public class TokenFilePicker extends FilePicker {
    public TokenFilePicker(State s) {
        this(s, "file:///");
    }

    protected TokenFilePicker(State s, String currentPath) {
        super(s, Locale.get(TOKEN_PICKER_TITLE), currentPath);
    }

    protected void directorySelected(String selectedPath) {
        s.disp.setCurrent(new TokenFilePicker(s, selectedPath));
    }

    protected boolean fileFilter(String fileName) {
        return fileName.endsWith(".txt") || fileName.indexOf('.') == -1;
    }
    
    protected void fileSelected(FileConnection fc, String selected) {
        InputStream is = null;
        try {
            int size = (int) fc.fileSize();
            is = fc.openInputStream();
            s.token = Util.bytesToString(Util.readBytes(is, size, 1024, 2048)).trim();
            s.token = Util.stringToLength(s.token, 200);
            close();
        }
        catch (Exception e) {
            s.error(e);
        }
        try { is.close(); } catch (Exception e) {}
        try { fc.close(); } catch (Exception e) {}
    }
}