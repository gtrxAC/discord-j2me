package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Attachment {
    public String url;
    public String sizeParam;
    public String browserSizeParam;
    public String name;
    public String size;
    public boolean supported;

    public Attachment(State s, JSONObject data) {
        url = s.cdn + data.getString("proxy_url").substring("https://media.discordapp.net".length());

        name = data.getString("filename", "Unnamed file");
        size = Util.fileSizeToString(data.getInt("size", 0));

        // Attachments that aren't images or videos are unsupported
        // (cannot be previewed but can be viewed as text or downloaded)
        if (!data.has("width")) {
            supported = false;
            return;
        }

        supported = true;
        int imageWidth = data.getInt("width");
        int imageHeight = data.getInt("height");

        int screenWidth = s.channelSelector != null ? s.channelSelector.getWidth() : s.dmSelector.getWidth();
        int screenHeight = s.channelSelector != null ? s.channelSelector.getHeight() : s.dmSelector.getHeight();

        int[] size = Util.resizeFit(imageWidth, imageHeight, screenWidth, screenHeight);
        int[] browserSize = Util.resizeFit(imageWidth, imageHeight, s.attachmentSize, s.attachmentSize);

        sizeParam = "format=" + (s.useJpeg ? "jpeg" : "png") + "&width=" + size[0] + "&height=" + size[1];

        // Don't resize when opening in browser if the image is smaller than the attachment size, or if it's a video
        if (
            (imageWidth <= s.attachmentSize && imageHeight <= s.attachmentSize) ||
            url.indexOf(".mp4") != -1 || url.indexOf(".mov") != -1
        ) {
            browserSizeParam = "";
        } else {
            browserSizeParam = "width=" + browserSize[0] + "&height=" + browserSize[1];
        }
    }
}
