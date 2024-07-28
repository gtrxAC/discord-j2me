package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;
import javax.microedition.lcdui.*;

public class Guild implements HasIcon {
    public String id;
    public String name;
    public Vector channels;
    public String iconHash;
    public Vector roles;

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

    public static Guild getById(State s, String id) {
        if (s.guilds == null) return null;
        
        for (int i = 0; i < s.guilds.size(); i++) {
            Guild g = (Guild) s.guilds.elementAt(i);
            if (g.id.equals(id)) return g;
        }
        return null;
    }

    public String toString(State s) {
        if (channels == null) return name;

        for (int i = 0; i < channels.size(); i++) {
            Channel ch = (Channel) channels.elementAt(i);
            if (s.unreads.hasUnreads(ch)) return "* " + name;
        }
        return name;
    }

    public String getIconID() { return id; }
    public String getIconHash() { return iconHash; }
    public String getIconType() { return "/icons/"; }

    public void iconLoaded(State s) {
		if (s.guildSelector != null) s.guildSelector.update(id);
    }
    
    public void largeIconLoaded(State s) {}
}
