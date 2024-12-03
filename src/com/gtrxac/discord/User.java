package com.gtrxac.discord;

import cc.nnproject.json.JSONObject;

public class User implements HasIcon {
    String id;
    String name;
    String iconHash;

    // For placeholder icon
    int iconColor;
    String initials;

    public User(State s, JSONObject data) {
        id = data.getString("id");

        name = data.getString("global_name", null);
        if (name == null) {
            name = data.getString("username", "(no name)");
        }

        if (s.pfpType == State.PFP_TYPE_NONE) return;

        iconHash = data.getString("avatar", null);

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

        iconColor = Util.hsvToRgb((int) Long.parseLong(id) % 360, 192, 208);
    }

    public String getIconID() { return id; }
    public String getIconHash() { return iconHash; }
    public String getIconType() { return "/avatars/"; }

    public void iconLoaded(State s) {
        if (s.channelView != null) s.channelView.repaint();
    }
}
