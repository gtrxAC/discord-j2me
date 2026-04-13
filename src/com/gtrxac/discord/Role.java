package com.gtrxac.discord;

import cc.nnproject.json.JSONObject;

public class Role {
    public String id;
    public int color;

    public Role(JSONObject data) {
        id = data.getString("id");
        color = data.getInt("color");

        if (Theme.isLight) {
            // light theme: darken the color if necessary for better contrast
            int[] rgb = Util.splitRGB(color);

            if (Math.min(Math.min(rgb[0], rgb[1]), rgb[2]) >= 240) {
                // if color is close to white, unset the color
                color = 0;
            } else {
                int[] hsv = Util.rgbToHsv(rgb[0], rgb[1], rgb[2]);

                if (hsv[2] > 55) {
                    hsv[2] = 55;
                    if (hsv[1] > 30 && hsv[1] < 80) hsv[1] = 80;
                }
                
                color = Util.hsvToRgb100(hsv[0], hsv[1], hsv[2]);
            }
        } else {
            // dark theme: lighten the color if necessary for better contrast
            int[] rgb = Util.splitRGB(color);

            if (Math.max(Math.max(rgb[0], rgb[1]), rgb[2]) <= 16) {
                // if color is close to black, unset the color
                color = 0;
            } else {
                int[] hsv = Util.rgbToHsv(rgb[0], rgb[1], rgb[2]);

                hsv[2] = 100;
                if (hsv[1] > 50) hsv[1] = 50;
                
                color = Util.hsvToRgb100(hsv[0], hsv[1], hsv[2]);
            }
        }
    }
}