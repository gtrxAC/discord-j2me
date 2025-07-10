package com.gtrxac.discord;

import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * Canvas-based replacement for LCDUI Alert.
 */
public class Dialog
// ifdef OVER_100KB
extends KineticScrollingCanvas
// else
extends MyCanvas
// endif
implements CommandListener
{
    public static String okLabel;
    public static String okLabelLong;
    private static Image overlay;

    public String text;
    private String[] textLines;
    public Displayable lastScreen;
    public Displayable nextScreen;
    private int fontHeight;
    // ifdef OVER_100KB
    private boolean isScrollable;
    // endif

    public Command DISMISS_COMMAND;
    private int commandCount;
    private CommandListener listener;

    Dialog(String title, String text) {
        this(title, text, null);
    }

    Dialog(String title, String text, Displayable nextScreen) {
        super();
        checkInitOverlay();
        super.setCommandListener(this);

        lastScreen = App.disp.getCurrent();
        this.nextScreen = (nextScreen != null) ? nextScreen : lastScreen;

        // Don't show title bar if the previous screen didn't have a title bar
        if (lastScreen == null || lastScreen.getTitle() != null) setTitle(title);

        fontHeight = ListScreen.font.getHeight();
        // ifdef OVER_100KB
        scrollUnit = fontHeight;
        // endif
        commandCount = 0;
        DISMISS_COMMAND = new Command(okLabel, okLabelLong, Command.BACK, 0);
        super.addCommand(DISMISS_COMMAND);

        setString(text);
    }

    private static void checkInitOverlay() {
        if (overlay == null && App.disp.numAlphaLevels() > 2) {
            try {
                overlay = Image.createImage("/overlay.png");
            }
            catch (Exception e) {}
        }
    }

    // ifdef OVER_100KB
    protected int getMinScroll() {
        return 0;
    }

    protected int getMaxScroll() {
        if (!isScrollable) return 0;
        return (textLines.length + 2)*fontHeight - getHeight();
    }
    // endif

    public int getContentWidth() {
        return Math.min(getWidth(), fontHeight*20);
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
        this.textLines = Util.wordWrap(text, getContentWidth() - fontHeight*2, ListScreen.font);
        // ifdef OVER_100KB
        this.isScrollable = (textLines.length*fontHeight > getHeight() - fontHeight/2);
        // endif
        repaint();
    }

    protected void paint(Graphics g) {
        // ifdef OVER_100KB
        checkScrollInRange();
        // endif

        g.setClip(0, 0, getWidth(), getHeight());
        int themeBg = Theme.dialogBackgroundColor;

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
            // ifdef NOKIA_THEME_BACKGROUND
            if (Settings.theme != Theme.SYSTEM)
            // endif
            {
                g.setColor(themeBg);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            try {
                ((MyCanvas) behindScreen)._paint(g);
            }
            catch (Exception e) {}

            g.setClip(0, 0, getWidth(), getHeight());
    
            // Draw overlay (grid of 64×64 black square images with 70% opacity)
            for (int y = 0; y < getHeight(); y += 64) {
                for (int x = 0; x < getWidth(); x += 64) {
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
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Centered card with actual theme background color
        int baseX = (getWidth() - getContentWidth())/2;
        int y =
            // ifdef OVER_100KB
            isScrollable ? fontHeight - scroll :
            // endif
            getHeight()/2 - textLines.length*fontHeight/2;
        
        g.setColor(themeBg);
        g.fillRoundRect(
            baseX + fontHeight/2,
            y - fontHeight/2,
            getContentWidth() - fontHeight,
            textLines.length*fontHeight + fontHeight,
            fontHeight/2,
            fontHeight/2
        );

        g.setFont(ListScreen.font);
        g.setColor(Theme.dialogTextColor);
        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], baseX + fontHeight, y, Graphics.TOP | Graphics.LEFT);
            y += fontHeight;
        }

        // ifdef OVER_100KB
        drawScrollbar(g);
        // endif
    }

    public void setCommandListener(CommandListener l) {
        listener = l;
    }

    public void commandAction(Command c, Displayable d) {
        if (listener == null) {
            App.disp.setCurrent(nextScreen);
        } else {
            listener.commandAction(c, d);
        }
    }
    
    // ifdef OVER_100KB
    protected void keyAction(int keycode) {
        if (!isScrollable) return;

        switch (getGameAction(keycode)) {
            case UP: {
                scroll -= fontHeight*3;
                break;
            }
            case DOWN: {
                scroll += fontHeight*3;
                break;
            }
        }
        repaint();
    }
    // endif
}