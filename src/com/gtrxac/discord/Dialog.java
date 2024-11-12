package com.gtrxac.discord;

import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * Canvas-based replacement for LCDUI Alert.
 */
public class Dialog extends MyCanvas implements CommandListener {
    public static String okLabel;
    public static String okLabelLong;
    private static Image overlay;

    public String text;
    private String[] textLines;
    private Display disp;
    public Displayable lastScreen;
    public Displayable nextScreen;
    private int fontHeight;

    public Command DISMISS_COMMAND;
    private int commandCount;
    private CommandListener listener;

    Dialog(Display disp, String title, String text) {
        this(disp, title, text, null);
    }
    Dialog(Display disp, String title, String text, Displayable nextScreen) {
        super();
        checkInitOverlay(disp);
        super.setCommandListener(this);

        this.disp = disp;
        lastScreen = disp.getCurrent();
        this.nextScreen = (nextScreen != null) ? nextScreen : lastScreen;

        // Don't show title bar if the previous screen didn't have a title bar
        if (lastScreen == null || lastScreen.getTitle() != null) setTitle(title);

        fontHeight = ListScreen.font.getHeight();
        commandCount = 0;
        DISMISS_COMMAND = new Command(okLabel, okLabelLong, Command.BACK, 0);
        super.addCommand(DISMISS_COMMAND);

        setString(text);
    }

    private static void checkInitOverlay(Display disp) {
        if (overlay == null && disp.numAlphaLevels() > 2) {
            try {
                overlay = Image.createImage("/overlay.png");
            }
            catch (Exception e) {}
        }
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
        g.setClip(0, 0, super.getWidth(), getHeight());
        int themeBg = ListScreen.backgroundColor;

        // Get the screen that should be drawn behind this one
        // If this Dialog is stacked above another Dialog, get the last non-Dialog screen
        Displayable behindScreen = lastScreen;
        while (behindScreen instanceof Dialog) {
            behindScreen = ((Dialog) behindScreen).lastScreen;
        }

        // If possible, draw last screen behind a darkened overlay
        // MyCanvas has a _paint() proxy method for protected paint() that lets us do this
        if (overlay != null && behindScreen instanceof MyCanvas) {
            // When changing screen size, the _paint() method will keep using the old width/height.
            // There may be a proper fix for this, but for now, we just fill the background with the theme
            // background color (so at least there isn't a white background)
            g.setColor(themeBg);
            g.fillRect(0, 0, super.getWidth(), getHeight());

            ((MyCanvas) behindScreen)._paint(g);

            g.setClip(0, 0, super.getWidth(), getHeight());
    
            // Draw overlay (grid of 64×64 black square images with 70% opacity)
            for (int y = 0; y < getHeight(); y += 64) {
                for (int x = 0; x < super.getWidth(); x += 64) {
                    g.drawImage(overlay, x, y, Graphics.TOP | Graphics.LEFT);
                }
            }
        } else {
            // Not possible to draw the next screen, or display doesn't support alpha blending:
            // fill background with darkened version of theme background color
            int background =
                (themeBg & 0xFF0000) >> 18 << 16
                | (themeBg & 0xFF00) >> 10 << 8
                | (themeBg & 0xFF) >> 2;

            g.setColor(background);
            g.fillRect(0, 0, super.getWidth(), getHeight());
        }

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