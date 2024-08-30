package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageBox extends TextBox implements CommandListener {
    State s;
    private Command sendCommand;
    private Command backCommand;

    public MessageBox(State s) {
        super("", "", 2000, 0);
        setTitle("Send message (" + s.selectedChannel.name + ")");
        
        setCommandListener(this);
        this.s = s;

        sendCommand = new Command("Send", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 1);

        addCommand(sendCommand);
        addCommand(backCommand);
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
    }
}
