package com.gtrxac.discord;

import java.util.Hashtable;
import org.pigler.tester.*;

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
    public void view(State s) {
        HTTPThread h = new HTTPThread(s, HTTPThread.VIEW_NOTIFICATION);
        h.guildID = guildID;
        h.channelID = channelID;
        h.start();
    }
}