package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelView extends Form implements CommandListener {
    State s;
    private Command backCommand;
    private Command sendCommand;

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
        sendCommand = new Command("Send message", "Send", Command.ITEM, 0);
        addCommand(backCommand);
        addCommand(sendCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(s.channelSelector);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageForm(s));
        }
    }
}