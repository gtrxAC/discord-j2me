package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Guild {
    public String id;
    public String name;
    public Vector channels;

    public Guild(JSONObject data) {
        id = data.getString("id");
        
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
}
