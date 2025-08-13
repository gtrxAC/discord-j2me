// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class NotificationDialog extends Dialog implements CommandListener, Strings {
    private Command viewCommand;
    private Command closeCommand;
    private Displayable lastScreen;

    private Notification notif;
    
    public NotificationDialog(Notification notif, String location, Message msg) {
        super(Locale.get(NOTIFICATION_TITLE), "");
        setCommandListener(this);
        this.notif = notif;
        lastScreen = App.disp.getCurrent();

        setString(Notification.createString(location, msg));

        viewCommand = Locale.createCommand(VIEW, Command.OK, 1);
        closeCommand = Locale.createCommand(CLOSE, Command.BACK, 0);
        addCommand(viewCommand);
        addCommand(closeCommand);
    }

    protected void showNotify() {
        App.gateway.playNotificationSound();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == viewCommand) {
            notif.view();
        }
        else if (c == closeCommand) {
            App.disp.setCurrent(lastScreen);
        }
    }
}
// endif