package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoadingAnimThread extends Thread {
    private Display disp;
    private LoadingScreen loadScreen;

    LoadingAnimThread(Display disp, LoadingScreen loadScreen) {
        this.disp = disp;
        this.loadScreen = loadScreen;
    }

    public void run() {
        // Wait for the load screen to show up
        while (disp.getCurrent() != loadScreen) {
            try {
                Thread.sleep(10);
            }
            catch (Exception e) {}
        }

        while (disp.getCurrent() == loadScreen) {
            loadScreen.repaint();
            loadScreen.serviceRepaints();

            try {
                // Sleep based on the frame number that was just drawn (first frame = 167 ms, last frame = 500 ms)
                switch (loadScreen.curFrame - loadScreen.animDirection) {
                    case 0: Thread.sleep(167); break;
                    case 7: Thread.sleep(500); break;
                    default: Thread.sleep(83); break;
                }
            }
            catch (Exception e) {}
        }
    }
}