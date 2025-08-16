package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AttachmentView extends Form implements CommandListener, ItemCommandListener, Strings {
    Message msg;

    private Command backCommand;
    private Command refreshCommand;

    public AttachmentView(Message msg) {
        super(Locale.get(ATTACHMENT_VIEW_TITLE));
        this.msg = msg;
        setCommandListener(this);

        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            App.openChannelView(false);
        }
        else if (c == refreshCommand) {
            App.openAttachmentView(true, msg);
        }
    }

    public void commandAction(Command c, Item i) {
        int prio = c.getPriority();
        Attachment attach = (Attachment) msg.attachments.elementAt(prio % 100);

        if (prio < 100) {
            // 'Open in browser' button
            App.platRequest(attach.browserUrl);
        }
        else
//#ifdef OVER_100KB
        if (prio < 200)
//#endif 
        {
            // 'View as text' or 'Set as notification sound' button
            int act =
//#ifdef OVER_100KB
                attach.isAudio ? HTTPThread.VIEW_ATTACHMENT_AUDIO :
//#endif
                HTTPThread.VIEW_ATTACHMENT_TEXT;
            HTTPThread h = new HTTPThread(act);
            h.viewAttach = attach;
            h.start();
        }
//#ifdef OVER_100KB
        else {
            // 'Set as theme' button
            HTTPThread h = new HTTPThread(HTTPThread.SET_THEME);
            h.viewAttach = attach;
            h.start();
        }
//#endif
    }
}
