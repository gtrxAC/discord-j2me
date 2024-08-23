package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class Icons {
    Image guilds;
    Image favorites;
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
    Image iconSize;
    Image icon16;
    Image icon32;
    Image language;
    Image flagGB;
    Image flagUS;
    Image flagES;
    Image flagFR;
    Image flagDE;
    Image flagIT;
    Image flagNL;
    Image flagPT;
    Image flagUK;
    Image flagFI;
    Image flagPL;
    Image flagID;
    Image flagTR;
    Image flagBR;
    Image flagRU;
    Image flagRO;
    Image flagSV;
    Image flagVI;

    boolean upscale;
    Image sheet;
    int x;
    int y;

    private Image next() {
        Image result = Image.createImage(sheet, x, y, 16, 16, Sprite.TRANS_NONE);
        if (upscale) result = Util.resizeImage(result, 32, 32);

        x += 16;
        if (x >= sheet.getWidth()) {
            x = 0;
            y += 16;
        }
        return result;
    }

    Icons(State s) {
        if (s.menuIconSize == State.ICON_SIZE_OFF) return;

        this.upscale = (s.menuIconSize == State.ICON_SIZE_32);

        try {
            sheet = Image.createImage("/icons.png");
        }
        catch (Exception e) {
            return;
        }

        favorites = next();
        dms = next();
        guilds = next();
        settings = next();
        logout = next();
        themesGroup = next();
        themeDark = next();
        themeLight = next();
        themeBlack = next();
        uiGroup = next();
        oldUI = next();
        use12h = next();
        nativePicker = next();
        autoReconnect = next();
        menuIcons = next();
        nameColors = next();
        fontSize = next();
        fontSmall = next();
        fontMedium = next();
        fontLarge = next();
        msgCount = next();
        attachFormat = next();
        attachSize = next();
        pfpType = next();
        pfpNone = next();
        pfpSquare = next();
        pfpCircle = next();
        pfpCircleHq = next();
        pfpSize = next();
        pfpPlaceholder = next();
        pfp16 = next();
        pfp32 = next();
        repliesGroup = next();
        repliesName = next();
        repliesFull = next();
        keys = next();
        keysDefault = next();
        iconSize = next();
        icon16 = next();
        icon32 = next();
        language = next();
        flagGB = next();
        flagUS = next();
        flagES = next();
        flagFR = next();
        flagDE = next();
        flagIT = next();
        flagNL = next();
        flagPT = next();
        flagUK = next();
        flagFI = next();
        flagPL = next();
        flagID = next();
        flagTR = next();
        flagBR = next();
        flagRU = next();

        sheet = null;
    }
}