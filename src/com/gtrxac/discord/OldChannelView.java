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

        backCommand = new Command("Back", Command.BACK, 0);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        olderCommand = new Command("Older", "View older messages", Command.ITEM, 2);
        newerCommand = new Command("Newer", "View newer messages", Command.ITEM, 3);
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
        deleteAll();

        if (s.typingUsers.size() > 0) {
            String typingStr;
            switch (s.typingUsers.size()) {
                case 1: typingStr = s.typingUsers.elementAt(0) + " is typing"; break;
                case 2: typingStr = s.typingUsers.elementAt(0) + ", " + s.typingUsers.elementAt(1) + " are typing"; break;
                case 3: typingStr = s.typingUsers.elementAt(0) + ", " + s.typingUsers.elementAt(1) + ", " + s.typingUsers.elementAt(2) + " are typing"; break;
                default: typingStr = s.typingUsers.size() + " people are typing"; break;
            }

            StringItem typingItem = new StringItem(null, typingStr);
            typingItem.setFont(s.messageFont);
            typingItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
            append(typingItem);
        }
        
        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            StringItem msgItem = new StringItem(
                msg.author + (msg.recipient != null ? (" -> " + msg.recipient) : ""),
                msg.content
            );
            msgItem.setFont(s.messageFont);
            msgItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
            append(msgItem);
        }

        if (s.messages.size() > 0 && page > 0) addCommand(newerCommand);
        else removeCommand(newerCommand);

        if (s.messages.size() == 0) {
            append("Nothing to see here");
            removeCommand(olderCommand);
        } else {
            addCommand(olderCommand);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            if (s.isDM) s.openDMSelector(false);
            else s.openChannelSelector(false);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageBox(s));
        }
        if (c == refreshCommand) {
            s.openChannelView(true);
        }
        if (c == olderCommand) {
            page++;
            after = null;
            before = ((Message) s.messages.elementAt(s.messages.size() - 1)).id;
            getMessages();
        }
        if (c == newerCommand) {
            page--;
            before = null;
            after = ((Message) s.messages.elementAt(0)).id;
            getMessages();
        }
    }
}