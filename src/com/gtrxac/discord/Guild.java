package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;
import javax.microedition.lcdui.*;

public class Guild extends HasUnreads implements HasIcon, Strings {
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
            name = Locale.get(NAME_UNKNOWN);
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

    public String getIconID() { return id; }
    public String getIconHash() { return iconHash; }
    public String getIconType() { return "/icons/"; }
    
    public boolean isDisabled(State s) {
        return !s.showMenuIcons || s.menuIconSize == 0;
    }

    public void iconLoaded(State s) {
		if (s.guildSelector != null) s.guildSelector.update(id);
    }

    public boolean hasUnreads() {
        if (channels == null) return false;

        for (int i = 0; i < channels.size(); i++) {
            Channel ch = (Channel) channels.elementAt(i);
            if (ch.hasUnreads()) return true;
        }
        return false;
    }
    
    public void markRead() {
        if (channels != null) UnreadManager.markRead(channels);
    }
}
