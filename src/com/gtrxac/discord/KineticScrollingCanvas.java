package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public abstract class KineticScrollingCanvas extends Canvas implements Runnable {
    public int scroll;
    public int totalScroll;

    public int scrollUnit;

    private int velocity;
    private boolean isScrolling;
    private long lastPointerTime;
    private int lastPointerY;

    protected abstract void checkScrollInRange();

    protected void pointerPressed(int x, int y) {
        lastPointerY = y;
        totalScroll = 0;

        isScrolling = true;
        velocity = 0;
        lastPointerTime = System.currentTimeMillis();
        repaint();
    }

    protected void pointerDragged(int x, int y) {
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
        isScrolling = false;
        // Start kinetic scrolling thread if finger was not held in place for too long
        if (System.currentTimeMillis() <= lastPointerTime + 110 && Math.abs(velocity) > scrollUnit*4) {
            new Thread(this).start();
        }
    }
    
    // Kinetic scrolling thread
    public void run() {
        int maxVel = scrollUnit*30;

        while (Math.abs(velocity) > 1) {
            if (velocity > maxVel) {
                velocity = maxVel;
            }
            else if (velocity < -maxVel) {
                velocity = -maxVel;
            }
            scroll -= velocity/30;
            velocity = velocity*19/20;
            checkScrollInRange();
            repaint();

            try {
                Thread.sleep(16);
            }
            catch (InterruptedException e) {}
        }
    }
}