package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class NotificationDialog extends Dialog implements CommandListener, Strings {
    private State s;
    private Command viewCommand;
    private Command closeCommand;
    private Displayable lastScreen;

    private boolean isDM;
    private String guildID;
    private String channelID;
    
    public NotificationDialog(State s, boolean isDM, String guildID, String channelID, String location, Message msg) {
        super(s.disp, Locale.get(NOTIFICATION_TITLE), "");
        setCommandListener(this);
        this.s = s;
        lastScreen = s.disp.getCurrent();

        this.isDM = isDM;
        this.guildID = guildID;
        this.channelID = channelID;

        StringBuffer sb = new StringBuffer();
        sb.append(msg.author.name);
        if (isDM) {
            sb.append(Locale.get(NOTIFICATION_DM));
        } else {
            sb.append(Locale.get(NOTIFICATION_SERVER)).append(location);
            sb.append(": \"");
        }
        sb.append(msg.content);
        sb.append("\"");
        setString(sb.toString());

        viewCommand = Locale.createCommand(VIEW, Command.OK, 1);
        closeCommand = Locale.createCommand(CLOSE, Command.BACK, 0);
        addCommand(viewCommand);
        addCommand(closeCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == viewCommand) {
            HTTPThread h = new HTTPThread(s, HTTPThread.VIEW_NOTIFICATION);
            h.isDM = isDM;
            h.guildID = guildID;
            h.channelID = channelID;
            h.start();
        }
        else if (c == closeCommand) {
            s.disp.setCurrent(lastScreen);
        }
    }
}
