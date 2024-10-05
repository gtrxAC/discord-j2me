package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AttachmentView extends Form implements CommandListener, ItemCommandListener, Strings {
    State s;
    Message msg;

    private Command backCommand;
    private Command refreshCommand;

    public AttachmentView(State s, Message msg) {
        super(Locale.get(ATTACHMENT_VIEW_TITLE));
        this.s = s;
        this.msg = msg;
        setCommandListener(this);

        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.openChannelView(false);
        }
        else if (c == refreshCommand) {
            s.openAttachmentView(true, msg);
        }
    }

    public void commandAction(Command c, Item i) {
        int prio = c.getPriority();

        if (prio < 100) {
            // 'Open in browser' button
            Attachment attach = (Attachment) msg.attachments.elementAt(prio);
            s.platformRequest(attach.browserUrl);
        } else {
            // 'View as text' button
            Attachment attach = (Attachment) msg.attachments.elementAt(prio - 100);
            HTTPThread h = new HTTPThread(s, HTTPThread.VIEW_ATTACHMENT_TEXT);
            h.viewAttach = attach;
            h.start();
        }
    }
}
