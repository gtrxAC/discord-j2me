package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class LoadingScreen extends MyCanvas implements Runnable, Strings {
    private int iconOffset;

    volatile String text;
//#ifdef EMOJI_SUPPORT
    volatile String text2;
//#endif
    int curFrame;
    int animDirection;

    static int scale;
    static Image[] frames;

    public LoadingScreen() {
        super();
        text = Locale.get(LOADING);
        curFrame = 0;
        animDirection = 1;

        checkLoadFrames();

        // Start loading icon animation
        new Thread(this).start();
    }

    private void checkLoadFrames() {
        int smallerDimension = Math.min(getWidth(), getHeight());
        int newScale = smallerDimension/270 + 1;

        if (frames == null || scale != newScale) {
            scale = newScale;
            loadFrames();
        }
        iconOffset = 4*scale;
    }

    private void loadFrames() {
        Image sheet;
        frames = new Image[8];

        try {
            sheet = Image.createImage("/loading.png");
        }
        catch (Exception e) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            try {
                frames[i] = Image.createImage(sheet, i*48, 0, 48, 48, Sprite.TRANS_NONE);
                if (scale > 1) {
                    frames[i] = Util.resizeImage(frames[i], 48*scale, 48*scale);
                }
            }
            catch (Exception e) {}
        }
    }

    // Icon animation thread
    public void run() {
        // Wait for the load screen to show up
        while (App.disp.getCurrent() != this) {
            Util.sleep(10);
        }

        while (App.disp.getCurrent() == this) {
            long paintTime = System.currentTimeMillis();
            repaint();
            serviceRepaints();
            paintTime = System.currentTimeMillis() - paintTime;

            // Sleep based on the frame number that was just drawn (first frame = 167 ms, last frame = 500 ms)
            int sleepTime;
            switch (curFrame) {
                case 0: sleepTime = 167; break;
                case 7: sleepTime = 500; break;
                default: sleepTime = 83; break;
            }
            Util.sleep(sleepTime - (int) paintTime);

            // Go to next animation frame.
            // If end reached, start going through frames in descending order.
            // If beginning reached, start going through frames in ascending order.
            curFrame += animDirection;
            if (curFrame == 7) animDirection = -1;
            if (curFrame == 0) animDirection = 1;
        }
    }

    public void sizeChanged(int w, int h) {
        checkLoadFrames();
    }

    public void paint(Graphics g) {
        clearScreen(g, Theme.loadingScreenBackgroundColor);

        // Draw current animation frame
        if (frames[curFrame] != null) {
            int messageFontHeight = App.messageFont.getHeight();
            int halfContainerHeight = 24*scale + messageFontHeight*3/4;

            g.drawImage(
                frames[curFrame], getWidth()/2 - iconOffset, getHeight()/2 - halfContainerHeight,
                Graphics.HCENTER | Graphics.TOP
            );

            g.setColor(Theme.loadingScreenTextColor);
            g.setFont(App.messageFont);
            g.drawString(
                text, getWidth()/2, getHeight()/2 + halfContainerHeight,
                Graphics.HCENTER | Graphics.BOTTOM
            );
//#ifdef EMOJI_SUPPORT
            if (text2 != null) {
                g.drawString(
                    text2, getWidth()/2, getHeight()/2 + halfContainerHeight + messageFontHeight,
                    Graphics.HCENTER | Graphics.BOTTOM
                );
            }
//#endif
        }
    }
}