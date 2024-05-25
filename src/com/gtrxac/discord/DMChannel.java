package com.gtrxac.discord;

import cc.nnproject.json.*;

public class DMChannel {
    boolean isGroup;
    public String id;
    public String name;
    public long lastMessageID;

    public DMChannel(JSONObject data) {
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
        } else {
            try {
                JSONObject recipient = data.getArray("recipients").getObject(0);

                name = recipient.getString("global_name", "(no name)");
                if (name == null || name.equals("(no name)")) {
                    name = recipient.getString("username");
                }
            }
            catch (Exception e) {}
        }
        if (name == null) name = "(unknown)";
    }
}