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
//#ifdef PIGLER_SUPPORT
    Image pigler;
//#endif
    Image about;
//#ifdef NOKIA_UI_ICON
    Image nokiaUI;
//#endif
//#ifdef J2ME_LOADER
    Image android;
//#endif
    Image vibra;
//#ifdef EMOJI_SUPPORT
    Image emoji;
//#endif
//#ifdef OVER_100KB
    Image markdown;
    Image typing;
    Image dataManager;
//#endif
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
    Image flagBG;
    Image flagMY;
    Image flagCA;
    Image fastScroll;
//#ifdef OVER_100KB
    Image themeCustom;
//#endif
    Image flagJP;
    Image flagAR;
    Image flagCN;

    Icons() {
        if (Settings.menuIconSize == 0) return;

        Spritesheet sh;
        try {
            sh = new Spritesheet("/icons.png", Settings.menuIconSize);
        }
        catch (Exception e) {
            return;
        }

        favorites = sh.next();
        dms = sh.next();
        guilds = sh.next();
        settings = sh.next();
        logout = sh.next();
        themesGroup = sh.next();
        themeDark = sh.next();
        themeLight = sh.next();
        themeBlack = sh.next();
        uiGroup = sh.next();
        use12h = sh.next();
        nativePicker = sh.next();
        autoReconnect = sh.next();
        menuIcons = sh.next();
        nameColors = sh.next();
        nameColorsOff = sh.next();
        fontSmall = sh.next();
        fontMedium = sh.next();
        fontLarge = sh.next();
        msgCount = sh.next();
        attachFormat = sh.next();
        attachSize = sh.next();
        pfpNone = sh.next();
        pfpSquare = sh.next();
        pfpCircle = sh.next();
        pfpCircleHq = sh.next();
        pfpPlaceholder = sh.next();
        pfp16 = sh.next();
        pfp32 = sh.next();
        repliesName = sh.next();
        repliesFull = sh.next();
        keys = sh.next();
        keysDefault = sh.next();
        iconSize = sh.next();
        language = sh.next();
        notify = sh.next();
        notifyPing = sh.next();
        notifyDM = sh.next();
        notifyAlert = sh.next();
        notifySound = sh.next();
        fullscreen = sh.next();
        keepChLoaded = sh.next();
        scrollBars = sh.next();
        sh.skip();
        about = sh.next();
        sh.skip(3);
        vibra = sh.next();
        sh.skip(4);
        fastScroll = sh.next();
        sh.skip();
        flagGB = sh.next();
        flagUS = sh.next();
        flagES = sh.next();
        flagFR = sh.next();
        flagDE = sh.next();
        flagIT = sh.next();
        flagNL = sh.next();
        flagPT = sh.next();
        flagUK = sh.next();
        flagFI = sh.next();
        flagPL = sh.next();
        flagID = sh.next();
        flagTR = sh.next();
        flagBR = sh.next();
        flagRU = sh.next();
        flagRO = sh.next();
        flagVI = sh.next();
        flagSV = sh.next();
        flagTH = sh.next();
        flagTW = sh.next();
        flagHK = sh.next();
        flagHR = sh.next();
        flagBG = sh.next();
        flagMY = sh.next();
        flagCA = sh.next();
        flagJP = sh.next();
        flagAR = sh.next();
        flagCN = sh.next();
    }
}
