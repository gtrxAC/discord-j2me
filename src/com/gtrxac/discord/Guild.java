package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;
import javax.microedition.lcdui.*;

public class Guild extends HasUnreads implements HasIcon, Strings {
    public String name;
    public Channel[] channels;
    public String iconHash;
    public Role[] roles;

    public Guild(JSONObject data) {
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

    public static Guild getById(String id) {
        if (App.guilds == null) return null;
        
        for (int i = 0; i < App.guilds.length; i++) {
            Guild g = App.guilds[i];
            if (g.id.equals(id)) return g;
        }
        return null;
    }

    public String getIconID() { return id; }
    public String getIconHash() { return iconHash; }
    public String getIconType() { return "/icons/"; }
    
    public boolean isDisabled() {
        return !Settings.showMenuIcons || Settings.menuIconSize == 0;
    }

    public void iconLoaded() {
		if (App.guildSelector != null) App.guildSelector.update(id);
    }

    public boolean hasUnreads() {
        if (channels == null) return false;

        for (int i = 0; i < channels.length; i++) {
            Channel ch = channels[i];
            if (
                ch.hasUnreads()
//#ifdef OVER_100KB
                && !FavoriteGuilds.isMuted(ch.id)
//#endif
            )
            return true;
        }
        return false;
    }
    
    public void markRead() {
        if (channels != null) UnreadManager.markRead(channels);
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("name", name);
        if (iconHash != null) result.put("icon", iconHash);
        return result;
    }
}
