package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;
import javax.microedition.lcdui.*;

public class Guild implements HasIcon {
    public String id;
    public String name;
    public Vector channels;
    public String iconHash;
    public Image icon;

    public Guild(State s, JSONObject data) {
        id = data.getString("id");
        iconHash = data.getString("icon", null);
        
        if (data.has("name")) {
            name = data.getString("name");
        }
        else if (data.has("properties")) {
            name = data.getObject("properties").getString("name");
        }
        else {
            name = "(unknown)";
        }

        if (data.has("channels")) {
            channels = Channel.parseChannels(data.getArray("channels"));
        }

        if (iconHash != null) {
            HTTPThread h = new HTTPThread(s, HTTPThread.FETCH_ICON);
            h.iconID = id;
            h.iconHash = iconHash;
            h.iconType = "/icons/";
            h.iconTarget = this;
            h.start();
        }
    }

    public String toString() {
        if (channels == null) return name;

        boolean unread = false;
        int pings = 0;

        for (int i = 0; i < channels.size(); i++) {
            Channel ch = (Channel) channels.elementAt(i);
            if (ch.unread) unread = true;
            pings += ch.pings;
        }

        if (pings > 0) return "(" + pings + ") " + name;
        if (unread) return "* " + name;
        return name;
    }

    /**
     * Sets the icon for this guild and updates the guild list to show it.
     * Called by the HTTP thread after fetching the icon.
     */
    public void setIcon(State s, Image icon) {
        this.icon = icon;
        this.iconHash = null;
		if (s.guildSelector != null) s.guildSelector.update();
    }
}
