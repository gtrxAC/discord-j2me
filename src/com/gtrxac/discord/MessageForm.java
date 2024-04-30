package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class MessageForm extends Form implements CommandListener {
    State s;

    private TextField textField;
    private Command sendCommand;
    private Command backCommand;

    public MessageForm(State s) {
        super("Send message (#" + s.selectedChannel.name + ")");
        setCommandListener(this);
        this.s = s;

        textField = new TextField("Message", "", 2000, 0);
        sendCommand = new Command("Send", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 0);

        append(textField);
        addCommand(sendCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            try {
                Message.send(s, textField.getString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            s.disp.setCurrent(new ChannelView(s));
        }
        if (c == backCommand) {
            s.disp.setCurrent(new ChannelView(s));
        }
    }
}
