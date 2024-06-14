package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;
import javax.microedition.lcdui.*;

public class Guild implements HasIcon {
    public String id;
    public String name;
    public Vector channels;
    public String iconHash;

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

    public String getIconID() { return id; }
    public String getIconHash() { return iconHash; }
    public String getIconType() { return "/icons/"; }

    public void iconLoaded(State s) {
		if (s.guildSelector != null) s.guildSelector.update();
    }
}
