package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageBox extends TextBox implements CommandListener, Strings {
    State s;
    private Command sendCommand;
    private Command addMentionCommand;
    private Command backCommand;

    public MessageBox(State s) {
        super("", "", 2000, 0);
        setTitle(getMessageBoxTitle(s));
        
        setCommandListener(this);
        this.s = s;

        sendCommand = Locale.createCommand(SEND_MESSAGE, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);
        addMentionCommand = Locale.createCommand(INSERT_MENTION, Command.ITEM, 2);

        addCommand(sendCommand);
        addCommand(backCommand);
        if (!s.isDM) addCommand(addMentionCommand);
    }

    // Also used by reply form
    public static String getMessageBoxTitle(State s) {
        if (s.isDM) {
            return 
                Locale.get(MESSAGE_BOX_TITLE_PREFIX_DM) +
                s.selectedDmChannel.name +
                Locale.get(RIGHT_PAREN);
        }
        return 
            Locale.get(MESSAGE_BOX_TITLE_PREFIX_CHANNEL) +
            s.selectedChannel.name +
            Locale.get(RIGHT_PAREN);
    }

    // Send HTTP request to send a message. Also used by ReplyForm
    public static void sendMessage(State s, String msg, String refID, boolean ping) {
        HTTPThread h = new HTTPThread(s, HTTPThread.SEND_MESSAGE);
        h.sendMessage = msg;
        h.sendReference = refID;
        h.sendPing = ping;
        h.start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            sendMessage(s, getString(), null, false);
        }
        else if (c == backCommand) {
            s.disp.setCurrent(s.channelView);
        }
        else if (c == addMentionCommand) {
            if (!s.gatewayActive()) {
                s.error(Locale.get(REQUIRES_GATEWAY));
                return;
            }
            s.disp.setCurrent(new MentionForm(s));
        }
    }
}
