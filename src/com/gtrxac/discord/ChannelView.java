package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends Form implements CommandListener {
    State s;
    private Command backCommand;
    private Command sendCommand;
    private Command refreshCommand;

    public ChannelView(State s) throws Exception {
        super("");
        if (s.isDM) setTitle("@" + s.selectedDmChannel.name);
        else setTitle("#" + s.selectedChannel.name);

        setCommandListener(this);
        this.s = s;

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

        backCommand = new Command("Back", Command.BACK, 0);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);
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
    }
}