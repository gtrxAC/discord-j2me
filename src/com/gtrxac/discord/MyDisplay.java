package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;

public class MyDisplay {
    public static final int TRANSITION_FORWARD = 1;
    public static final int TRANSITION_BACKWARD = -1;

    public Display disp;
    public Stack history;
    public MyCanvas current;

    public MyDisplay(Display d) {
        disp = d;
        history = new Stack();
        new WrapperCanvas();
    }

    public int getColor(int colorSpecifier) {
        return disp.getColor(colorSpecifier);
    }

    public Object getActualCurrent() {
        Object result = disp.getCurrent();

        if (result instanceof WrapperCanvas) {
            return ((WrapperCanvas) result).current;
        }
        return result;
    }

    public Object getCurrent() {
//#ifdef TRANSITION_SCREEN
        Object result = getActualCurrent();

        if (result instanceof TransitionScreen) {
            return ((TransitionScreen) result).next;
        }
        return result;
//#else
        return getActualCurrent();
//#endif
    }

    public int numAlphaLevels() {
        return disp.numAlphaLevels();
    }

    private int updateHistory(Object d, Object curr) {
//#ifdef TRANSITION_SCREEN
        if (d instanceof TransitionScreen) return 0;
//#endif

        // System.out.println("---");
        // System.out.println("go to: " + d.toString());
        // System.out.println("---");
        // System.out.println("before:");
        // for (int i = 0;i <  history.size(); i++) System.out.println(history.elementAt(i));
        // System.out.println("---");
        // System.out.println("after;");

        if (history.size() > 0 && d.getClass().equals(history.peek().getClass())) {
            history.pop();
            return TRANSITION_BACKWARD;
        }
        else if (
            curr != null &&
//#ifdef TRANSITION_SCREEN
            !(curr instanceof TransitionScreen) &&
//#endif
            !(curr instanceof LoadingScreen)
        ) {
            history.push(curr);
        }
        return TRANSITION_FORWARD;
    }

    public synchronized void setCurrent(Displayable d) {
        updateHistory(d, getActualCurrent());
        // for (int i = 0;i <  history.size(); i++) System.out.println(history.elementAt(i));
        // System.out.println("---");
        disp.setCurrent(d);
    }

//#ifdef TRANSITION_SCREEN
    private boolean allowTransition(Object prev, MyCanvas next) {
        return !TransitionScreen.tempDisabled &&
            prev instanceof MyCanvas &&
            !(prev instanceof LoadingScreen) &&
            !(prev instanceof Dialog) &&
            !(next instanceof Dialog) &&
            !(prev instanceof ChannelView && next instanceof LoadingScreen);
    }
//#endif

    /**
     * set current screen and specify custom transition direction, 1 for forward, -1 for backward
     */
    public synchronized void setCurrent(MyCanvas d, int direction) {
        setCurrent(d, true, direction);
    }

    public synchronized void setCurrent(MyCanvas d, boolean transition) {
        setCurrent(d, transition, 0);
    }

    public synchronized void setCurrent(MyCanvas d, boolean transition, int customDirection) {
        Object curr = getActualCurrent();

        int direction = updateHistory(d, curr);
        // for (int i = 0;i <  history.size(); i++) System.out.println(history.elementAt(i));
        // System.out.println("---");

        if (customDirection != 0) direction = customDirection;

//#ifdef TRANSITION_SCREEN
        if (transition && allowTransition(curr, d)) {
            if (d instanceof MainMenu) {
                direction = TRANSITION_BACKWARD;
            }
            else if (d instanceof LoadingScreen) {
                direction = TRANSITION_FORWARD;
            }
            TransitionScreen ts = new TransitionScreen((MyCanvas) curr, d, direction);
            WrapperCanvas.instance.setCurrent(ts);
        } else {
            WrapperCanvas.instance.setCurrent(d);
            TransitionScreen.tempDisabled = false;
        }
//#else
        WrapperCanvas.instance.setCurrent(d);
//#endif

        disp.setCurrent(WrapperCanvas.instance);
    }

    public void setCurrent(MyCanvas d) {
        setCurrent(d, true);
    }

    public void setCurrent(Object d) {
        if (d instanceof MyCanvas) setCurrent((MyCanvas) d);
        else setCurrent((Displayable) d);
    }

    // private void actualSetCurrent(Displayable d) {
    //     if (d instanceof MyCanvas) {
    //         WrapperCanvas.instance.setCurrent(d);
    //         disp.setCurrent(WrapperCanvas.instance);
    //     }
    // }

    public void setCurrentItem(Item i) {
        disp.setCurrentItem(i);
    }

    public boolean vibrate(int duration) {
        return disp.vibrate(duration);
    }
}