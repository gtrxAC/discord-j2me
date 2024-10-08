package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Attachment implements Strings {
    public String previewUrl;
    public String browserUrl;
    public String name;
    public String size;
    public boolean supported;
    public boolean isText;

    private static final String[] nonTextFormats = {
        ".zip", ".rar", ".7z",
        ".exe", ".jar", ".sis", ".sisx", ".bin",
        ".mp3", ".wav", ".ogg", ".m4a", ".amr", ".flac", ".mid", ".mmf",
        ".mp4", ".3gp", ".bmp"
    };

    public Attachment(State s, JSONObject data) {
        String proxyUrl = data.getString("proxy_url");

        browserUrl = s.cdn + proxyUrl.substring("https://media.discordapp.net".length());

        name = data.getString("filename", Locale.get(UNNAMED_FILE));
        size = Util.fileSizeToString(data.getInt("size", 0));

        // Attachments that aren't images or videos are unsupported
        // (cannot be previewed but can be viewed as text or downloaded)
        if (!data.has("width")) {
            supported = false;

            // Can be viewed as text if it's not one of the blacklisted file extensions
            isText = (Util.indexOfAny(name.toLowerCase(), nonTextFormats, 0) == -1);

            return;
        }

        supported = true;
        int imageWidth = data.getInt("width");
        int imageHeight = data.getInt("height");

        MainMenu menu = MainMenu.get(null);
        int screenWidth = menu.getWidth();
        int screenHeight = menu.getHeight();

        int[] size = Util.resizeFit(imageWidth, imageHeight, screenWidth, screenHeight);

        // Preview url is not using our own proxy, because media.discordapp.net works over http
        previewUrl =
            "http://" + proxyUrl.substring("https://".length()) +
            "format=" + (s.useJpeg ? "jpeg" : "png") + "&width=" + size[0] + "&height=" + size[1];

        // Don't resize when opening in browser if the image is smaller than the configured max attachment size, or if it's a video
        // If resizing, use media.discordapp.net instead of CDN proxy
        if (
            (imageWidth >= s.attachmentSize || imageHeight >= s.attachmentSize) &&
            browserUrl.indexOf(".mp4") == -1 && browserUrl.indexOf(".mov") == -1
        ) {
            int[] browserSize = Util.resizeFit(imageWidth, imageHeight, s.attachmentSize, s.attachmentSize);
            browserUrl =
                "http://media.discordapp.net" + browserUrl.substring(s.cdn.length()) +
                "width=" + browserSize[0] + "&height=" + browserSize[1];
        }
    }
}
