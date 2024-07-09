package com.gtrxac.discord;

import cc.nnproject.json.JSONObject;

public class User {
    String id;
    String name;

    // For placeholder icon
    int iconColor;
    String initials;

    public User(State s, JSONObject data) {
        id = data.getString("id");

        name = data.getString("global_name", null);
        if (name == null) {
            name = data.getString("username", "(no name)");
        }

        if (s.iconType == State.ICON_TYPE_NONE) return;

        StringBuffer initialsBuf = new StringBuffer();
        initialsBuf.append(name.charAt(0));
        if (name.length() > 1) {
            for (int i = 1; i < name.length(); i++) {
                char last = name.charAt(i - 1);
                char curr = name.charAt(i);

                if (last == ' ' || (Character.isLowerCase(last) && Character.isUpperCase(curr))) {
                    initialsBuf.append(curr);
                    break; // max 2 chars
                }
            }
        }
        initials = initialsBuf.toString();

        iconColor = Util.hsvToRgb((int) Long.parseLong(id) % 360, 192, 224);
    }
}
