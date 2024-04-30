package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Guild {
    public String id;
    public String name;

    public Guild(JSONObject data) {
        id = data.getString("id");
        name = data.getString("name");
    }

    public static void fetchGuilds(State s) throws Exception {
        JSONArray guilds = JSON.getArray(s.http.get("/users/@me/guilds"));
        s.guilds = new Vector();

        for (int i = 0; i < guilds.size(); i++) {
            s.guilds.addElement(new Guild(guilds.getObject(i)));
        }
    }
}
