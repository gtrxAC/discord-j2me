package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;

public class ReplyForm extends Form implements CommandListener, Strings {
    private Object lastScreen;
    Message msg;

    public TextField replyField;
    private ChoiceGroup pingGroup;
    private Command sendCommand;
    private Command addMentionCommand;
//#ifdef EMOJI_SUPPORT
    private Command addEmojiCommand;
//#endif
    private Command backCommand;

    private String attachName;
    private FileConnection attachFc;

//#ifdef OVER_100KB
    public boolean showedPreviewScreen = false;
//#endif

    public ReplyForm(Message msg) {
        this(msg, null, null);
    }

    public ReplyForm(Message msg, String attachName, FileConnection attachFc) {
        super("");
        setTitle(MessageBox.getMessageBoxTitle());
        
        setCommandListener(this);
        this.lastScreen = App.disp.getCurrent();
        this.msg = msg;
        this.attachName = attachName;
        this.attachFc = attachFc;

        StringItem refItem = new StringItem(Locale.get(REPLYING_TO) + msg.author.name, msg.content);
        refItem.setFont(App.messageFont);
        append(refItem);

        if (attachName != null) {
            StringItem fileItem = new StringItem(Locale.get(ATTACHED_FILE), attachName);
            fileItem.setFont(App.messageFont);
            append(fileItem);
        }

//#ifdef OVER_100KB
        replyField = new TextField(Locale.get(REPLY_FORM_LABEL), ChannelView.draftMessage, 2000, 0);
//#else
        replyField = new TextField(Locale.get(REPLY_FORM_LABEL), "", 2000, 0);
//#endif
        append(replyField);

        if (!App.isDM) {
            String[] pingChoices = {Locale.get(REPLY_FORM_PING)};
            Image[] pingImages = {null};
            boolean[] pingSelection = {!msg.author.id.equals(App.myUserId)};
            pingGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, pingChoices, pingImages);
            pingGroup.setSelectedFlags(pingSelection);
            append(pingGroup);
        }

        sendCommand = Locale.createCommand(SEND_MESSAGE, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);
        addMentionCommand = Locale.createCommand(INSERT_MENTION, Command.ITEM, 2);
//#ifdef EMOJI_SUPPORT
        addEmojiCommand = Locale.createCommand(INSERT_EMOJI, Command.ITEM, 3);
//#endif

        addCommand(sendCommand);
        addCommand(backCommand);
        if (!App.isDM) addCommand(addMentionCommand);
//#ifdef EMOJI_SUPPORT
        addCommand(addEmojiCommand);
//#endif
//#ifdef OVER_100KB
        App.gatewaySendTyping();
//#endif
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            boolean[] selected = {true};
            if (!App.isDM) pingGroup.getSelectedFlags(selected);
            
            MessageBox.sendMessage(replyField.getString(), msg.id, attachName, attachFc, selected[0]);
        }
        else if (c == backCommand) {
//#ifdef OVER_100KB
            if (!showedPreviewScreen) {
                try {
                    attachFc.close();
                }
                catch (Throwable e) {}
            }
            App.channelView.setDraftMessage(replyField.getString());
//#endif
            App.disp.setCurrent(lastScreen);
        }
        else if (c == addMentionCommand) {
            if (!App.gatewayActive()) {
                App.error(Locale.get(REQUIRES_GATEWAY));
                return;
            }
            App.disp.setCurrent(new MentionForm());
        }
//#ifdef EMOJI_SUPPORT
        else {
            // add emoji command
            EmojiPicker.show();
        }
//#endif
    }
}
