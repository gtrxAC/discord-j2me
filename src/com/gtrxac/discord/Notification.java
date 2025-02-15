package com.gtrxac.discord;

import java.util.Hashtable;

public class Notification {
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
}