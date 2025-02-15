// ifdef SAMSUNG_100KB
package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.util.*;
import java.io.*;

public class AttachmentPickerOld extends ListScreen implements CommandListener, Strings {
    private Command closeCommand;
    private String currentPath;
    private Displayable lastScreen;
    private Message recipientMsg;

    public AttachmentPickerOld(Message recipientMsg) {
        this(recipientMsg, "file:///");
    }

    private AttachmentPickerOld(Message recipientMsg, String currentPath) {
        super(Locale.get(ATTACHMENT_PICKER_TITLE), List.IMPLICIT);
        this.recipientMsg = recipientMsg;
        this.lastScreen = App.disp.getCurrent();
        setCommandListener(this);

        closeCommand = Locale.createCommand(CLOSE, Command.BACK, 2);
        addCommand(closeCommand);

        this.currentPath = currentPath;
        listFiles();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (index >= 0) {
                String selected = getString(index);
                String selectedPath = currentPath + selected;

                if (selected.endsWith("/")) { // Directory
                    App.disp.setCurrent(new AttachmentPickerOld(recipientMsg, selectedPath));
                } else { // File
                    FileConnection fc;
                    try {
                        fc = (FileConnection) Connector.open(selectedPath, Connector.READ);
                    }
                    catch (Exception e) {
                        App.error(e);
                        return;
                    }
                    // ifdef OVER_100KB
                    try {
                        if (!Settings.useFilePreview) throw new Exception();
                        // Try to show image preview (fails for non-image files)
                        App.disp.setCurrent(new ImagePreviewScreen(recipientMsg, selected, fc));
                    }
                    catch (Exception e) {
                    // endif
                        // File is probably not an image, or viewing it is unsupported by the OS, or the user disabled file previews.
                        // Attach the file directly without previewing, and show the appropriate message text entry screen (normal message box or reply form).
                        App.disp.setCurrent(App.createTextEntryScreen(recipientMsg, selected, fc));
                    // ifdef OVER_100KB
                    }
                    catch (OutOfMemoryError e) {
                        App.error(Locale.get(PREVIEW_NO_MEMORY), App.createTextEntryScreen(recipientMsg, selected, fc));
                    }
                    // endif
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
                App.openChannelView(false);
            }
        }
        else if (c == closeCommand) {
            App.openChannelView(false);
        }
    }

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
                    } else {
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
// endif