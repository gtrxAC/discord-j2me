package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class OldChannelView extends Form implements CommandListener, ItemCommandListener {
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
        sendCommand = new Command("Send", "Send message", Command.ITEM, 1);
        refreshCommand = new Command("Refresh", Command.ITEM, 2);
        olderCommand = new Command("Older", "View older messages", Command.ITEM, 3);
        newerCommand = new Command("Newer", "View newer messages", Command.ITEM, 4);
        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);

        int layout = Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE;

        olderButton = new StringItem(null, "View older messages", Item.BUTTON);
        olderButton.setFont(s.messageFont);
        olderButton.setLayout(layout);
        olderButton.setDefaultCommand(olderCommand);
        olderButton.setItemCommandListener(this);

        newerButton = new StringItem(null, "View newer messages", Item.BUTTON);
        newerButton.setFont(s.messageFont);
        newerButton.setLayout(layout);
        newerButton.setDefaultCommand(newerCommand);
        newerButton.setItemCommandListener(this);

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
            s.error(e);
        }
    }

    public void update() {
        deleteAll();

        if (s.typingUsers != null && s.typingUsers.size() > 0) {
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

        if (s.messages.size() > 0 && page > 0) append(newerButton);
        
        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            StringItem msgItem = new StringItem(
                msg.author.name + (msg.recipient != null ? (" -> " + msg.recipient) : "") + "  " + msg.timestamp,
                msg.content
            );
            msgItem.setFont(s.messageFont);
            msgItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
            append(msgItem);
        }

        if (s.messages.size() == 0) {
            append("Nothing to see here");
        } else {
            append(olderButton);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.unreads.save();
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

    public void commandAction(Command c, Item i) {
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