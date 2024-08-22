package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class OldChannelView extends Form implements CommandListener, ItemCommandListener, Strings {
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

        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        sendCommand = Locale.createCommand(SEND_MESSAGE, Command.ITEM, 1);
        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 2);
        olderCommand = Locale.createCommand(VIEW_OLDER_MESSAGES, Command.ITEM, 3);
        newerCommand = Locale.createCommand(VIEW_NEWER_MESSAGES, Command.ITEM, 4);
        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);

        int layout = Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE;

        olderButton = new StringItem(null, Locale.get(VIEW_OLDER_MESSAGES_L), Item.BUTTON);
        olderButton.setFont(s.messageFont);
        olderButton.setLayout(layout);
        olderButton.setDefaultCommand(olderCommand);
        olderButton.setItemCommandListener(this);

        newerButton = new StringItem(null, Locale.get(VIEW_NEWER_MESSAGES_L), Item.BUTTON);
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
            String typingStr = ChannelView.getTypingString(s);
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
            append(Locale.get(CHANNEL_VIEW_EMPTY));
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