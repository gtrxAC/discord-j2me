package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReplyForm extends Form implements CommandListener {
    State s;
    Message msg;

    public TextField replyField;
    private ChoiceGroup pingGroup;
    private Command sendCommand;
    private Command addMentionCommand;
    private Command backCommand;

    public ReplyForm(State s, Message msg) {
        super("");
        if (s.isDM) setTitle("Send message (@" + s.selectedDmChannel.name + ")");
        else setTitle("Send message (#" + s.selectedChannel.name + ")");
        
        setCommandListener(this);
        this.s = s;
        this.msg = msg;

        StringItem refItem = new StringItem("Replying to " + msg.author.name, msg.content);
        refItem.setFont(s.messageFont);
        append(refItem);

        replyField = new TextField("Your message:", "", 2000, 0);
        append(replyField);

        if (!s.isDM) {
            String[] pingChoices = {"Mention author"};
            Image[] pingImages = {null};
            boolean[] pingSelection = {!msg.author.id.equals(s.myUserId)};
            pingGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, pingChoices, pingImages);
            pingGroup.setSelectedFlags(pingSelection);
            append(pingGroup);
        }

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
                boolean[] selected = {true};
                if (!s.isDM) pingGroup.getSelectedFlags(selected);

                s.sendMessage = replyField.getString();
                s.sendReference = msg.id;
                s.sendPing = selected[0];
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
