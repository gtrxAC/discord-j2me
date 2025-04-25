package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Prompt for sending and editing messages.
 */
public class MessageBox extends TextBox implements CommandListener {
    private Message editMessage;  // null if sending a message
    private Command sendCommand;
    private Command backCommand;

    MessageBox(Message editMessage) {
        super("", "", 2000, 0);
        this.editMessage = editMessage;
        setCommandListener(this);

        if (editMessage == null) {
            setTitle("Send message (" + App.selectedChannel.name + ")");
        } else {
            setTitle("Edit message");
            setString(editMessage.content);
        }

        sendCommand = new Command("OK", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(sendCommand);
        addCommand(backCommand);
    }

    // Send HTTP request to send a message. Also used by ReplyForm
    public static void sendMessage(String msg, String refID, boolean ping) {
        HTTPThread h = new HTTPThread(HTTPThread.SEND_MESSAGE);
        h.sendMessage = msg;
        h.sendReference = refID;
        h.sendPing = ping;
        h.start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            if (editMessage == null) {
                sendMessage(getString(), null, false);
            } else {
                HTTPThread h = new HTTPThread(HTTPThread.EDIT_MESSAGE);
                h.editMessage = editMessage;
                h.editContent = getString();
                h.start();
            }
        } else {
            // back command
            App.disp.setCurrent(App.channelView);
        }
    }
}
