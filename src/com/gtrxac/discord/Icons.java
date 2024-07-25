package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class Icons {
    Image guilds;
    Image dms;
    Image settings;
    Image logout;
    Image themesGroup;
    Image themeDark;
    Image themeLight;
    Image themeBlack;
    Image uiGroup;
    Image oldUI;
    Image use12h;
    Image nativePicker;
    Image autoReconnect;
    Image menuIcons;
    Image nameColors;
    Image fontSize;
    Image fontSmall;
    Image fontMedium;
    Image fontLarge;
    Image msgCount;
    Image attachFormat;
    Image attachSize;
    Image pfpType;
    Image pfpNone;
    Image pfpSquare;
    Image pfpCircle;
    Image pfpCircleHq;
    Image pfpSize;
    Image pfpPlaceholder;
    Image pfp16;
    Image pfp32;
    Image repliesGroup;
    Image repliesName;
    Image repliesFull;
    Image keys;
    Image keysDefault;

    boolean upscale;

    private Image load(String path) {
        try {
            Image result = Image.createImage(path);
            if (upscale) result = Util.resizeImage(result, 32, 32);
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    Icons(boolean upscale) {
        this.upscale = upscale;

        guilds = load("/2_servers.png");
        dms = load("/1_DM.png");
        settings = load("/3_settings.png");
        logout = load("/4_logout.png");
        themesGroup = load("/5_themes.png");
        themeDark = load("/6_dark.png");
        themeLight = load("/7_white.png");
        themeBlack = load("/8_black.png");
        uiGroup = load("/9_UI.png");
        oldUI = load("/10_oldUI.png");
        use12h = load("/11_12hourtime.png");
        nativePicker = load("/12_filepicker.png");
        autoReconnect = load("/13_autogateway.png");
        menuIcons = load("/14_menuicons.png");
        nameColors = load("/15_namecolors.png");
        fontSize = load("/16_fontsize.png");
        fontSmall = load("/17_small.png");
        fontMedium = load("/18_medium.png");
        fontLarge = load("/19_large.png");
        msgCount = load("/20_msgload.png");
        attachFormat = load("/21_attachmentformat.png");
        attachSize = load("/22_maxsize.png");
        pfpType = load("/23_pfp.png");
        pfpNone = load("/24_nopfp.png");
        pfpSquare = load("/25_square.png");
        pfpCircle = load("/26_circle.png");
        pfpCircleHq = load("/27_circlehq.png");
        pfpSize = load("/28_pfpsize.png");
        pfpPlaceholder = load("/29_placeholder.png");
        pfp16 = load("/30_pfp16px.png");
        pfp32 = load("/31_pfp32px.png");
        repliesGroup = load("/32_replies.png");
        repliesName = load("/33_onlynames.png");
        repliesFull = load("/34_fullreply.png");
        keys = load("/35_keys.png");
        keysDefault = load("/36_defaultkeys.png");
    }
}