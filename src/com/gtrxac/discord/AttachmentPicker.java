package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.util.*;
import java.io.*;

public class AttachmentPicker extends List implements CommandListener {
    private State s;
    private Command closeCommand;
    private Command backCommand;
    private String currentPath; // Root directory

    public AttachmentPicker(State s) {
        super("Select attachment", List.IMPLICIT);
        this.s = s;
        setCommandListener(this);

        backCommand = new Command("Back", Command.BACK, 1);
        closeCommand = new Command("Close", Command.BACK, 2);

        addCommand(closeCommand);
        addCommand(backCommand);

        currentPath = "file:///";
        listFiles();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (index >= 0) {
                String selected = getString(index);
                String selectedPath = currentPath + selected;

                if (selected.endsWith("/")) { // Directory
                    currentPath = selectedPath;
                    listFiles();
                } else { // File
                    HTTPThread h = new HTTPThread(s, HTTPThread.SEND_ATTACHMENT);
                    h.attachName = selected;
                    h.attachPath = selectedPath;
                    h.start();
                }
            }
        }
        else if (c == backCommand) {
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
                while (list.hasMoreElements()) {
                    String fileName = (String) list.nextElement();
                    append(fileName, null);
                }
                fc.close();
            }
        }
        catch (IOException e) {
            s.error(e);
        }
    }
}
