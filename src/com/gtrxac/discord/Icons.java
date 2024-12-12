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
    Image use12h;
    Image nativePicker;
    Image autoReconnect;
    Image menuIcons;
    Image nameColors;
    Image nameColorsOff;
    Image fontSmall;
    Image fontMedium;
    Image fontLarge;
    Image msgCount;
    Image attachFormat;
    Image attachSize;
    Image pfpNone;
    Image pfpSquare;
    Image pfpCircle;
    Image pfpCircleHq;
    Image pfpPlaceholder;
    Image pfp16;
    Image pfp32;
    Image repliesName;
    Image repliesFull;
    Image keys;
    Image keysDefault;
    Image iconSize;
    Image language;
    Image notify;
    Image notifyPing;
    Image notifyDM;
    Image notifyAlert;
    Image notifySound;
    Image fullscreen;
    Image keepChLoaded;
    Image scrollBars;
    // ifdef PIGLER_SUPPORT
    Image pigler;
    // endif
    Image about;
    Image autoUpdate;
    // ifdef NOKIA_UI_ICON
    Image nokiaUI;
    // endif
    // ifdef J2ME_LOADER
    Image android;
    // endif
    // Image vibra;
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
    Image flagTH;
    Image flagTW;
    Image flagHK;
    Image flagHR;

    Image sheet;
    int size;
    int x;
    int y;

    private Image next() {
        Image result = Image.createImage(sheet, x, y, 16, 16, Sprite.TRANS_NONE);
        
        // Integer scale to nearest multiple of 16px, rounding up
        if (size > 16) {
            int multiple = size/16*16;
            if (multiple < size) multiple += 16;
            result = Util.resizeImage(result, multiple, multiple);
        }
        // If requested icon size is not an integer multiple, scale down to requested size with bilinear filter
        if (size % 16 != 0) {
            result = Util.resizeImageBilinear(result, size, size);
        }

        x += 16;
        if (x >= sheet.getWidth()) {
            x = 0;
            y += 16;
        }
        return result;
    }

    Icons(State s) {
        size = s.menuIconSize;
        if (size == 0) return;

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
        use12h = next();
        nativePicker = next();
        autoReconnect = next();
        menuIcons = next();
        nameColors = next();
        nameColorsOff = next();
        fontSmall = next();
        fontMedium = next();
        fontLarge = next();
        msgCount = next();
        attachFormat = next();
        attachSize = next();
        pfpNone = next();
        pfpSquare = next();
        pfpCircle = next();
        pfpCircleHq = next();
        pfpPlaceholder = next();
        pfp16 = next();
        pfp32 = next();
        repliesName = next();
        repliesFull = next();
        keys = next();
        keysDefault = next();
        iconSize = next();
        language = next();
        notify = next();
        notifyPing = next();
        notifyDM = next();
        notifyAlert = next();
        notifySound = next();
        fullscreen = next();
        keepChLoaded = next();
        scrollBars = next();
        // ifdef PIGLER_SUPPORT
        pigler =
        // endif
        next();
        about = next();
        autoUpdate = next();
        // ifdef NOKIA_UI_ICON
        nokiaUI =
        // endif
        next();
        // ifdef J2ME_LOADER
        android =
        // endif
        next();
        next(); // vibra (not used yet)
        next(); // blank space
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
        flagRO = next();
        flagVI = next();
        flagSV = next();
        flagTH = next();
        flagTW = next();
        flagHK = next();
        flagHR = next();

        sheet = null;
    }
}