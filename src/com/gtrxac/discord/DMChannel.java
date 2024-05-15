package com.gtrxac.discord;

import cc.nnproject.json.*;

public class DMChannel {
    boolean isGroup;
    public String id;
    public String name;
    public long lastMessageID;

    public DMChannel(JSONObject data) {
        id = data.getString("id");
        lastMessageID = Long.parseLong(data.getString("last_message_id"));
        isGroup = data.getInt("type") == 3;

        if (isGroup) {
            name = data.getString("name");
        } else {
            name = data.getArray("recipients").getObject(0).getString("username");
        }
    }
}