package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Attachment {
    public String previewUrl;
    public String name;
    public String size;
    public boolean supported;

    public Attachment(State s, JSONObject data) {
        String proxyUrl = data.getString("proxy_url");

        name = data.getString("filename", "Unnamed file");
        size = Util.fileSizeToString(data.getInt("size", 0));

        // Attachments that aren't images or videos are unsupported
        // (cannot be previewed)
        if (!data.has("width")) {
            supported = false;
            return;
        }

        supported = true;
        int imageWidth = data.getInt("width");
        int imageHeight = data.getInt("height");

        DummyCanvas canvas = new DummyCanvas();
        int screenWidth = canvas.getWidth();
        int screenHeight = canvas.getHeight();

        int[] size = Util.resizeFit(imageWidth, imageHeight, screenWidth, screenHeight);

        // Preview url is not using our own proxy, because media.discordapp.net works over http
        previewUrl =
            "http://" + proxyUrl.substring("https://".length()) +
            "format=" + (s.useJpeg ? "jpeg" : "png") + "&width=" + size[0] + "&height=" + size[1];
    }
}
