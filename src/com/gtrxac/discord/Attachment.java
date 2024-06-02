package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Attachment {
    public String url;

    public Attachment(State s, JSONObject data) {
        int imageWidth = data.getInt("width");
        int imageHeight = data.getInt("height");
        int imageAspectRatio = imageWidth*100 / imageHeight;

        int screenWidth = s.channelSelector != null ? s.channelSelector.getWidth() : s.dmSelector.getWidth();
        int screenHeight = s.channelSelector != null ? s.channelSelector.getHeight() : s.dmSelector.getHeight();
        int screenAspectRatio = screenWidth*100 / screenHeight;

        int width, height;

        if (imageWidth <= screenWidth && imageHeight <= screenHeight) {
            width = imageWidth;
            height = imageHeight;
        }
        else if (imageAspectRatio > screenAspectRatio) {
            width = screenWidth;
            height = (screenWidth*100)/imageAspectRatio;
        } else {
            height = screenHeight;
            width = (screenHeight*imageAspectRatio)/100;
        }

        url = data.getString("proxy_url") + "width=" + width + "&height=" + height + "&format=" + (s.useJpeg ? "jpeg" : "png");
        url = s.cdn + url.substring("https://media.discordapp.net".length());
    }
}
