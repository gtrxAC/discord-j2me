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
    Image about;
    Image autoUpdate;
    Image vibra;
    Image markdown;
    Image typing;
    Image dataManager;
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
    Image themeCustom;
    Image flagJP;
    Image flagAR;
    Image flagCN;
    Image flagHU;

    public static int TYPE_MAIN_MENU = 0;  // subset of icons to be loaded in the main menu and rest of the app
    public static int TYPE_SETTINGS = 1;  // settings and data manager
    public static int TYPE_LANGUAGE = 2;  // language selector

    Icons(int type) {
        if (Settings.menuIconSize == 0) return;

        Spritesheet sh;
        try {
            sh = new Spritesheet("/icons.png", Settings.menuIconSize);
        }
        catch (Exception e) {
            return;
        }

        if (type == TYPE_MAIN_MENU) {
            favorites = sh.next();
            dms = sh.next();
            guilds = sh.next();
        } else {
            sh.skip(3);
        }

        if (type != TYPE_LANGUAGE) {
            settings = sh.next();
        } else {
            sh.skip();
        }

        if (type == TYPE_MAIN_MENU) {
            logout = sh.next();
        } else {
            sh.skip();
        }

        if (type == TYPE_SETTINGS) {
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
        } else {
            sh.skip(39);
        }

        if (type == TYPE_MAIN_MENU) {
            about = sh.next();
        } else {
            sh.skip();
        }

        if (type == TYPE_SETTINGS) {
            autoUpdate = sh.next();
            sh.skip(2);
            vibra = sh.next();
            sh.skip();
            markdown = sh.next();
            typing = sh.next();
            dataManager = sh.next();
            fastScroll = sh.next();
            themeCustom = sh.next();
        }

        if (type == TYPE_LANGUAGE) {
            sh.skip(11);
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
            flagHU = sh.next();
            flagCN = sh.next();
        }
    }
}
