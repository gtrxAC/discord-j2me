package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.util.*;
import java.io.*;

public abstract class FilePicker extends ListScreen implements CommandListener, Strings {
    private Command closeCommand;
    private String currentPath;
    public Object lastScreen;

    public FilePicker(String title) {
        this(title, "file:///");
    }

    protected FilePicker(String title, String currentPath) {
        super(title, List.IMPLICIT);
        this.lastScreen = App.disp.getCurrent();
        setCommandListener(this);

        closeCommand = Locale.createCommand(CLOSE, Command.BACK, 2);
        addCommand(closeCommand);

        this.currentPath = currentPath;
        listFiles();
    }

    protected boolean fileFilter(String fileName) {
        // show all files by default
        return true;
    }

    protected abstract void directorySelected(String selectedPath);

    protected abstract void fileSelected(FileConnection fc, String selected);

    protected void close() {
        Object last = lastScreen;
        while (last instanceof FilePicker) {
            last = ((FilePicker) last).lastScreen;
        }
        App.disp.setCurrent(last);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (index >= 0) {
                String selected = getString(index);
                String selectedPath = currentPath + selected;

                try {
                    if (selected.endsWith("/")) { // Directory
                        directorySelected(selectedPath);
                    } else { // File
                        FileConnection fc = (FileConnection) Connector.open(selectedPath, Connector.READ);
                        fileSelected(fc, selected);
                    }
                }
                catch (Exception e) {
                    App.error(e);
                }
            }
        }
        else if (c == BACK_COMMAND) {
            if (!currentPath.equals("file:///")) {
                int lastSlashIndex = currentPath.lastIndexOf('/', currentPath.length() - 2);
                if (lastSlashIndex != -1) {
                    App.disp.setCurrent(lastScreen);
                }
            } else {
                close();
            }
        }
        else if (c == closeCommand) {
            close();
        }
    }

    /**
     * Get the alphabetically first string from the vector and remove it from the vector.
     */
    private static String getFirstString(Vector v) {
        String result = null;
        for (int i = 0; i < v.size(); i++) {
            String cur = (String) v.elementAt(i);
            if (result == null || cur.compareTo(result) < 0) {
                result = cur;
            }
        }
        v.removeElement(result);
        return result;
    }

    private void listFiles() {
        try {
            if (currentPath.equals("file:///")) {
                Enumeration roots = FileSystemRegistry.listRoots();
                while (roots.hasMoreElements()) {
                    String root = (String) roots.nextElement();
                    append(root, null);
                }
            } else {
                FileConnection fc = (FileConnection) Connector.open(currentPath, Connector.READ);
                Enumeration list = fc.list();

                // Add items in alphabetical order, directories first
                Vector dirs = new Vector();
                Vector files = new Vector();

                while (list.hasMoreElements()) {
                    String fileName = (String) list.nextElement();
                    if (fileName.endsWith("/")) {
                        dirs.addElement(fileName);
                    }
                    else if (fileFilter(fileName)) {
                        files.addElement(fileName);
                    }
                }
                while (!dirs.isEmpty()) {
                    append(getFirstString(dirs), null);
                }
                while (!files.isEmpty()) {
                    append(getFirstString(files), null);
                }
                fc.close();
            }
        }
        catch (IOException e) {
            App.error(e);
        }
    }
}