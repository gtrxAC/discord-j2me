package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class CachedImage {
    static int usageCounter;

    private Image image;
    int lastUsed;

    public CachedImage(Image i) {
        image = i;
    }

    public Image getImage() {
        usageCounter++;
        lastUsed = usageCounter;
        return image;
    }
}