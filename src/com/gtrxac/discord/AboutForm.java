package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AboutForm extends Form implements CommandListener, Strings {
    private Image appIcon;

    private static final int spacerHeight = Util.fontSize/8;

    private static final int LAYOUT_BASE =
        Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER;

    private static final int LAYOUT_DEFAULT =
        LAYOUT_BASE | Item.LAYOUT_EXPAND;

    private static final int LAYOUT_CENTER =
        LAYOUT_BASE | Item.LAYOUT_CENTER;

    private void addString(String str, int fontStyle, int fontSize, int layout) {
        StringItem item = new StringItem(null, str);
        item.setLayout(layout);
        item.setFont(Font.getFont(Font.FACE_PROPORTIONAL, fontStyle, fontSize));
        append(item);
    }

    private void addString(String str, int fontSize, int layout) {
        addString(str, Font.STYLE_PLAIN, fontSize, layout);
    }

    private void addDeveloper(String name, int descKey) {
        addSpacer();
        addString(name, Font.STYLE_BOLD, Font.SIZE_SMALL, LAYOUT_DEFAULT);
        append(new Spacer(getWidth(), 1));
        addString(Locale.get(descKey), Font.SIZE_SMALL, LAYOUT_DEFAULT);
        addSpacer();
    }

    private void addSpacer() {
        append(new Spacer(getWidth(), spacerHeight));
    }

    AboutForm() {
        super(Locale.get(ABOUT_TITLE));
        setCommandListener(this);

        try {
            appIcon = Image.createImage("/icon.png");
            int size = Settings.getBestMenuIconSize()*3;
            appIcon = Util.resizeImage(appIcon, size, size);
            append(new ImageItem(null, appIcon, LAYOUT_CENTER, null));
        }
        catch (Exception e) {}
        
        addString("Discord J2ME", Font.SIZE_MEDIUM, LAYOUT_CENTER);
        append(new Spacer(getWidth(), 1));

        String versionStr = Locale.get(VERSION) + App.VERSION_NAME + Locale.get(LEFT_PAREN) + App.VERSION_VARIANT + Locale.get(RIGHT_PAREN);
        addString(versionStr, Font.SIZE_SMALL, LAYOUT_CENTER);
        addSpacer();

        addString(Locale.get(ABOUT_DEVELOPERS), Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addDeveloper("gtrxAC", LEAD_DEVELOPER);
        addDeveloper("Shinovon", WHAT_SHINOVON_DID);
        addDeveloper("Saetta06", WHAT_SAETTA06_DID);
        // ifdef OVER_100KB
        addDeveloper("AeroPurple", WHAT_AEROPURPLE_DID);
        // endif
        addSpacer();

        addString(Locale.get(ABOUT_TRANSLATORS), Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addDeveloper("ACPI Fixed Feature Button", TRANSLATOR_VI);
        addDeveloper("AlanHudik", TRANSLATOR_HR);
        addDeveloper("Alex222", TRANSLATOR_SV);
        addDeveloper("Calsain", TRANSLATOR_ID);
        addDeveloper("cappuchi", TRANSLATOR_PL);
        addDeveloper("ElHamexd\nAlexisBlade2001", TRANSLATOR_ES);
        addDeveloper("Kaiky Alexandre Souza", TRANSLATOR_PTBR);
        addDeveloper("Lennard_105", TRANSLATOR_DE);
        addDeveloper("logoffon", TRANSLATOR_TH);
        addDeveloper("mal0gen", TRANSLATOR_PT);
        addDeveloper("Motorazr", TRANSLATOR_FR);
        addDeveloper("nativeshades", TRANSLATOR_BG);
        addDeveloper("pdyq", TRANSLATOR_ZHTW_YUE);
        addDeveloper("proxion", TRANSLATOR_UK);
        addDeveloper("raul0028", TRANSLATOR_RO);
        addDeveloper("SpazJR61\nviolent body", TRANSLATOR_TR);
        addDeveloper("SpiroWalkman\nmultiplemegapixels", TRANSLATOR_RU);
        addDeveloper("tsukihimeri6969", TRANSLATOR_JA);
        
        addString(Locale.get(ABOUT_SUPPORT), Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addSpacer();
        addString("discord.gg/2GKuJjQagp", Font.SIZE_SMALL, LAYOUT_DEFAULT);
        addSpacer();
        addString("t.me/dscforsymbian", Font.SIZE_SMALL, LAYOUT_DEFAULT);

        addCommand(Locale.createCommand(BACK, Command.BACK, 0));
    }
    
    public void commandAction(Command c, Displayable d) {
        App.disp.setCurrent(MainMenu.get(false));
    }
}