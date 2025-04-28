package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReplyForm extends Form implements CommandListener {
    Message msg;

    public TextField replyField;
    private ChoiceGroup pingGroup;
    private Command sendCommand;
    private Command backCommand;

    public ReplyForm(Message msg) {
        super("Send message");
        setCommandListener(this);
        this.msg = msg;

        StringItem refItem = new StringItem("Replying to " + msg.author, msg.content);
        append(refItem);

        replyField = new TextField("Your message:", "", 2000, 0);
        append(replyField);

        if (!App.isDM) {
            String[] pingChoices = {"Mention author"};
            pingGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, pingChoices, null);
            pingGroup.setSelectedIndex(0, true);
            append(pingGroup);
        }

        sendCommand = new Command("Send", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 1);

        addCommand(sendCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            MessageBox.sendMessage(
                replyField.getString(),
                msg.id,
                !App.isDM && pingGroup.isSelected(0)
            );
        } else {
            // back command
            App.disp.setCurrent(App.channelView);
        }
    }
}
