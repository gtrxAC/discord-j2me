package com.gtrxac.discord;

import cc.nnproject.json.*;
import javax.microedition.lcdui.*;

public class DMChannel implements HasIcon {
    boolean isGroup;
    public String id;
    public String name;
    public long lastMessageID;
    public String iconID;  // for groups, group ID. for users, recipient ID (not DM channel ID)
    public String iconHash;
    public Image icon;

    public DMChannel(State s, JSONObject data) {
        id = data.getString("id");
        isGroup = data.getInt("type") == 3;
        
        try {
            lastMessageID = Long.parseLong(data.getString("last_message_id"));
        }
        catch (Exception e) {
            lastMessageID = 0L;
        }

        if (isGroup) {
            name = data.getString("name");
            iconID = id;
            iconHash = data.getString("icon", null);
        } else {
            try {
                JSONObject recipient = data.getArray("recipients").getObject(0);

                name = recipient.getString("global_name", "(no name)");
                if (name == null || name.equals("(no name)")) {
                    name = recipient.getString("username");
                }

                iconID = recipient.getString("id");
                iconHash = recipient.getString("avatar");
            }
            catch (Exception e) {}
        }
        if (name == null) name = "(unknown)";

        if (iconHash != null) {
            HTTPThread h = new HTTPThread(s, HTTPThread.FETCH_ICON);
            h.iconID = iconID;
            h.iconHash = iconHash;
            h.iconType = isGroup ? "/channel-icons/" : "/avatars/";
            h.iconTarget = this;
            h.start();
        }
    }

    /**
     * Sets the icon for this guild and updates the guild list to show it.
     * Called by the HTTP thread after fetching the icon.
     */
    public void setIcon(State s, Image icon) {
        this.icon = icon;
        this.iconID = null;
        this.iconHash = null;
		if (s.dmSelector != null) s.dmSelector.update();
    }
}