package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public abstract class MyCanvas extends Canvas {
//#ifdef MIDP2_GENERIC
    MyCanvas() {
        if (Util.isKemulator) setFullScreenMode(true);
    }

    protected void paint(Graphics g) {}
//#endif

    public void _paint(Graphics g) {
        paint(g);
    }

//#ifdef SAMSUNG_FULL
    private static boolean hasDoneSamsungFontFix;

    protected void showNotify() {
        // On Samsung Jet S8000 (tested with S800MCEIK1 firmware) the first canvas that is shown
        // in a Java app will have fonts that are way too small (approx 16px on a 480p display).
        // The solution is to reload the fonts and the main menu.
        // More about this in Util.java
        if (Util.hasSamsungFontBug && !hasDoneSamsungFontFix) {
            App.loadFonts();
            App.disp.setCurrent(reload());
            hasDoneSamsungFontFix = true;
        }
    }

    // overridden when needed
    protected MyCanvas reload() {
        return null;
    }
//#endif

//#ifdef MIDP2_GENERIC
    public void setTitle(String title) {
        if (Util.isKemulator && !"Discord".equals(title) && title != null) {
            super.setTitle("Discord - " + title);
        } else {
            super.setTitle(title);
        }
    }
//#endif

    protected void keyAction(int key) {}

    private static volatile boolean isKeyPressed = false;
    public static volatile long beginRepeatTime;

    protected void keyPressed(int key) {
//#ifdef MIDP2_GENERIC
        // Ignore home button presses which would otherwise deactivate touch mode
        if (Util.isSymbian && key == -12) return;
//#endif
        keyAction(key);

        if (Threads100kb.enabled && !isKeyPressed) {
            isKeyPressed = true;
            beginRepeatTime = System.currentTimeMillis() + 500;
            synchronized (Threads100kb.instance) {
                Threads100kb.activeKey = key;
                Threads100kb.instance.notify();
            }
        }
    }

    protected void keyReleased(int key) {
        if (Threads100kb.enabled && isKeyPressed) {
            isKeyPressed = false;
            Threads100kb.activeKey = 0;
        }
    }

    protected void keyRepeated(int key) {
//#ifdef MIDP2_GENERIC
        if (Util.isSymbian && key == -12) return;
//#endif
        if (!Threads100kb.enabled) keyAction(key);
    }

//#ifdef NOKIA_THEME_BACKGROUND
    private boolean fullscreen = false;

    public void setFullScreenMode(boolean mode) {
        fullscreen = mode;
        super.setFullScreenMode(mode);
    }
//#endif

//#ifdef BLACKBERRY
    protected int bbTitleHeight;
    private static final Font bbTitleFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    protected void bbDrawTitle(Graphics g) {
        bbTitleHeight = g.getTranslateY();

        if (bbTitleHeight != 0) {
            String title = getTitle();
            if (title == null) title = "Discord";

            g.setColor(0xEEEEFF);
            g.fillRect(0, -bbTitleHeight, getWidth(), bbTitleHeight);

            g.setColor(0x111111);
            g.setFont(bbTitleFont);
            g.drawString(title, getWidth()/2, -bbTitleHeight, Graphics.HCENTER | Graphics.TOP);
            g.drawLine(0, -1, getWidth(), -1);

            g.setClip(0, 0, getWidth(), getHeight());
        }
    }
//#endif

    protected void clearScreen(Graphics g, int color) {
        // On BlackBerry, the clip is set by default to (0, -y, width, height+y), where y is the height of the title bar. This means that apps can draw stuff over the title bar.
        // We'll draw a custom title bar over the default one, then set a new clip so nothing else in the app can draw over it.
//#ifdef BLACKBERRY
        bbDrawTitle(g);
//#endif

//#ifdef NOKIA_THEME_BACKGROUND
        if (Settings.theme != Theme.SYSTEM || fullscreen)
//#endif
        {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());

//#ifdef NOKIA_THEME_BACKGROUND
            // Fix white border rendering bug on Symbian 9.3 - 9.4
            if (!fullscreen) {
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
//#endif
        }
    }

//#ifdef TOUCH_SUPPORT
    protected void _pointerPressed(int x, int y) {}
    protected void _pointerDragged(int x, int y) {}
    protected void _pointerReleased(int x, int y) {}
//#endif

//#ifdef BLACKBERRY
    protected void pointerPressed(int x, int y) {
        _pointerPressed(x, y - bbTitleHeight);
    }

    protected void pointerDragged(int x, int y) {
        _pointerDragged(x, y - bbTitleHeight);
    }

    protected void pointerReleased(int x, int y) {
        _pointerReleased(x, y - bbTitleHeight);
    }
//#else
//#ifdef TOUCH_SUPPORT
    protected void pointerPressed(int x, int y) {
        _pointerPressed(x, y);
    }

    protected void pointerDragged(int x, int y) {
        _pointerDragged(x, y);
    }

    protected void pointerReleased(int x, int y) {
        _pointerReleased(x, y);
    }
//#endif
//#endif
}