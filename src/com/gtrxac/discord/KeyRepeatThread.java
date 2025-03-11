// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class KeyRepeatThread extends Thread {
    public static volatile int activeKey;
    public static volatile boolean enabled;
    public static KeyRepeatThread instance;

    public static void toggle(boolean on) {
        enabled = on;
        if (on && (instance == null || !instance.isAlive())) {
            instance = new KeyRepeatThread();
            instance.setPriority(MIN_PRIORITY);
            instance.start();
        }
    }
    
    public void run() {
        while (enabled) {
            try {
                synchronized (this) {
                    wait(0);
                }
            }
            catch (InterruptedException e) {}

            while (enabled) {
                long curr = System.currentTimeMillis();
                if (curr >= MyCanvas.beginRepeatTime) break;
                Util.sleep((int) (curr - MyCanvas.beginRepeatTime));
            }

            Displayable current = App.disp.getCurrent();
            if (!(current instanceof MyCanvas)) continue;

            while (enabled && activeKey != 0) {
                try {
                    ((MyCanvas) current).keyAction(activeKey);
                    ((MyCanvas) current).repaint();
                    Thread.sleep(50);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
// endif