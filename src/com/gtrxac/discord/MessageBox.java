package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageBox extends TextBox implements CommandListener {
    State s;
    private Command sendCommand;
    private Command addMentionCommand;
    private Command backCommand;

    public MessageBox(State s) {
        super("", "", 2000, 0);
        if (s.isDM) setTitle("Send message (@" + s.selectedDmChannel.name + ")");
        else setTitle("Send message (#" + s.selectedChannel.name + ")");
        
        setCommandListener(this);
        this.s = s;

        sendCommand = new Command("Send", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 1);
        addMentionCommand = new Command("Insert mention", Command.ITEM, 2);

        addCommand(sendCommand);
        addCommand(backCommand);
        if (!s.isDM) addCommand(addMentionCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            try {
                s.sendMessage = getString();
                s.sendReference = null;
                s.sendPing = false;
                new HTTPThread(s, HTTPThread.SEND_MESSAGE).start();
            }
            catch (Exception e) {
                e.printStackTrace();
                s.error(e);
            }
        }
        else if (c == backCommand) {
            if (s.oldUI) s.disp.setCurrent(s.oldChannelView);
            else s.disp.setCurrent(s.channelView);
        }
        else if (c == addMentionCommand) {
            if (!s.gatewayActive()) {
                s.error("Requires active gateway connection");
                return;
            }
            s.disp.setCurrent(new MentionForm(s));
        }
    }
}
