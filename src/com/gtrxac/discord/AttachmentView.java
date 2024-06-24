package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AttachmentView extends Form implements CommandListener, ItemCommandListener {
    State s;
    Message msg;

    private Command backCommand;
    private Command refreshCommand;

    public AttachmentView(State s, Message msg) {
        super("Attachments");
        this.s = s;
        this.msg = msg;
        setCommandListener(this);

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);

        new HTTPThread(s, HTTPThread.FETCH_ATTACHMENTS).start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.openChannelView(false);
        }
        if (c == refreshCommand) {
            s.openAttachmentView(true, msg);
        }
    }

    public void commandAction(Command c, Item i) {
        int prio = c.getPriority();

        if (prio < 100) {
            // 'Open in browser' button
            Attachment attach = (Attachment) msg.attachments.elementAt(prio);
            s.platformRequest(attach.url + attach.browserSizeParam);
        } else {
            // 'View as text' button
            Attachment attach = (Attachment) msg.attachments.elementAt(prio - 100);
            HTTPThread h = new HTTPThread(s, HTTPThread.VIEW_ATTACHMENT_TEXT);
            h.viewAttach = attach;
            h.start();
        }
    }
}
