//#ifdef SAMSUNG_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class Dialogs100kb extends Dialog implements Strings, CommandListener {
    public static final int DELETE_CONFIRM_DIALOG = 0;
    public static final int NOTIFICATION_DIALOG = 1;
    public static final int RECONNECT_DIALOG = 2;

    public int dialogType;
    private Command yesCommand;
    private Command noCommand;
    private Displayable lastScreen;

    // Delete confirm dialog
    Message msg;

    public Dialogs100kb(Message msg) {
        super(Locale.get(DELETE_CONFIRM_TITLE), "");
        dialogType = DELETE_CONFIRM_DIALOG;

        String content = Util.stringToLength(msg.content, 30);
        setString(Locale.get(DELETE_CONFIRM_TEXT) + " \r\n\"" + content + '"');

        setCommandListener(this);
        this.msg = msg;

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    // Notification dialog
    private Notification notif;
    private boolean hasPlayedSound;

    public Dialogs100kb(Notification notif, String location, Message msg) {
        super(Locale.get(NOTIFICATION_TITLE), "");
        setCommandListener(this);
        this.notif = notif;
        lastScreen = App.disp.getCurrent();
        dialogType = NOTIFICATION_DIALOG;

        setString(Notification.createString(location, msg));

        yesCommand = Locale.createCommand(VIEW, Command.OK, 1);
        noCommand = Locale.createCommand(CLOSE, Command.BACK, 0);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    // Reconnect dialog
    public Dialogs100kb(String message) {
        super(Locale.get(RECONNECT_FORM_TITLE), "");
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();
        dialogType = RECONNECT_DIALOG;

        StringBuffer sb = new StringBuffer(Locale.get(Settings.autoReConnect ? AUTO_RECONNECT_FAILED : RECONNECT_FORM_TEXT));
        if (message != null && message.length() > 0) {
            sb.append("\n");
            sb.append(Locale.get(RECONNECT_FORM_MESSAGE));
            sb.append(":\n");
            sb.append(message);
        }
        setString(sb.toString());

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    protected void showNotify() {
        if (dialogType == NOTIFICATION_DIALOG && !hasPlayedSound) {
            App.gateway.playNotificationSound();
            hasPlayedSound = true;
        }
    }

    public void commandAction(Command c, Displayable d) {
        switch (dialogType) {
            case DELETE_CONFIRM_DIALOG: {
                App.openChannelView(false);
                
                if (c == yesCommand) {
                    HTTPThread h = new HTTPThread(HTTPThread.DELETE_MESSAGE);
                    h.editMessage = msg;
                    h.start();
                }
                break;
            }

            case NOTIFICATION_DIALOG: {
                if (c == yesCommand) {
                    notif.view();
                } else {
                    App.disp.setCurrent(lastScreen);
                }
                break;
            }

            case RECONNECT_DIALOG: {
                if (c == yesCommand) {
                    App.gateway = new GatewayThread();
                    App.gateway.start();
                }
                App.disp.setCurrent(lastScreen);
                break;
            }
        }
    }
}
//#endif