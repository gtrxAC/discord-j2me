package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class LoadingScreen extends MyCanvas implements Runnable, Strings {
    private int phoneOffset;
    private int bubbleOffset;

    volatile String text;
//#ifdef EMOJI_SUPPORT
    volatile String text2;
//#endif
    int curFrame;
    int animDirection;

    static int scale;
    static Image[] phoneFrames;
    static Image[] bubbleFrames;

    public LoadingScreen() {
        super();
        setTitle("Discord");
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

        if (phoneFrames == null || scale != newScale) {
            scale = newScale;
            loadFrames();
        }
        // -24 (half of full frame width to center it on screen)
        // -3 (offset to make it look more centered since the phone is more towards the right)
        // 15 (phone offset to make it align with the bubble)
        phoneOffset = (-3 + 14 - 24)*scale;
        bubbleOffset = (-3 - 24)*scale;
    }

    private void loadFrames() {
        Image sheet;
        phoneFrames = new Image[4];
        bubbleFrames = new Image[4];

        try {
            sheet = Image.createImage("/l.png");
        }
        catch (Exception e) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            try {
                phoneFrames[i] = Image.createImage(sheet, i*32, 0, 32, 48, Sprite.TRANS_NONE);
                bubbleFrames[i] = Image.createImage(sheet, 128 + i*31, 0, 31, 24, Sprite.TRANS_NONE);
                if (scale > 1) {
                    phoneFrames[i] = Util.resizeImage(phoneFrames[i], 32*scale, 48*scale);
                    bubbleFrames[i] = Util.resizeImage(bubbleFrames[i], 31*scale, 24*scale);
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
        int messageFontHeight = App.messageFont.getHeight();
        int halfContainerHeight = 24*scale + messageFontHeight*3/4;
        int halfWidth = getWidth()/2;
        int halfHeight = getHeight()/2;

        int curPhoneFrame = Math.min(curFrame, 3);
        g.drawImage(
            phoneFrames[curPhoneFrame], halfWidth + phoneOffset, halfHeight - halfContainerHeight,
            Graphics.LEFT | Graphics.TOP
        );

        int curBubbleFrame = curFrame - 4;
        if (curBubbleFrame >= 0) {
            g.drawImage(
                bubbleFrames[curBubbleFrame], halfWidth + bubbleOffset, halfHeight - halfContainerHeight,
                Graphics.LEFT | Graphics.TOP
            );
        }

        g.setColor(Theme.loadingScreenTextColor);
        g.setFont(App.messageFont);
        g.drawString(
            text, halfWidth, halfHeight + halfContainerHeight,
            Graphics.HCENTER | Graphics.BOTTOM
        );
//#ifdef EMOJI_SUPPORT
        if (text2 != null) {
            g.drawString(
                text2, halfWidth, halfHeight + halfContainerHeight + messageFontHeight,
                Graphics.HCENTER | Graphics.BOTTOM
            );
        }
//#endif
    }
}