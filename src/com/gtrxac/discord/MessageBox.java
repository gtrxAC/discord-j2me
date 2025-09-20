package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;

public class MessageBox extends TextBox implements CommandListener, Strings {
    private Command sendCommand;
    private Command addMentionCommand;
//#ifdef EMOJI_SUPPORT
    private Command addEmojiCommand;
//#endif
    private Command backCommand;

    private String attachName;
    private FileConnection attachFc;

    private Message editMessage;  // message that is being edited, or null if writing a new message

//#ifdef OVER_100KB
    public boolean showedPreviewScreen = false;
//#endif

//#ifdef TOUCH_SUPPORT
    public boolean showEmojiPicker;
    private boolean shownEmojiPicker;
//#endif

    private void init(int sendCommandLabel) {
        setCommandListener(this);

        sendCommand = Locale.createCommand(sendCommandLabel, Command.OK, 0);
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

    public MessageBox() {
        this(null, null);
    }

    public MessageBox(Message editMessage) {
        super(Locale.get(MESSAGE_EDIT_BOX_TITLE), editMessage.rawContent, 2000, 0);
        init(OK);
        this.editMessage = editMessage;
    }

    public MessageBox(String attachName, FileConnection attachFc) {
        super(getMessageBoxTitle(), "", 2000, 0);
        init(SEND_MESSAGE);
        this.attachName = attachName;
        this.attachFc = attachFc;
    }

//#ifdef TOUCH_SUPPORT
    public void showEmojiPicker() {
        if (showEmojiPicker && !shownEmojiPicker) {
            EmojiPicker.show();
            shownEmojiPicker = true;
        }
    }
//#endif

    // Also used by reply form
    public static String getMessageBoxTitle() {
        StringBuffer sb = new StringBuffer();
        
        if (App.isDM) {
            sb.append(Locale.get(MESSAGE_BOX_TITLE_PREFIX_DM));
            sb.append(App.selectedDmChannel.name);
        } else {
            String prefix = Locale.get(MESSAGE_BOX_TITLE_PREFIX_CHANNEL);
            // Remove "#" character if we're in a thread
            if (App.selectedChannel.isThread) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            sb.append(prefix);
            sb.append(App.selectedChannel.name);
        }
        sb.append(Locale.get(RIGHT_PAREN));
        return sb.toString();
    }

    // Send HTTP request to send a message. Also used by ReplyForm
    public static void sendMessage(String msg, String refID, String attachName, FileConnection attachFc, boolean ping) {
        HTTPThread h;
        if (attachName != null) {
            h = new HTTPThread(HTTPThread.SEND_ATTACHMENT);
            h.attachName = attachName;
            h.attachFc = attachFc;
        } else {
            h = new HTTPThread(HTTPThread.SEND_MESSAGE);
        }
//#ifdef OVER_100KB
        // Bypass proxy-side upload warning which was previously done by manually adding a # at the start of the message
        if (attachName != null) h.sendMessage = "#" + msg;
        else
//#endif
        h.sendMessage = msg;
        
        h.sendReference = refID;
        h.sendPing = ping;
        h.start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            if (editMessage == null) {
                sendMessage(getString(), null, attachName, attachFc, false);
            } else {
                App.openChannelView(false);
                HTTPThread h = new HTTPThread(HTTPThread.EDIT_MESSAGE);
                h.editMessage = editMessage;
                h.editContent = getString();
                h.start();
            }
        }
        else if (c == backCommand) {
//#ifdef OVER_100KB
            if (!showedPreviewScreen) {
                try {
                    attachFc.close();
                }
                catch (Throwable e) {}
            }
//#endif
            App.openChannelView(false);
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
