package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoadingScreen extends Canvas {
    private State s;
    private boolean upscaled;
    private int iconOffset;

    String text;
    int curFrame;
    int animDirection;

    static Image[] frames;

    public LoadingScreen(State s) {
        super();
        this.s = s;
        text = "Loading";
        curFrame = 0;
        animDirection = 1;

        upscaled = getWidth() > 270 && getHeight() > 270;
        iconOffset = upscaled ? 10 : 5;

        if (frames == null) {
            frames = new Image[8];
            for (int i = 0; i < 8; i++) {
                try {
                    frames[i] = Image.createImage("/" + (i + 1) + ".png");
                    if (upscaled) {
                        frames[i] = Util.resizeImage(frames[i], 96, 96);
                    }
                }
                catch (Exception e) {}
            }
        }

        new LoadingAnimThread(s.disp, this).start();
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