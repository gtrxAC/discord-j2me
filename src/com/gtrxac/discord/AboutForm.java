package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AboutForm extends Form implements CommandListener, Strings {
    private State s;
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
        item.setFont(Font.getFont(Font.FACE_SYSTEM, fontStyle, fontSize));
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

    AboutForm(State s) {
        super(Locale.get(ABOUT_TITLE));
        setCommandListener(this);
        this.s = s;

        try {
            appIcon = Image.createImage("/icon.png");
            int size = LoginSettings.getBestMenuIconSize()*3;
            appIcon = Util.resizeImage(appIcon, size, size);
            append(new ImageItem(null, appIcon, LAYOUT_CENTER, null));
        }
        catch (Exception e) {}
        
        addString("Discord J2ME", Font.SIZE_MEDIUM, LAYOUT_CENTER);
        append(new Spacer(getWidth(), 1));

        String versionStr = Locale.get(VERSION) + State.VERSION_NAME + Locale.get(LEFT_PAREN) + State.VERSION_VARIANT + Locale.get(RIGHT_PAREN);
        addString(versionStr, Font.SIZE_SMALL, LAYOUT_CENTER);
        addSpacer();

        addString(Locale.get(ABOUT_DEVELOPERS), Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addDeveloper("gtrxAC", LEAD_DEVELOPER);
        addDeveloper("Shinovon", WHAT_SHINOVON_DID);
        addDeveloper("Saetta06", WHAT_SAETTA06_DID);
        addSpacer();

        addString(Locale.get(ABOUT_TRANSLATORS), Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addDeveloper("ACPI Fixed Feature Button", TRANSLATOR_VI);
        addDeveloper("Alex222", TRANSLATOR_SV);
        addDeveloper("Borsain", TRANSLATOR_ID);
        addDeveloper("cappuchi", TRANSLATOR_PL);
        addDeveloper("ElHamexd", TRANSLATOR_ES);
        addDeveloper("Kaiky Alexandre Souza", TRANSLATOR_PTBR);
        addDeveloper("Lennard_105", TRANSLATOR_DE);
        addDeveloper("logoffon", TRANSLATOR_TH);
        addDeveloper("mal0gen", TRANSLATOR_PT);
        addDeveloper("pdyq", TRANSLATOR_ZHTW_YUE);
        addDeveloper("proxion", TRANSLATOR_UK);
        addDeveloper("raul0028", TRANSLATOR_RO);
        addDeveloper("SpazJR61\nviolent body", TRANSLATOR_TR);
        addDeveloper("SpiroWalkman\nmultiplemegapixels", TRANSLATOR_RU);
        
        addString(Locale.get(ABOUT_SUPPORT), Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addSpacer();
        addString("discord.gg/2GKuJjQagp", Font.SIZE_SMALL, LAYOUT_DEFAULT);
        addSpacer();
        addString("t.me/dscforsymbian", Font.SIZE_SMALL, LAYOUT_DEFAULT);

        addCommand(Locale.createCommand(BACK, Command.BACK, 0));
    }
    
    public void commandAction(Command c, Displayable d) {
        s.disp.setCurrent(MainMenu.get(null));
    }
}