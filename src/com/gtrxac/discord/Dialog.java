package com.gtrxac.discord;

import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * Canvas-based replacement for LCDUI Alert.
 */
public class Dialog extends Canvas implements CommandListener {
    public static String okLabel;
    public static String okLabelLong;

    public String text;
    private String[] textLines;
    private Display disp;
    private Displayable nextScreen;
    private int fontHeight;

    public Command DISMISS_COMMAND;
    private int commandCount;
    private CommandListener listener;

    Dialog(Display disp, String title, String text) {
        this(disp, title, text, null);
    }
    Dialog(Display disp, String title, String text, Displayable nextScreen) {
        super();
        setTitle(title);
        super.setCommandListener(this);

        this.disp = disp;
        this.nextScreen = (nextScreen != null) ? nextScreen : disp.getCurrent();

        fontHeight = ListScreen.font.getHeight();
        commandCount = 0;
        DISMISS_COMMAND = new Command(okLabel, okLabelLong, Command.BACK, 0);
        super.addCommand(DISMISS_COMMAND);

        setString(text);
    }

    public int getWidth() {
        return Math.min(super.getWidth(), fontHeight*20);
    }

    public void setString(String str) {
        text = str;
        sizeChanged(0, 0);
    }

    public void addCommand(Command c) {
        super.removeCommand(DISMISS_COMMAND);
        super.addCommand(c);
        commandCount++;
    }

    public void removeCommand(Command c) {
        super.removeCommand(c);
        commandCount--;
        if (commandCount == 0) {
            super.addCommand(DISMISS_COMMAND);
        }
    }

    protected void sizeChanged(int w, int h) {
        this.textLines = Util.wordWrap(text, getWidth() - fontHeight*2, ListScreen.font);
        repaint();
    }

    protected void paint(Graphics g) {
        int themeBg = ListScreen.backgroundColor;
        int background =
            (themeBg & 0xFF0000) >> 17 << 16
            | (themeBg & 0xFF00) >> 9 << 8
            | (themeBg & 0xFF) >> 1;

        // Darkened background
        g.setColor(background);
        g.setClip(0, 0, super.getWidth(), getHeight());
        g.fillRect(0, 0, super.getWidth(), getHeight());

        // Centered card with actual theme background color
        int baseX = (super.getWidth() - getWidth())/2;
        int y = getHeight()/2 - textLines.length*fontHeight/2;
        
        g.setColor(themeBg);
        g.fillRoundRect(
            baseX + fontHeight/2,
            y - fontHeight/2,
            getWidth() - fontHeight,
            textLines.length*fontHeight + fontHeight,
            fontHeight/2,
            fontHeight/2
        );

        g.setFont(ListScreen.font);
        g.setColor(ListScreen.textColor);
        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], baseX + fontHeight, y, Graphics.TOP | Graphics.LEFT);
            y += fontHeight;
        }
    }

    public void setCommandListener(CommandListener l) {
        listener = l;
    }

    public void commandAction(Command c, Displayable d) {
        if (listener == null) {
            disp.setCurrent(nextScreen);
        } else {
            listener.commandAction(c, d);
        }
    }
}