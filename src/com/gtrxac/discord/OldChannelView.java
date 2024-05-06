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
        addCommand(olderCommand);

        getMessages();
    }

    public void getMessages() {
        try {
            Message.fetchMessages(s, before, after);
            deleteAll();

            for (int i = 0; i < s.messages.size(); i++) {
                Message msg = (Message) s.messages.elementAt(i);
                StringItem msgItem = new StringItem(
                    msg.author + (msg.recipient != null ? (" -> " + msg.recipient) : ""),
                    msg.content
                );
                msgItem.setFont(s.smallFont);
                msgItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
                append(msgItem);
            }

            if (page > 0) addCommand(newerCommand);
            else removeCommand(newerCommand);
        }
        catch (Exception e) {
            s.error(e.toString());
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            if (s.isDM) s.disp.setCurrent(new DMSelector(s));
            else s.disp.setCurrent(s.channelSelector);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageForm(s));
        }
        if (c == refreshCommand) {
            s.openChannelView(true);
        }
        if (c == olderCommand) {
            page++;
            after = null;
            before = ((Message) s.messages.elementAt(19)).id;
            try {
                getMessages();
            }
            catch (Exception e) {
                s.error(e.toString());
            }
        }
        if (c == newerCommand) {
            page--;
            before = null;
            after = ((Message) s.messages.elementAt(0)).id;
            try {
                getMessages();
            }
            catch (Exception e) {
                s.error(e.toString());
            }
        }
    }
}