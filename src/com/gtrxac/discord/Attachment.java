package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Attachment {
    public String url;

    public Attachment(State s, JSONObject data) {
        int imageWidth = data.getInt("width");
        int imageHeight = data.getInt("height");
        int imageAspectRatio = imageWidth*100 / imageHeight;

        int screenWidth = s.oldUI ? s.oldChannelView.getWidth() : s.channelView.getWidth();
        int screenHeight = s.oldUI ? s.oldChannelView.getHeight() : s.channelView.getHeight();
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

        url = data.getString("proxy_url") + "width=" + width + "&height=" + height + "&format=png";
        url = "http://cdndsc.wunderwungiel.pl" + url.substring("https://media.discordapp.net".length());
    }
}
