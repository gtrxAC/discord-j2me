package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.util.*;
import java.io.*;

public class AttachmentPicker extends ListScreen implements CommandListener, Strings {
    private State s;
    private Command closeCommand;
    private String currentPath; // Root directory

    private Message recipientMsg;

    public AttachmentPicker(State s, Message recipientMsg) {
        super(Locale.get(ATTACHMENT_PICKER_TITLE), List.IMPLICIT);
        this.s = s;
        this.recipientMsg = recipientMsg;
        setCommandListener(this);

        closeCommand = Locale.createCommand(CLOSE, Command.BACK, 2);
        addCommand(closeCommand);

        currentPath = "file:///";
        listFiles();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (index >= 0) {
                String selected = getString(index);
                String selectedPath = currentPath + selected;

                if (selected.endsWith("/")) { // Directory
                    currentPath = selectedPath;
                    listFiles();
                } else { // File
                    if (recipientMsg != null) {
                        s.disp.setCurrent(new ReplyForm(s, recipientMsg, selected, selectedPath));
                    } else {
                        s.disp.setCurrent(new MessageBox(s, selected, selectedPath));
                    }
                }
            }
        }
        else if (c == BACK_COMMAND) {
            if (!currentPath.equals("file:///")) {
                int lastSlashIndex = currentPath.lastIndexOf('/', currentPath.length() - 2);
                if (lastSlashIndex != -1) {
                    currentPath = currentPath.substring(0, lastSlashIndex + 1);
                    listFiles();
                }
            } else {
                s.openChannelView(false);
            }
        }
        else if (c == closeCommand) {
            s.openChannelView(false);
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
        deleteAll();
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
            s.error(e);
        }
    }
}
