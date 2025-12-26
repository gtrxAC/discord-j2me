package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;

public class MyDisplay {
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
        Object result = getActualCurrent();

        if (result instanceof TransitionScreen) {
            return ((TransitionScreen) result).next;
        }
        return result;
    }

    public int numAlphaLevels() {
        return disp.numAlphaLevels();
    }

    private int updateHistory(Object d, Object curr) {
        if (d instanceof TransitionScreen) return 0;
        // System.out.println("---");
        // System.out.println("go to: " + d.toString());
        // System.out.println("---");
        // System.out.println("before:");
        // for (int i = 0;i <  history.size(); i++) System.out.println(history.elementAt(i));
        // System.out.println("---");
        // System.out.println("after;");

        if (history.size() > 0 && d.getClass().equals(history.peek().getClass())) {
            history.pop();
            return -1;
        }
        else if (curr != null && !(curr instanceof TransitionScreen) && !(curr instanceof LoadingScreen)) {
            history.push(curr);
        }
        return 1;
    }

    public synchronized void setCurrent(Displayable d) {
        updateHistory(d, getActualCurrent());
        // for (int i = 0;i <  history.size(); i++) System.out.println(history.elementAt(i));
        // System.out.println("---");
        disp.setCurrent(d);
    }

    private boolean allowTransition(Object prev, MyCanvas next) {
        return !TransitionScreen.tempDisabled &&
            prev instanceof MyCanvas &&
            !(prev instanceof LoadingScreen) &&
            !(next instanceof Dialog) &&
            !(prev instanceof ChannelView && next instanceof LoadingScreen);
    }

    public synchronized void setCurrent(MyCanvas d, boolean transition) {
        Object curr = getActualCurrent();

        int direction = updateHistory(d, curr);
        // for (int i = 0;i <  history.size(); i++) System.out.println(history.elementAt(i));
        // System.out.println("---");

        if (transition && allowTransition(curr, d)) {
            if (d instanceof MainMenu) {
                direction = -1;
            }
            else if (d instanceof LoadingScreen) {
                direction = 1;
            }
            TransitionScreen ts = new TransitionScreen((MyCanvas) curr, d, direction);
            WrapperCanvas.instance.setCurrent(ts);
        } else {
            WrapperCanvas.instance.setCurrent(d);
            TransitionScreen.tempDisabled = false;
        }
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