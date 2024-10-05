package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class LoadingScreen extends Canvas implements Runnable, Strings {
    private State s;
    private int iconOffset;

    String text;
    int curFrame;
    int animDirection;

    static boolean upscaled;
    static Image[] frames;

    public LoadingScreen(State s) {
        super();
        this.s = s;
        text = Locale.get(LOADING);
        curFrame = 0;
        animDirection = 1;

        checkLoadFrames();

        // Start loading icon animation
        new Thread(this).start();
    }

    private void checkLoadFrames() {
        boolean shouldUpscale = getWidth() > 270 && getHeight() > 270;

        if (frames == null || upscaled != shouldUpscale) {
            upscaled = shouldUpscale;
            loadFrames();
        }
        iconOffset = upscaled ? 8 : 4;
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
                if (upscaled) {
                    frames[i] = Util.resizeImage(frames[i], 96, 96);
                }
            }
            catch (Exception e) {}
        }
    }

    // Icon animation thread
    public void run() {
        // Wait for the load screen to show up
        while (s.disp.getCurrent() != this) {
            try {
                Thread.sleep(10);
            }
            catch (Exception e) {}
        }

        while (s.disp.getCurrent() == this) {
            repaint();
            serviceRepaints();

            try {
                // Sleep based on the frame number that was just drawn (first frame = 167 ms, last frame = 500 ms)
                switch (curFrame - animDirection) {
                    case 0: Thread.sleep(167); break;
                    case 7: Thread.sleep(500); break;
                    default: Thread.sleep(83); break;
                }
            }
            catch (Exception e) {}
        }
    }

    protected void sizeChanged(int w, int h) {
        checkLoadFrames();
    }

    protected void paint(Graphics g) {
        // Fill background with selected theme's background color
        g.setColor(ChannelView.backgroundColors[s.theme]);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw current animation frame
        if (frames[curFrame] != null) {
            int messageFontHeight = s.messageFont.getHeight();
            int halfContainerHeight = (upscaled ? 48 : 24) + messageFontHeight*3/4;

            g.drawImage(
                frames[curFrame], getWidth()/2 - iconOffset, getHeight()/2 - halfContainerHeight,
                Graphics.HCENTER | Graphics.TOP
            );

            g.setColor(ChannelView.timestampColors[s.theme]);
            g.setFont(s.messageFont);
            g.drawString(
                text, getWidth()/2, getHeight()/2 + halfContainerHeight,
                Graphics.HCENTER | Graphics.BOTTOM
            );
        }

        // Go to next animation frame.
        // If end reached, start going through frames in descending order.
        // If beginning reached, start going through frames in ascending order.
        curFrame += animDirection;
        if (curFrame == 7) animDirection = -1;
        if (curFrame == 0) animDirection = 1;
    }
}