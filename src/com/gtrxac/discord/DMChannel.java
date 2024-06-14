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
    }

    public String getIconID() { return iconID; }
    public String getIconHash() { return iconHash; }
    public String getIconType() { return isGroup ? "/channel-icons/" : "/avatars/"; }

    public void iconLoaded(State s) {
		if (s.dmSelector != null) s.dmSelector.update();
    }
}