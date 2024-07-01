package com.gtrxac.discord;

import cc.nnproject.json.JSONObject;

public class Role {
    public String id;
    public int color;

    public Role(JSONObject data) {
        id = data.getString("id");
        color = data.getInt("color");
    }
}