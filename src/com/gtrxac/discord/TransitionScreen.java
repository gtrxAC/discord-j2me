package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class TransitionScreen extends MyCanvas implements Runnable {
    private MyCanvas prev;
    MyCanvas next;
    private long startTime;
    private long time;
    private int mode;
    public static int touchOffset;
    Command backCommand;

    static boolean tempDisabled;

    private static final int LENGTH = 150;

    public TransitionScreen(MyCanvas prev, MyCanvas next, int mode) {
        this.prev = prev;
        this.next = next;
        this.mode = mode;

        if (mode != 0) {
            startTime = System.currentTimeMillis();
            new Thread(this).start();
        }

        setTitle(prev.title);
        WrapperCanvas.instance.checkUpdateSize(next);
    }

    public void paint(Graphics g) {
        int direction = mode;
        int offset;
        if (mode == 0) {
            offset = touchOffset;
            direction = -1;
        } else {
            offset = getWidth()*((int) time)/LENGTH;
        }

        if (prev instanceof Dialog && ((Dialog) prev).behindScreen == next) {
            next.paint(g);
            g.translate(offset*-direction, 0);
            ((Dialog) prev).paint(g, false);
        } else {
            g.translate(offset*-direction, 0);
            prev.paint(g);
            g.translate(getWidth()*direction, 0);
            g.setClip(0, 0, getWidth(), getHeight());
            next.paint(g);
        }
    }

    public void run() {
        do {
            repaint();
            serviceRepaints();
            time = System.currentTimeMillis() - startTime;
        }
        while (time < LENGTH);

        // one last repaint where the animation is over, to make the lag from switching screens less noticeable
        time = LENGTH;
        repaint();
        serviceRepaints();

        if (App.disp.getActualCurrent() == this) {
            App.disp.setCurrent(next, false);
        }
    }

    public void pointerReleased(int x, int y) {
        if (x > getWidth()/2) {
            TransitionScreen.tempDisabled = true;
            try {
                ((CommandListener) prev).commandAction(backCommand, null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            // if the screen didn't actually change, then go back to the last screen
            if (App.disp.getActualCurrent() == this) {
                App.disp.setCurrent(prev, false);
            }
        } else {
            App.disp.setCurrent(prev, false);
        }
    }

    public void hideNotify() {
        prev.hideNotify();
    }
}