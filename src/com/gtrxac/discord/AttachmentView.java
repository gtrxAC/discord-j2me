package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AttachmentView extends Form implements CommandListener, ItemCommandListener, Strings {
    Attachment[] atts;

    private Command backCommand;
    private Command refreshCommand;

    public AttachmentView(Attachment[] atts) {
        super(Locale.get(ATTACHMENT_VIEW_TITLE));
        this.atts = atts;
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
            App.openAttachmentView(true, atts);
        }
    }

    public void commandAction(Command c, Item i) {
        int prio = c.getPriority();
        Attachment attach = atts[prio % 100];

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
