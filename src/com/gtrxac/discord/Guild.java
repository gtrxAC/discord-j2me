package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Guild {
    public String id;
    public String name;

    public Guild(JSONObject data) {
        id = data.getString("id");
        name = data.getString("name");
    }
}
