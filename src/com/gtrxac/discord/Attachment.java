package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Attachment implements Strings
//#ifdef INLINE_ATTACHMENTS
, HasIcon
//#endif
{
    public String previewUrl;
    public String browserUrl;
//#ifdef INLINE_ATTACHMENTS
    public String inlineUrl;
    public boolean isVideo;
//#endif
    public String name;
    public String size;
    public boolean supported;
    public boolean isText;
//#ifdef OVER_100KB
    public boolean isAudio;
//#endif

    public static final String[] nonTextFormats = {
        ".zip", ".rar", ".7z",
        ".exe", ".jar", ".sis", ".sisx", ".bin", ".elf", ".vxp", ".mrp",
        ".nth", ".thm",
        ".mp4", ".3gp", ".bmp"
    };

    public static final String[] audioFormats = {
        ".aac", ".mp3", ".wav", ".ogg", ".m4a", ".amr", ".awb", ".flac", ".mid", ".mmf", ".mxmf", ".ott", ".rng", ".imy"
    };

    public static final String[] videoFormats = {
        ".mp4", ".3gp", ".avi", ".webm", ".mkv", ".mov", ".wmv"
    };

    public static String fileSizeToString(int size) {
        if (size >= 1000000) return "" + size/1000000 + Locale.get(SIZE_MB);
        if (size >= 1000) return "" + size/1000 + Locale.get(SIZE_KB);
        return "" + size + Locale.get(SIZE_BYTES);
    }

    public Attachment(JSONObject data) {
        String proxyUrl = data.getString("proxy_url");

        browserUrl = Settings.cdn + proxyUrl.substring("https://media.discordapp.net".length());

        name = data.getString("filename", Locale.get(UNNAMED_FILE));
        size = fileSizeToString(data.getInt("size", 0));

//#ifndef INLINE_ATTACHMENTS
        boolean
//#endif
        isVideo = (Util.indexOfAny(name.toLowerCase(), videoFormats, 0) != -1);

        // Attachments that aren't images or videos are unsupported
        // (cannot be previewed but can be viewed as text or downloaded)
        if (!data.has("width")) {
            supported = false;

//#ifdef SAMSUNG_100KB
            boolean
//#endif
            isAudio = (Util.indexOfAny(name.toLowerCase(), audioFormats, 0) != -1);

            // Can be viewed as text if it's not one of the blacklisted file extensions
            isText = !isAudio && !isVideo && (Util.indexOfAny(name.toLowerCase(), nonTextFormats, 0) == -1);

            return;
        }

        supported = true;
        int imageWidth = data.getInt("width");
        int imageHeight = data.getInt("height");

        MainMenu menu = MainMenu.get(false);
        int screenWidth = menu.getWidth();
        int screenHeight = menu.getHeight();

        int[] size = Util.resizeFit(imageWidth, imageHeight, screenWidth, screenHeight);

        // Preview url is not using our own proxy, because media.discordapp.net works over http
        String scaledUrlBase = "http://" + proxyUrl.substring("https://".length()) +
            "format=" + (Settings.useJpeg ? "jpeg" : "png") + "&width=";

        previewUrl = scaledUrlBase + size[0] + "&height=" + size[1];

//#ifdef INLINE_ATTACHMENTS
        int inlineSize = screenWidth/5;
        inlineUrl = scaledUrlBase + inlineSize + "&height=" + inlineSize;
//#endif

        // Don't resize when opening in browser if the image is smaller than the configured max attachment size, or if it's a video
        // If resizing, use media.discordapp.net instead of CDN proxy
        if ((imageWidth >= Settings.attachmentSize || imageHeight >= Settings.attachmentSize) && !isVideo) {
            int[] browserSize = Util.resizeFit(imageWidth, imageHeight, Settings.attachmentSize, Settings.attachmentSize);
            browserUrl =
                "http://media.discordapp.net" + browserUrl.substring(Settings.cdn.length()) +
                "width=" + browserSize[0] + "&height=" + browserSize[1];
        }
    }

//#ifdef INLINE_ATTACHMENTS
    public String getIconID() {
        return null;
    }

    public String getIconHash() {
        return inlineUrl;
    }

    public String getIconType() {
        return null;
    }

    public void iconLoaded() {
		if (App.channelView != null) App.channelView.repaint();
    }

    public boolean isDisabled() {
        return false;
    }
//#endif
}
