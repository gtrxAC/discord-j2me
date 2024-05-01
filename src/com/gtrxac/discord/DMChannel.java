package com.gtrxac.discord;

import java.util.*;
import cc.nnproject.json.*;

public class DMChannel {
    public String id;
    public String name;

    public DMChannel(JSONObject data) {
        id = data.getString("id");
        name = data.getArray("recipients").getObject(0).getString("username");
    }

    public static Vector fetchDMChannels(State s) throws Exception {
        JSONArray channels = JSON.getArray(s.http.get("/users/@me/channels"));
        Vector result = new Vector();

        for (int i = 0; i < channels.size(); i++) {
            JSONObject ch = channels.getObject(i);
            int type = ch.getInt("type", 1);
            if (type != 1) continue; // TODO: support group DM

            result.addElement(new DMChannel(ch));
        }
        return result;
    }
}
