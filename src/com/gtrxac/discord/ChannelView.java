package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelView extends Form implements CommandListener {
    State s;
    private Command backCommand;
    private Command sendCommand;
    private Command refreshCommand;

    public ChannelView(State s) {
        super("#" + s.selectedChannel.name);
        setCommandListener(this);
        this.s = s;

        try {
            Message.fetchMessages(s);
            for (int i = 0; i < s.messages.size(); i++) {
                Message msg = (Message) s.messages.elementAt(i);
                StringItem msgItem = new StringItem(
                    msg.author + (msg.recipient != null ? (" -> " + msg.recipient) : ""),
                    msg.content
                );
                msgItem.setFont(s.smallFont);
                append(msgItem);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            append("Failed to get messages");
        }

        backCommand = new Command("Back", Command.BACK, 0);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(s.channelSelector);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageForm(s));
        }
        if (c == refreshCommand) {
            s.channelView = new ChannelView(s);
            s.disp.setCurrent(s.channelView);
        }
    }
}