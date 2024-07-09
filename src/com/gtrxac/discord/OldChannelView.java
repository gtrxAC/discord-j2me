package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class OldChannelView extends Form implements CommandListener {
    State s;

    private Command backCommand;
    private Command sendCommand;
    private Command refreshCommand;
    private Command olderCommand;
    private Command newerCommand;

    private StringItem olderButton;
    private StringItem newerButton;

    // Parameters for viewing old messages (message IDs)
    int page;
    String before;
    String after;

    public OldChannelView(State s) throws Exception {
        super("");
        if (s.isDM) setTitle("@" + s.selectedDmChannel.name);
        else setTitle("#" + s.selectedChannel.name);

        setCommandListener(this);
        this.s = s;
        s.channelIsOpen = true;

        backCommand = new Command("Back", Command.BACK, 0);
        sendCommand = new Command("Send", Command.ITEM, 1);
        refreshCommand = new Command("Refresh", Command.ITEM, 2);
        olderCommand = new Command("Older", Command.ITEM, 3);
        newerCommand = new Command("Newer", Command.ITEM, 4);
        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);

        update();
    }

    public void getMessages() {
        try {
            HTTPThread h = new HTTPThread(s, HTTPThread.FETCH_MESSAGES);
            h.fetchMsgsBefore = before;
            h.fetchMsgsAfter = after;
            h.start();
        }
        catch (Exception e) {
            s.error(e.toString());
        }
    }

    public void update() {
        while (size() > 0) delete(0);

        if (s.typingUsers != null && s.typingUsers.size() > 0) {
            String typingStr;
            switch (s.typingUsers.size()) {
                case 1: typingStr = s.typingUsers.elementAt(0) + " is typing"; break;
                case 2: typingStr = s.typingUsers.elementAt(0) + ", " + s.typingUsers.elementAt(1) + " are typing"; break;
                case 3: typingStr = s.typingUsers.elementAt(0) + ", " + s.typingUsers.elementAt(1) + ", " + s.typingUsers.elementAt(2) + " are typing"; break;
                default: typingStr = s.typingUsers.size() + " people are typing"; break;
            }

            StringItem typingItem = new StringItem(null, typingStr);
            append(typingItem);
        }

        if (s.messages.size() > 0 && page > 0) {
            addCommand(olderCommand);
        } else {
            removeCommand(olderCommand);
        }
        
        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            StringItem msgItem = new StringItem(
                msg.author.name + (msg.recipient != null ? (" -> " + msg.recipient) : "") + "  " + msg.timestamp,
                msg.content
            );
            append(msgItem);
        }

        if (s.messages.size() == 0) {
            append("Nothing to see here");
            removeCommand(olderCommand);
        } else {
            addCommand(olderCommand);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.channelIsOpen = false;
            if (s.isDM) s.openDMSelector(false);
            else s.openChannelSelector(false);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageBox(s));
        }
        if (c == refreshCommand) {
            s.openChannelView(true);
        }
    }
}