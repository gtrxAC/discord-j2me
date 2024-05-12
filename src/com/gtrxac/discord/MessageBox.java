package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageBox extends TextBox implements CommandListener {
    State s;
    private Command sendCommand;
    private Command backCommand;

    public MessageBox(State s) {
        super("", "", 2000, 0);
        if (s.isDM) setTitle("Send message (@" + s.selectedDmChannel.name + ")");
        else setTitle("Send message (#" + s.selectedChannel.name + ")");
        
        setCommandListener(this);
        this.s = s;

        sendCommand = new Command("Send", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 0);

        addCommand(sendCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            try {
                Message.send(s, getString());
                s.openChannelView(true);
            }
            catch (Exception e) {
                e.printStackTrace();
                s.error(e.toString());
            }
        }
        if (c == backCommand) {
            s.disp.setCurrent(s.channelView);
        }
    }
}
