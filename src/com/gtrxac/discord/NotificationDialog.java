//#ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.media.*;

public class NotificationDialog extends Dialog implements CommandListener, Strings, Runnable {
    private Command viewCommand;
    private Command closeCommand;
    private Displayable lastScreen;

    private Notification notif;

    private boolean threadIsForClosingPlayer;
    private Player playerToClose;
    
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

        new Thread(this).start();
    }

    public NotificationDialog(Player player) {
        super("", "");
        threadIsForClosingPlayer = true;
        playerToClose = player;
        new Thread(this).start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == viewCommand) {
            notif.view();
        }
        else if (c == closeCommand) {
            App.disp.setCurrent(lastScreen);
        }
    }

    public void run() {
        if (!threadIsForClosingPlayer) {
            Util.sleep(50);
            while (true) {
                Displayable curr = App.disp.getCurrent();
                if (curr == this) {
                    playerToClose = App.gateway.playNotificationSound();
                    break;
                }
                Util.sleep(125);
            }
        }
        Util.sleep(10000);
        try {
            playerToClose.close();
        }
        catch (Exception e) {}
    }
}
//#endif