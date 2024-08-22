package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ReplyForm extends Form implements CommandListener, Strings {
    State s;
    Message msg;

    public TextField replyField;
    private ChoiceGroup pingGroup;
    private Command sendCommand;
    private Command addMentionCommand;
    private Command backCommand;

    public ReplyForm(State s, Message msg) {
        super("");
        setTitle(MessageBox.getMessageBoxTitle(s));
        
        setCommandListener(this);
        this.s = s;
        this.msg = msg;

        StringItem refItem = new StringItem(Locale.get(REPLYING_TO) + msg.author.name, msg.content);
        refItem.setFont(s.messageFont);
        append(refItem);

        replyField = new TextField(Locale.get(REPLY_FORM_LABEL), "", 2000, 0);
        append(replyField);

        if (!s.isDM) {
            String[] pingChoices = {Locale.get(REPLY_FORM_PING)};
            Image[] pingImages = {null};
            boolean[] pingSelection = {!msg.author.id.equals(s.myUserId)};
            pingGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, pingChoices, pingImages);
            pingGroup.setSelectedFlags(pingSelection);
            append(pingGroup);
        }

        sendCommand = Locale.createCommand(SEND_MESSAGE, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);
        addMentionCommand = Locale.createCommand(INSERT_MENTION, Command.ITEM, 2);

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
                s.error(Locale.get(REQUIRES_GATEWAY));
                return;
            }
            s.disp.setCurrent(new MentionForm(s));
        }
    }
}
