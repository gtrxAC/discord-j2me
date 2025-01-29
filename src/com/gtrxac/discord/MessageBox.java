package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;

public class MessageBox extends TextBox implements CommandListener, Strings {
    private State s;
    private Displayable lastScreen;
    private Command sendCommand;
    private Command addMentionCommand;
    // ifdef OVER_100KB
    private Command addEmojiCommand;
    // endif
    private Command backCommand;

    private String attachName;
    private FileConnection attachFc;

    private Message editMessage;  // message that is being edited, or null if writing a new message

    // ifdef OVER_100KB
    public boolean showedPreviewScreen = false;
    // endif

    private void init(State s, int sendCommandLabel) {
        setCommandListener(this);
        this.s = s;
        this.lastScreen = s.disp.getCurrent();

        sendCommand = Locale.createCommand(sendCommandLabel, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);
        addMentionCommand = Locale.createCommand(INSERT_MENTION, Command.ITEM, 2);
        // ifdef OVER_100KB
        addEmojiCommand = Locale.createCommand(INSERT_EMOJI, Command.ITEM, 3);
        // endif

        addCommand(sendCommand);
        addCommand(backCommand);
        if (!s.isDM) addCommand(addMentionCommand);
        // ifdef OVER_100KB
        addCommand(addEmojiCommand);
        s.gatewaySendTyping();
        // endif
    }

    public MessageBox(State s) {
        this(s, null, null);
    }

    public MessageBox(State s, Message editMessage) {
        super(Locale.get(MESSAGE_EDIT_BOX_TITLE), editMessage.rawContent, 2000, 0);
        init(s, OK);
        this.editMessage = editMessage;
    }

    public MessageBox(State s, String attachName, FileConnection attachFc) {
        super(getMessageBoxTitle(s), "", 2000, 0);
        init(s, SEND_MESSAGE);
        this.attachName = attachName;
        this.attachFc = attachFc;
    }

    // Also used by reply form
    public static String getMessageBoxTitle(State s) {
        StringBuffer sb = new StringBuffer();
        
        if (s.isDM) {
            sb.append(Locale.get(MESSAGE_BOX_TITLE_PREFIX_DM));
            sb.append(s.selectedDmChannel.name);
        } else {
            String prefix = Locale.get(MESSAGE_BOX_TITLE_PREFIX_CHANNEL);
            // Remove "#" character if we're in a thread
            if (s.selectedChannel.isThread) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            sb.append(prefix);
            sb.append(s.selectedChannel.name);
        }
        sb.append(Locale.get(RIGHT_PAREN));
        return sb.toString();
    }

    // Send HTTP request to send a message. Also used by ReplyForm
    public static void sendMessage(State s, String msg, String refID, String attachName, FileConnection attachFc, boolean ping) {
        HTTPThread h;
        if (attachName != null) {
            h = new HTTPThread(s, HTTPThread.SEND_ATTACHMENT);
            h.attachName = attachName;
            h.attachFc = attachFc;
        } else {
            h = new HTTPThread(s, HTTPThread.SEND_MESSAGE);
        }
        h.sendMessage = msg;
        h.sendReference = refID;
        h.sendPing = ping;
        h.start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            if (editMessage == null) {
                sendMessage(s, getString(), null, attachName, attachFc, false);
            } else {
                s.openChannelView(false);
                HTTPThread h = new HTTPThread(s, HTTPThread.EDIT_MESSAGE);
                h.editMessage = editMessage;
                h.editContent = getString();
                h.start();
            }
        }
        else if (c == backCommand) {
            // ifdef OVER_100KB
            if (!showedPreviewScreen) {
                try {
                    attachFc.close();
                }
                catch (Throwable e) {}
            }
            // endif
            s.disp.setCurrent(lastScreen);
        }
        else if (c == addMentionCommand) {
            if (!s.gatewayActive()) {
                s.error(Locale.get(REQUIRES_GATEWAY));
                return;
            }
            s.disp.setCurrent(new MentionForm(s));
        }
        // ifdef OVER_100KB
        else {
            // add emoji command
            EmojiPicker.show(s);
        }
        // endif
    }
}
