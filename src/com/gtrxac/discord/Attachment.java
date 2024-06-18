package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Attachment {
    public String url;
    public String sizeParam;
    public String browserSizeParam;

    public static int[] resizeFit(int imgW, int imgH, int maxW, int maxH) {
        int imgAspect = imgW*100 / imgH;
        int maxAspect = maxW*100 / maxH;
        int width, height;

        if (imgW <= maxW && imgH <= maxH) {
            width = imgW;
            height = imgH;
        }
        else if (imgAspect > maxAspect) {
            width = maxW;
            height = (maxW*100)/imgAspect;
        } else {
            height = maxH;
            width = (maxH*imgAspect)/100;
        }

        return new int[]{width, height};
    }

    public Attachment(State s, JSONObject data) {
        int imageWidth = data.getInt("width");
        int imageHeight = data.getInt("height");

        int screenWidth = s.channelSelector != null ? s.channelSelector.getWidth() : s.dmSelector.getWidth();
        int screenHeight = s.channelSelector != null ? s.channelSelector.getHeight() : s.dmSelector.getHeight();

        int[] size = resizeFit(imageWidth, imageHeight, screenWidth, screenHeight);
        int[] browserSize = resizeFit(imageWidth, imageHeight, s.attachmentSize, s.attachmentSize);

        url = s.cdn + data.getString("proxy_url").substring("https://media.discordapp.net".length());

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
