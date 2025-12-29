package com.gtrxac.discord;

import java.util.Vector;

import javax.microedition.lcdui.*;

public class WrapperCanvas extends Canvas {
    public static WrapperCanvas instance;
    MyCanvas current;
    private Vector commands;
    boolean needUpdateCommands;

    public WrapperCanvas() {
        super();
        instance = this;
        commands = new Vector();
    }

    protected void hideNotify() {
        current.hideNotify();
    }

    private static volatile boolean isKeyPressed = false;
    public static volatile long beginRepeatTime;

//#ifdef SYMBIAN
    private static long uiq3BackButtonTimer;
//#endif

    protected void keyPressed(int key) {
//#ifdef SYMBIAN
        // Symbian^3: Ignore home button presses which would otherwise deactivate touch mode
        if (key == -12) return;

        // UIQ3: Back button has duplicated key events, use a timer to suppress the latter event
        if (key == -11) {
            long curr = System.currentTimeMillis();
            if (curr < uiq3BackButtonTimer + 500) return;
            uiq3BackButtonTimer = curr;
        }
//#endif
        current.keyPressed(key);

        if (KeyRepeatThread.enabled && !isKeyPressed) {
            isKeyPressed = true;
            beginRepeatTime = System.currentTimeMillis() + 500;
            synchronized (KeyRepeatThread.instance) {
                KeyRepeatThread.activeKey = key;
                KeyRepeatThread.instance.notify();
            }
        }
    }

    protected void keyReleased(int key) {
        if (KeyRepeatThread.enabled && isKeyPressed) {
            isKeyPressed = false;
            KeyRepeatThread.activeKey = 0;
        }
        current.keyReleased(key);
    }

    protected void keyRepeated(int key) {
//#ifdef SYMBIAN
        if (key == -12) return;
//#endif
        if (!KeyRepeatThread.enabled) current.keyRepeated(key);
    }

    protected void paint(Graphics g) {
        if (needUpdateCommands) {
            updateCommands_();
            needUpdateCommands = false;
        }
        current.paint(g);
    }

//#ifdef TOUCH_SUPPORT
    private static boolean isDraggingBack;

    private boolean checkBackGesture(int x) {
        // find command with type "back" and lowest priority value
        // the command will be run when the gesture completes
        Command backCommand = null;

        for (int i = 0; i < commands.size(); i++) {
            Command c = (Command) commands.elementAt(i);
            if (c.getCommandType() != Command.BACK) continue;
            if (backCommand == null || c.getPriority() < backCommand.getPriority()) {
                backCommand = c;
            }
        }
        // don't allow gesture if the screen doesn't have a back command
        if (backCommand == null) return false;

        if (x < getWidth()/10 && App.disp.history.size() > 0 && App.disp.history.peek() instanceof MyCanvas) {
            isDraggingBack = true;
            TransitionScreen.touchOffset = x;
            TransitionScreen ts = new TransitionScreen(current, (MyCanvas) App.disp.history.peek(), 0);
            ts.backCommand = backCommand;
            App.disp.setCurrent(ts, false);
            return true;
        }
        return false;
    }
//#endif

    protected void pointerPressed(int x, int y) {
//#ifdef BLACKBERRY
        if (!checkBackGesture(x)) current.pointerPressed(x, y - current.bbTitleHeight);
//#else
//#ifdef TOUCH_SUPPORT
        if (!checkBackGesture(x)) current.pointerPressed(x, y);
//#endif
//#endif
    }

    protected void pointerDragged(int x, int y) {
//#ifdef BLACKBERRY
        if (isDraggingBack) {
            TransitionScreen.touchOffset = x;
            repaint();
        }
        else current.pointerDragged(x, y - current.bbTitleHeight);
//#else
//#ifdef TOUCH_SUPPORT
        if (isDraggingBack) {
            TransitionScreen.touchOffset = x;
            repaint();
        }
        else current.pointerDragged(x, y);
//#endif
//#endif
    }

    protected void pointerReleased(int x, int y) {
//#ifdef BLACKBERRY
        isDraggingBack = false;
        current.pointerReleased(x, y - current.bbTitleHeight);
//#else
//#ifdef TOUCH_SUPPORT
        isDraggingBack = false;
        current.pointerReleased(x, y);
//#endif
//#endif
    }

//#ifdef SAMSUNG_FULL
    private static boolean hasDoneSamsungFontFix;
//#endif

    protected void showNotify() {
//#ifdef SAMSUNG_FULL
        // On Samsung Jet S8000 (tested with S800MCEIK1 firmware) the first canvas that is shown
        // in a Java app will have fonts that are way too small (approx 16px on a 480p display).
        // The solution is to reload the fonts and the main menu.
        // More about this in Util.java
        if (Util.hasSamsungFontBug && !hasDoneSamsungFontFix) {
            App.loadFonts();
            App.disp.setCurrent(current.reload());
            hasDoneSamsungFontFix = true;
        } else
//#endif
        current.showNotify();
    }

    protected void sizeChanged(int width, int height) {
        updateSize(current, width, height);
    }

    public void updateSize(MyCanvas canvas, int width, int height) {
        canvas.sizeChanged(width, height);
        canvas.width = width;
        canvas.height = height;
        repaint();
    }

    public void checkUpdateSize(MyCanvas canvas) {
        if (canvas.width != getWidth() || canvas.height != getHeight()) {
            updateSize(canvas, getWidth(), getHeight());
        }
    }

    public void updateTitle() {
        if (current == null) return;
//#ifdef KEMULATOR
        if (!"Discord".equals(current.title) && current.title != null) {
            setTitle("Discord - " + current.title);
        } else
//#endif
        setTitle(current.title);
    }

    public void updateCommands_() {  // cant use updateCommands name because kemulator would bug out
        // delete old ones that don't belong to current screen
        if (current instanceof TransitionScreen) return;

        for (int i = 0; i < commands.size(); i++) {
            Command c = (Command) commands.elementAt(i);
            // if (!current.commands.contains(c)) {
            removeCommand(c);
            // }
        }
        commands = new Vector();
        // Add new ones
        for (int i = 0; i < current.commands.size(); i++) {
            Command c = (Command) current.commands.elementAt(i);
            // if (!commands.contains(c)) {
            commands.addElement(c);
            addCommand(c);
            // }
        }
    }

    public void updateFullscreen() {
        if (current != null) setFullScreenMode(current.isFullscreen);
    }

    public void updateCommandListener() {
        if (current != null) setCommandListener(current.commandListener);
    }

    public synchronized void setCurrent(MyCanvas d) {
        current = d;
        updateTitle();
        updateCommands_();
        updateFullscreen();
        updateCommandListener();
        checkUpdateSize(current);
        repaint();
    }
}