package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReplyForm extends Form implements CommandListener {
    State s;
    Message msg;

    public TextField replyField;
    private ChoiceGroup pingGroup;
    private Command sendCommand;
    private Command backCommand;

    public ReplyForm(State s, Message msg) {
        super("");
        setTitle("Send message (" + s.selectedChannel.name + ")");
        
        setCommandListener(this);
        this.s = s;
        this.msg = msg;

        StringItem refItem = new StringItem("Replying to " + msg.author, msg.content);
        append(refItem);

        replyField = new TextField("Your message:", "", 2000, 0);
        append(replyField);

        if (!s.isDM) {
            String[] pingChoices = {"Mention author"};
            boolean[] pingSelection = {true};
            pingGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, pingChoices, null);
            pingGroup.setSelectedFlags(pingSelection);
            append(pingGroup);
        }

        sendCommand = new Command("Send", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 1);

        addCommand(sendCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            boolean[] selected = {true};
            if (!s.isDM) pingGroup.getSelectedFlags(selected);
            MessageBox.sendMessage(s, replyField.getString(), msg.id, selected[0]);
        }
        else if (c == backCommand) {
            s.disp.setCurrent(s.channelView);
        }
    }
}
