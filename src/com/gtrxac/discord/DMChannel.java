package com.gtrxac.discord;

import java.util.*;
import cc.nnproject.json.*;

public class DMChannel {
    public String id;
    public String name;
    public long lastMessageID;

    public DMChannel(JSONObject data) {
        id = data.getString("id");
        name = data.getArray("recipients").getObject(0).getString("username");
        lastMessageID = Long.parseLong(data.getString("last_message_id"));
    }

    public static void fetchDMChannels(State s) throws Exception {
        JSONArray channels = JSON.getArray(s.http.get("/users/@me/channels"));
        s.dmChannels = new Vector();

        for (int i = 0; i < channels.size(); i++) {
            JSONObject ch = channels.getObject(i);
            int type = ch.getInt("type", 1);
            if (type != 1) continue; // TODO: support group DM

            s.dmChannels.addElement(new DMChannel(ch));
        }
    }
}
