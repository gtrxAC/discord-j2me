package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Canvas with vertical scrolling support (kinetic/smooth scrolling when swiping + optional scroll bar)
 * Scrollable content can be drawn using the 'scroll' field as a vertical offset
 * If using this in a different app: For kinetic scrolling, make sure to define 'scrollUnit' (as e.g. the font height)
 */
public abstract class KineticScrollingCanvas extends MyCanvas
//#ifdef TOUCH_SUPPORT
implements Runnable
//#endif
{
	static final int SCROLL_BAR_OFF = 0;
	static final int SCROLL_BAR_HIDDEN = 1;
	static final int SCROLL_BAR_VISIBLE = 2;
    public static int scrollBarMode;

    public int scroll;
    private int totalScroll;
    private int totalScrollAbs;

//#ifdef TOUCH_SUPPORT
    public static int scrollUnit;
    protected int velocity;
    private long lastPointerTime;
    private int lastPointerY;

    public boolean usingScrollBar;
    private int lastScrollBarY;
//#endif

    private static int scrollBarSize; 
    
    static {
//#ifdef TOUCH_SUPPORT
        scrollBarSize = Font.getDefaultFont().stringWidth("a")*5/2;
//#ifdef SAMSUNG_FULL
        if (Util.hasSamsungFontBug) scrollBarSize = scrollBarSize*5/2;
//#endif
//#else
        scrollBarSize = Font.getDefaultFont().stringWidth("a");
//#endif
    }

    protected abstract int getMinScroll();
    protected abstract int getMaxScroll();

    protected void checkScrollInRange() {
        scroll = Math.min(Math.max(scroll, getMinScroll()), getMaxScroll());
    }

    private boolean isScrollable() {
        return getMaxScroll() - getMinScroll() > 0;
    }

    public int getWidth() {
        if (prevShowScrollbar) {
            return super.getWidth() - scrollBarSize;
        }
        return super.getWidth();
    }

    protected boolean pointerWasTapped(int fontHeight) {
        return totalScrollAbs < fontHeight/2 && Math.abs(totalScroll) < fontHeight/4;
    }

//#ifdef TOUCH_SUPPORT
    private void handleScrollBar(int y) {
        lastScrollBarY = y;
        int height = getHeight() - scrollBarSize;
        y = Math.max(Math.min(y, getHeight() - scrollBarSize/2), scrollBarSize/2);
        y -= scrollBarSize/2;
        int ratio = y*1000/height;

        scroll = (getMaxScroll() - getMinScroll())*ratio;
        // if (scroll%1000 >= 500) scroll += 500;
        scroll = scroll/1000 + getMinScroll();
        repaint();
    }

    protected void _pointerPressed(int x, int y) {
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

    protected void _pointerDragged(int x, int y) {
        // Asha fix
//#ifdef MIDP2_GENERIC
        if (y > 65500) y = 0;
//#endif
        
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
    
    protected void _pointerReleased(int x, int y) {
        if (usingScrollBar) {
            usingScrollBar = false;
            repaint();
            return;
        }
        // Start kinetic scrolling thread if finger was not held in place for too long
        if (System.currentTimeMillis() <= lastPointerTime + 110 && Math.abs(velocity) > Util.fontSize*4) {
            new Thread(this).start();
        }
    }
    
    // Kinetic scrolling thread
    public void run() {
        int maxVel = Util.fontSize*30;

        while (Math.abs(velocity) > 1) {
            velocity = Math.min(Math.max(velocity, -maxVel), maxVel);
            scroll -= velocity/30;
            velocity = velocity*19/20;
            checkScrollInRange();
            repaint();

            Util.sleep(16);
        }
    }
//#endif

    private int getYFromScroll() {
        int height = getHeight() - scrollBarSize;
        int ratio = (scroll - getMinScroll())*1000/(getMaxScroll() - getMinScroll());
        int y = height*ratio;
        // if (y%1000 >= 500) y += 500;
        y /= 1000;
        return y;
    }

    private boolean prevShowScrollbar = false;

    protected void drawScrollbar(Graphics g) {
        int height = getHeight();
        g.setClip(0, 0, super.getWidth(), height);

        boolean showScrollbar = (scrollBarMode == SCROLL_BAR_VISIBLE && isScrollable());

        // If scrollbar is set to always show, and the bar's visibility changes
        // (because of the display content becoming taller/shorter than the screen height)
        // then the available screen width changes, so notify the sub-class by calling sizeChanged
        if (showScrollbar != prevShowScrollbar) {
            prevShowScrollbar = showScrollbar;
            sizeChanged(getWidth(), height);
            repaint();
        }
        // Draw scroll bar if it is set to always show, or if set to hidden and it's currently being dragged
        // Don't show scrollbar when pointer dragged to the very top/bottom, to avoid the scrollbar getting stuck visible (in hidden mode)
        else if (
            showScrollbar
//#ifdef TOUCH_SUPPORT
            || (usingScrollBar && lastScrollBarY > height/30 && lastScrollBarY < height*29/30)
//#endif
        ) {
            int x = super.getWidth() - scrollBarSize;
            int barPos = getYFromScroll();

            g.setColor(Theme.scrollbarColor);
            g.fillRect(x, 0, scrollBarSize, height);
            g.setColor(Theme.scrollbarHandleColor);
            g.fillRect(x, barPos, scrollBarSize, scrollBarSize);
        }
    }
}
