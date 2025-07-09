package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public abstract class MyCanvas extends Canvas {
    // ifdef MIDP2_GENERIC
    MyCanvas() {
        if (Util.isKemulator) setFullScreenMode(true);
    }

    protected void paint(Graphics g) {}
    // endif

    public void _paint(Graphics g) {
        paint(g);
    }

    // ifdef SAMSUNG_FULL
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
    // endif

    // ifdef MIDP2_GENERIC
    public void setTitle(String title) {
        if (Util.isKemulator && !"Discord".equals(title) && title != null) {
            super.setTitle("Discord - " + title);
        } else {
            super.setTitle(title);
        }
    }
    // endif

    protected void keyAction(int key) {}

    // ifdef OVER_100KB
    private static volatile boolean isKeyPressed = false;
    public static volatile long beginRepeatTime;
    // endif

    protected void keyPressed(int key) {
        keyAction(key);

        // ifdef OVER_100KB
        if (KeyRepeatThread.enabled && !isKeyPressed) {
            isKeyPressed = true;
            beginRepeatTime = System.currentTimeMillis() + 500;
            synchronized (KeyRepeatThread.instance) {
                KeyRepeatThread.activeKey = key;
                KeyRepeatThread.instance.notify();
            }
        }
        // endif
    }

    // ifdef OVER_100KB
    protected void keyReleased(int key) {
        if (KeyRepeatThread.enabled && isKeyPressed) {
            isKeyPressed = false;
            KeyRepeatThread.activeKey = 0;
        }
    }
    // endif

    protected void keyRepeated(int key) {
        // ifdef OVER_100KB
        if (!KeyRepeatThread.enabled)
        // endif
        keyAction(key);
    }

    // ifdef NOKIA_THEME_BACKGROUND
    private boolean fullscreen = false;

    public void setFullScreenMode(boolean mode) {
        fullscreen = mode;
        super.setFullScreenMode(mode);
    }
    // endif

    protected void clearScreen(Graphics g, int color) {
        // On BlackBerry, the clip is set by default to (0, -y, width, height+y), where y is the height of the title bar. This means that apps can draw stuff over the title bar. We don't want to do that.
        // ifdef BLACKBERRY
        g.setClip(0, 0, getWidth(), getHeight());
        // endif

        // ifdef NOKIA_THEME_BACKGROUND
        if (Settings.theme != Theme.SYSTEM || fullscreen)
        // endif
        {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}