package com.gtrxac.discord;

import java.util.Hashtable;

public class Notification implements Strings {
    public String guildID;
    public String channelID;

    Notification(String guildID, String channelID) {
        this.guildID = guildID;
        this.channelID = channelID;
    }

    /**
     * Opens the channel where this notification occurred.
     */
    public void view() {
        HTTPThread h = new HTTPThread(HTTPThread.VIEW_NOTIFICATION);
        h.guildID = guildID;
        h.channelID = channelID;
        h.start();
    }

    public static String createString(String location, Message msg) {
        StringBuffer sb = new StringBuffer();
        sb.append(msg.author.name);
        if (location == null) {
            sb.append(Locale.get(NOTIFICATION_DM));
        } else {
            sb.append(Locale.get(NOTIFICATION_SERVER)).append(location).append(": \"");
        }
        sb.append(msg.content).append("\"");
        return sb.toString();
    }
}