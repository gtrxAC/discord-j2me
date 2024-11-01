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
        append(new Spacer(getWidth(), spacerHeight));
        addString(name, Font.STYLE_BOLD, Font.SIZE_SMALL, LAYOUT_DEFAULT);
        addString(desc, Font.SIZE_SMALL, LAYOUT_DEFAULT);
        append(new Spacer(getWidth(), spacerHeight));
    }

    AboutForm(State s) {
        super("About");
        setCommandListener(this);
        this.s = s;

        try {
            appIcon = Image.createImage("/icon.png");
            append(new ImageItem(null, appIcon, LAYOUT_CENTER, null));
        }
        catch (Exception e) {}
        
        addString("Discord J2ME", Font.SIZE_MEDIUM, LAYOUT_CENTER);

        String versionStr = "Version " + s.midlet.getAppProperty("MIDlet-Version") + " (beta)";
        addString(versionStr, Font.SIZE_SMALL, LAYOUT_CENTER);
        append(new Spacer(getWidth(), spacerHeight));

        addString("Developers", Font.SIZE_MEDIUM, LAYOUT_CENTER);
        //
        append(new Spacer(getWidth(), spacerHeight));

        addString("Contributors", Font.SIZE_MEDIUM, LAYOUT_CENTER);
        //

        addCommand(Locale.createCommand(BACK, Command.BACK, 0));
    }
    
    public void commandAction(Command c, Displayable d) {
        s.disp.setCurrent(MainMenu.get(null));
    }
}