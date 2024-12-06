package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Canvas with vertical scrolling support (kinetic/smooth scrolling when swiping + optional scroll bar)
 * Scrollable content can be drawn using the 'scroll' field as a vertical offset
 */
public abstract class KineticScrollingCanvas extends MyCanvas implements Runnable {
	static final int SCROLL_BAR_OFF = 0;
	static final int SCROLL_BAR_HIDDEN = 1;
	static final int SCROLL_BAR_VISIBLE = 2;
    public static int scrollBarMode;

    public int scroll;
    private int totalScroll;
    private int totalScrollAbs;

    public int scrollUnit;

    private int velocity;
    private long lastPointerTime;
    private int lastPointerY;

    public boolean usingScrollBar;

    private static int scrollBarSize; 
    
    static {
        scrollBarSize = Font.getDefaultFont().stringWidth("a")*5/2;
        // ifdef SAMSUNG
        if (Util.isSamsungJet) scrollBarSize = scrollBarSize*5/2;
        // endif
    }

    protected abstract int getMinScroll();
    protected abstract int getMaxScroll();

    protected void checkScrollInRange() {
        scroll = Math.min(Math.max(scroll, getMinScroll()), getMaxScroll());
    }

    private boolean isScrollable() {
        return getMaxScroll() - getMinScroll() > 0;
    }

    private void handleScrollBar(int y) {
        int height = getHeight() - scrollBarSize;
        y = Math.max(Math.min(y, getHeight() - scrollBarSize/2), scrollBarSize/2);
        lastPointerY = y;
        y -= scrollBarSize/2;
        int ratio = y*1000/height;

        scroll = getMinScroll() + (getMaxScroll() - getMinScroll())*ratio;
        if (scroll%1000 > 500) scroll += 500;
        scroll /= 1000;
        repaint();
    }

    public int getWidth() {
        if (scrollBarMode == SCROLL_BAR_VISIBLE && isScrollable()) {
            return super.getWidth() - scrollBarSize;
        }
        return super.getWidth();
    }

    protected boolean pointerWasTapped(int fontHeight) {
        return totalScrollAbs < fontHeight/2 && Math.abs(totalScroll) < fontHeight/4;
    }

    protected void pointerPressed(int x, int y) {
        // Use scrollbar if the content is tall enough to be scrollable and the user pressed on the right edge of the screen
        // Note: Scrollbar hitbox is wider than the actual rendered scrollbar
        usingScrollBar =
            scrollBarMode != SCROLL_BAR_OFF &&
            isScrollable() &&
            x > super.getWidth() - scrollBarSize*5/3;

        if (usingScrollBar) {
            velocity = 0;  // stop any kinetic scrolling
            totalScrollAbs = 65500;
            handleScrollBar(y);
            return;
        }
        lastPointerY = y;
        totalScroll = 0;
        totalScrollAbs = 0;

        velocity = 0;
        lastPointerTime = System.currentTimeMillis();
        repaint();
    }

    protected void pointerDragged(int x, int y) {
        // Asha fix
        // ifdef MIDP2_GENERIC
        if (y > 65500) y = 0;
        // endif
        
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
        totalScroll += deltaY;
        totalScrollAbs += Math.abs(deltaY);
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

        if ((scrollBarMode == SCROLL_BAR_VISIBLE && isScrollable()) || usingScrollBar) {
            int x = super.getWidth() - scrollBarSize;
            int barPos = lastPointerY - scrollBarSize/2;

            g.setColor(0xDDDDDD);
            g.fillRect(x, 0, scrollBarSize, getHeight());
            g.setColor(0x888888);
            g.fillRect(x, barPos, scrollBarSize, scrollBarSize);
        }
    }
}