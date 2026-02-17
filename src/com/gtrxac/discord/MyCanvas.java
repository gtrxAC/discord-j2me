package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.util.*;

public abstract class MyCanvas {
    public static final int UP = 1;
    public static final int DOWN = 6;
    public static final int LEFT = 2;
    public static final int RIGHT = 5;
    public static final int FIRE = 8;
    public static final int GAME_A = 9;
    public static final int GAME_B = 10;
    public static final int GAME_C = 11;
    public static final int GAME_D = 12;

    public static Image backgroundImageOrig;  // set to null to force reload
    private static Image backgroundImage;
    private static int backgroundImageWidth;
    private static int backgroundImageHeight;

    String title;
    boolean isFullscreen;
    Vector commands;
    int width;
    int height;
    CommandListener commandListener;

    public MyCanvas(String title) {
        this.title = title;
        commands = new Vector();

//#ifdef KEMULATOR
        this.isFullscreen = true;
//#else
        this.isFullscreen = false;
//#endif

        width = WrapperCanvas.instance.getWidth();
        height = WrapperCanvas.instance.getHeight();
    }

    public MyCanvas() {
        this(null);
    }

    int getGameAction(int keyCode) {
        return WrapperCanvas.instance.getGameAction(keyCode);
    }

    // int getKeyCode(int gameAction)  not used

    // String getKeyName(int keyCode)  not used

    public boolean hasPointerEvents() {
//#ifdef SAMSUNG_FULL
        // Samsung S7350i does not have a touchscreen but hasPointerEvents on it still returns true. Good job, Samsung!
        return WrapperCanvas.instance.hasPointerEvents() && Util.noPointerEventsBug;
//#else
        return WrapperCanvas.instance.hasPointerEvents();
//#endif
    }

    // public boolean hasPointerMotionEvents()  not used

    // public boolean hasRepeatEvents()  not used

    public void hideNotify() { }

    // public boolean isDoubleBuffered()  not used

    public void keyPressed(int key) {
        keyAction(key);
    }

    public void keyReleased(int key) { }

    public void keyRepeated(int key) {
        keyAction(key);
    }

    public void pointerDragged(int x, int y) { }

    public void pointerPressed(int x, int y) { }

    public void pointerReleased(int x, int y) { }

    public void showNotify() { }

    public void sizeChanged(int width, int height) { }

    public void keyAction(int key) { }  // nonstandard

    // nonstandard, called when dragging back
    // argument indicates if transition should be shown, when called by transitionscreen, it's false
    public void backAction(boolean transition) { }

    public void paint(Graphics g) { }

//#ifdef SAMSUNG_FULL
    // see WrapperCanvas
    // overridden when needed
    protected MyCanvas reload() {
        return null;
    }
//#endif

    public void setTitle(String title) {
        this.title = title;
        WrapperCanvas.instance.updateTitle();
    }

    public void setFullScreenMode(boolean mode) {
        isFullscreen = mode;
        WrapperCanvas.instance.updateFullscreen();
    }

    protected void clearScreen(Graphics g, int color) {
        // On BlackBerry, the clip is set by default to (0, -y, width, height+y), where y is the height of the title bar. This means that apps can draw stuff over the title bar.
        // We'll draw a custom title bar over the default one, then set a new clip so nothing else in the app can draw over it.
//#ifdef BLACKBERRY
        bbDrawTitle(g);
//#endif
        int screenWidth = WrapperCanvas.instance.getWidth();  // getting width this way so it ignores area taken by scrollbar
        int screenHeight = getHeight();

        if (Settings.wallpaperMode != Settings.WALLPAPER_OFF) {
            if (backgroundImageOrig == null) {
                backgroundImage = null;
                backgroundImageWidth = 0;
                backgroundImageHeight = 0;

                RecordStore rms = null;

                try {
                    rms = RecordStore.openRecordStore("bgimage", false);
                    byte[] imageRecord = rms.getRecord(2);
                    backgroundImageOrig = Image.createImage(imageRecord, 0, imageRecord.length);

                    int largerDimension = Math.max(screenWidth, screenHeight);
                    backgroundImageOrig = Util.resizeImageBilinear(backgroundImageOrig, largerDimension, largerDimension);
                }
                catch (Exception e) {
                    Settings.wallpaperMode = Settings.WALLPAPER_OFF;
                    return;
                }
                
                Util.closeRecordStore(rms);
            }

            if (backgroundImageWidth != screenWidth || backgroundImageHeight != screenHeight) {
                if (Settings.wallpaperMode == Settings.WALLPAPER_CROP) {
                    int origWidth = backgroundImageOrig.getWidth();
                    int origHeight = backgroundImageOrig.getHeight();
                    int origRatio = origWidth*1000/origHeight;

                    int scaledWidth, scaledHeight;
                    
                    if (screenWidth > screenHeight) {
                        scaledWidth = screenWidth;
                        scaledHeight = (screenWidth*1000)/origRatio;
                    } else {
                        scaledHeight = screenHeight;
                        scaledWidth = (screenHeight*1000)/origRatio;
                    }

                    Image backgroundImageScaled = Util.resizeImageBilinear(backgroundImageOrig, scaledWidth, scaledHeight);

                    backgroundImage = Image.createImage(screenWidth, screenHeight);
                    Graphics bgG = backgroundImage.getGraphics();
                    bgG.drawImage(backgroundImageScaled, screenWidth/2, screenHeight/2, Graphics.HCENTER | Graphics.VCENTER);
                    backgroundImage = Image.createImage(backgroundImage);
                } else {
                    // stretch
                    backgroundImage = Util.resizeImageBilinear(backgroundImageOrig, screenWidth, screenHeight);
                }
                backgroundImageWidth = screenWidth;
                backgroundImageHeight = screenHeight;
            }

            if (!Settings.useTransition || !TransitionScreen.hasClearedScreen) {
                g.drawImage(backgroundImage, -g.getTranslateX(), 0, Graphics.TOP | Graphics.LEFT);
            }
        } else
//#ifdef NOKIA_THEME_BACKGROUND
        if (Settings.theme != Theme.SYSTEM || isFullscreen)
//#endif
        {
            g.setColor(color);
            g.fillRect(0, 0, screenWidth, screenHeight);

//#ifdef NOKIA_THEME_BACKGROUND
            // Fix white border rendering bug on Symbian 9.3 - 9.4
            if (!isFullscreen) {
                g.drawRect(0, 0, screenWidth - 1, screenHeight - 1);
            }
//#endif
        }
    }

//#ifdef BLACKBERRY
    public int bbTitleHeight;
    private static final Font bbTitleFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    protected void bbDrawTitle(Graphics g) {
        bbTitleHeight = g.getTranslateY();

        if (bbTitleHeight != 0) {
            String title = getTitle();
            if (title == null) title = "Discord";

            g.setColor(0xEEEEFF);
            g.fillRect(0, -bbTitleHeight, getWidth(), bbTitleHeight);

            g.setColor(0x111111);
            g.setFont(bbTitleFont);
            g.drawString(title, getWidth()/2, -bbTitleHeight, Graphics.HCENTER | Graphics.TOP);
            g.drawLine(0, -1, getWidth(), -1);

            g.setClip(0, 0, getWidth(), getHeight());
        }
    }
//#endif

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void repaint() {
        WrapperCanvas.instance.repaint();
    }

    public void serviceRepaints() {
        WrapperCanvas.instance.serviceRepaints();
    }

    public void setCommandListener(CommandListener l) {
        commandListener = l;
        WrapperCanvas.instance.updateCommandListener();
    }

    public void addCommand(Command c) {
        if (!commands.contains(c)) {
            commands.addElement(c);
            WrapperCanvas.instance.needUpdateCommands = true;
            repaint();
        }
    }

    public void removeCommand(Command c) {
        if (commands.removeElement(c)) {
            WrapperCanvas.instance.needUpdateCommands = true;
            repaint();
        }
    }

    public String getTitle() {
        return title;
    }
}