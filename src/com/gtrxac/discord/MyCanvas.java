package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public
// ifdef MIDP2_GENERIC
// else
abstract
// endif
class MyCanvas extends Canvas {
    // ifdef MIDP2_GENERIC
    MyCanvas() {
        if (Util.isKemulator) setFullScreenMode(true);
    }

    protected void paint(Graphics g) {}
    // endif

    public void _paint(Graphics g) {
        paint(g);
    }

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
    private static boolean isKeyPressed = false;
    public static long beginRepeatTime;
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
}