package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public abstract class KineticScrollingCanvas extends Canvas implements Runnable {
    public int scroll;
    public int totalScroll;

    public int scrollUnit;

    private int velocity;
    private long lastPointerTime;
    private int lastPointerY;

    public boolean usingScrollBar;

    private static final int scrollBarSize = Font.getDefaultFont().stringWidth("a")*5/2;

    protected abstract int getMinScroll();
    protected abstract int getMaxScroll();

    protected void checkScrollInRange() {
        scroll = Math.min(Math.max(scroll, getMinScroll()), getMaxScroll());
    }

    private boolean isScrollable() {
        return getMaxScroll() - getMinScroll() > 0;
    }

    private void handleScrollBar(int y) {
        int ratio = y*1000/getHeight();
        scroll = getMinScroll() + (getMaxScroll() - getMinScroll())*ratio/1000;
        repaint();
    }

    public int getWidth() {
        if (Util.isKemulator && isScrollable()) return super.getWidth() - scrollBarSize;
        return super.getWidth();
    }

    protected void pointerPressed(int x, int y) {
        // Use scrollbar if the content is tall enough to be scrollable and the user pressed on the right edge of the screen
        usingScrollBar = isScrollable() && x > super.getWidth() - scrollBarSize;

        if (usingScrollBar) {
            velocity = 0;  // stop any kinetic scrolling
            totalScroll = 9999; // for the purposes of discord j2me, this just means "dont select the highlighted item after releasing finger"
            handleScrollBar(y);
            return;
        }
        lastPointerY = y;
        totalScroll = 0;

        velocity = 0;
        lastPointerTime = System.currentTimeMillis();
        repaint();
    }

    protected void pointerDragged(int x, int y) {
        if (usingScrollBar) {
            handleScrollBar(y);
            return;
        }
        int deltaY = y - lastPointerY;
        scroll -= deltaY;

        // Keep track of velocity
        long currentTime = System.currentTimeMillis();
        int timeDelta = (int) (currentTime - lastPointerTime);
        if (timeDelta > 0) {
            velocity = deltaY * 1000 / timeDelta;  // Pixels per second
        }
        lastPointerY = y;
        lastPointerTime = currentTime;
        totalScroll += Math.abs(deltaY);
        repaint();
    }
    
    protected void pointerReleased(int x, int y) {
        if (usingScrollBar) {
            usingScrollBar = false;
            repaint();
            return;
        }
        // Start kinetic scrolling thread if finger was not held in place for too long
        if (System.currentTimeMillis() <= lastPointerTime + 110 && Math.abs(velocity) > scrollUnit*4) {
            new Thread(this).start();
        }
    }
    
    // Kinetic scrolling thread
    public void run() {
        int maxVel = scrollUnit*30;

        while (Math.abs(velocity) > 1) {
            velocity = Math.min(Math.max(velocity, -maxVel), maxVel);
            scroll -= velocity/30;
            velocity = velocity*19/20;
            checkScrollInRange();
            repaint();

            Util.sleep(16);
        }
    }

    protected void drawScrollbar(Graphics g) {
        g.setClip(0, 0, super.getWidth(), getHeight());

        if ((Util.isKemulator && isScrollable()) || usingScrollBar) {
            int x = super.getWidth() - scrollBarSize;

            int ratio = scroll*1000/(getMaxScroll() - getMinScroll());
            int barPos = getHeight()*ratio/1000;

            barPos = Math.min(Math.max(barPos - scrollBarSize/2, 0), getHeight() - scrollBarSize);

            g.setColor(0xDDDDDD);
            g.fillRect(x, 0, scrollBarSize, getHeight());

            g.setColor(0x888888);
            g.fillRect(x, barPos, scrollBarSize, scrollBarSize);
        }
    }
}