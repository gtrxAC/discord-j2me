package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class IconResizeThread extends Thread {
    private static final int[] alphaAndValues = {0x00000000, 0x3FFFFFFF, 0x7FFFFFFF, 0xBFFFFFFF, 0xFFFFFFFF};

    private State s;
    private HasIcon target;
    private Image smallIcon;
    private int size;

    public IconResizeThread(State s, HasIcon target, Image smallIcon, int size) {
        this.s = s;
        this.target = target;
        this.smallIcon = smallIcon;
        this.size = size;
    }

    private static int[] createCircleBuf(int size) {
        // Draw circle to a secondary buffer (bg = white, circle = black)
        Image circleImage = Image.createImage(size, size);
        Graphics cg = circleImage.getGraphics();
        cg.setColor(0);
        cg.fillArc(0, 0, size, size, 0, 360);

        // Get the bitmap data of the circle
        int[] circleData = new int[size*size];
        circleImage.getRGB(circleData, 0, size, 0, 0, size, size);

        return circleData;
    }

    public static Image circleCutout(State s, Image img) {
        int size = img.getWidth();

        int[] imageData = new int[size*size];
        img.getRGB(imageData, 0, size, 0, 0, size, size);

        // Modify the image data to turn the pixels outside the circle transparent
        if (s.disp.numAlphaLevels() > 2 && s.pfpType == State.PFP_TYPE_CIRCLE_HQ) {
            // Alpha blending supported and enabled - use anti-aliasing (double resolution circle
            // buffer where 4 neighboring pixels are calculated together -> 5 alpha levels)
            int[] circleData = createCircleBuf(size*2);

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int alpha = 0;
                    if (circleData[(y*4)*size + (x*2)] == 0xFF000000) alpha++;
                    if (circleData[(y*4)*size + (x*2) + 1] == 0xFF000000) alpha++;
                    if (circleData[(y*4 + 2)*size + (x*2)] == 0xFF000000) alpha++;
                    if (circleData[(y*4 + 2)*size + (x*2) + 1] == 0xFF000000) alpha++;

                    imageData[y*size + x] &= alphaAndValues[alpha];
                }
            }
        } else {
            // No alpha blending - do basic non-anti-aliased circle cutout
            int[] circleData = createCircleBuf(size);

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (circleData[y*size + x] == 0xFF000000) continue;
                    imageData[y*size + x] = 0;
                }
            }
        }
        // Create a new image from the modified image data
        return Image.createRGBImage(imageData, size, size, true);
    }

    public void run() {
        try {
            Image resized;
            if (s.pfpType == State.PFP_TYPE_CIRCLE_HQ) {
                resized = Util.resizeImageBilinear(smallIcon, size, size);
            } else {
                resized = Util.resizeImage(smallIcon, size, size);
            }

            Image result;
            if (s.pfpType == State.PFP_TYPE_CIRCLE || s.pfpType == State.PFP_TYPE_CIRCLE_HQ) {
                result = circleCutout(s, resized);
            } else {
                result = resized;
            }

            s.iconCache.setResized(target.getIconHash() + size, result);
            target.largeIconLoaded(s);
        }
        catch (Exception e) {
            s.error(e);
        }
    }
}