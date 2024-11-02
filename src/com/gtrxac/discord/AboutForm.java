package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AboutForm extends Form implements CommandListener, Strings {
    private State s;
    private Image appIcon;

    private static final int spacerHeight = Font.getDefaultFont().getHeight()/8;

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

    private void addDeveloper(String name, String desc) {
        addSpacer();
        addString(name, Font.STYLE_BOLD, Font.SIZE_SMALL, LAYOUT_DEFAULT);
        addString(desc, Font.SIZE_SMALL, LAYOUT_DEFAULT);
        addSpacer();
    }

    private void addSpacer() {
        append(new Spacer(getWidth(), spacerHeight));
    }

    AboutForm(State s) {
        super("About");
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

        String versionStr = "Version " + s.midlet.getAppProperty("MIDlet-Version") + " (beta)";
        addString(versionStr, Font.SIZE_SMALL, LAYOUT_CENTER);
        addSpacer();

        addString("Developers", Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addDeveloper("gtrxAC", "Lead developer");
        addDeveloper("Shinovon", "Attachment loading, Pigler API integration, JSON parser");
        addDeveloper("Saetta06", "Loading animation, menu icons, Italian translation");
        addSpacer();

        addString("Translators", Font.SIZE_MEDIUM, LAYOUT_CENTER);
        addDeveloper("ACPI Fixed Feature Button", "Vietnamese");
        addDeveloper("Alex222", "Swedish");
        addDeveloper("Borsain", "Indonesian");
        addDeveloper("cappuchi", "Polish");
        addDeveloper("ElHamexd", "Spanish");
        addDeveloper("Kaiky Alexandre Souza", "Portuguese (Brazil)");
        addDeveloper("logoffon", "Thai");
        addDeveloper("pdyq", "Chinese (Traditional) and Cantonese");
        addDeveloper("proxion", "Ukrainian");
        addDeveloper("raul0028", "Romanian");
        addDeveloper("SpiroWalkman\nmultiplemegapixels", "Russian");
        addDeveloper("violent body", "Turkish");
        
        addString("Support", Font.SIZE_MEDIUM, LAYOUT_CENTER);
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