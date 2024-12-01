package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public abstract class MyCanvas extends Canvas {
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
}