package com.gtrxac.discord;

import java.util.*;
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

    public static void fetchChannels(State s) throws Exception {
        JSONArray channels = JSON.getArray(s.http.get("/guilds/" + s.selectedGuild.id + "/channels"));
        s.channels = new Vector();

        for (int i = 0; i < channels.size(); i++) {
            for (int a = 0; a < channels.size(); a++) {
                JSONObject ch = channels.getObject(a);
                if (ch.getInt("position", i) != i) continue;

                int type = ch.getInt("type", 0);
                if (type != 0 && type != 5) continue;

                s.channels.addElement(new Channel(ch));
            }
        }
    }
}
