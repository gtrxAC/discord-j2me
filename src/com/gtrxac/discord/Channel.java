package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Channel {
    public String guildId;
    public String id;
    public String name;

    public Channel(JSONObject data) {
        guildId = data.getString("guild_id");
        id = data.getString("id");
        name = data.getString("name");
    }
}
