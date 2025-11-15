//#ifdef SAMSUNG_100KB
package com.gtrxac.discord;

import java.io.*;
import cc.nnproject.json.*;
import java.util.*;
import javax.microedition.lcdui.*;

// Multiple thread types combined into one class to save space
public class Threads100kb extends Thread implements Strings {
    public static final int HEARTBEAT_THREAD = 0;
    public static final int STOP_TYPING_THREAD = 1;
    public static final int KEY_REPEAT_THREAD = 2;
    public static final int ICON_RESIZE_THREAD = 3;

    public int threadType;

    // ______________________________
    //
    // HeartbeatThread
    // ______________________________
    //
    int lastReceived;
    private int interval;
    volatile boolean stop;

    public Threads100kb(int interval) {
        this.interval = interval - 3000;  // Discord already more or less accounts for network latency but this is 2G we're talking about
        this.lastReceived = -1;
        threadType = HEARTBEAT_THREAD;
    }

    // ______________________________
    //
    // StopTypingThread
    // ______________________________
    //
    String userID;

    public Threads100kb(String userID) {
        this.userID = userID;
        threadType = STOP_TYPING_THREAD;
    }

    // ______________________________
    //
    // KeyRepeatThread
    // ______________________________
    //
    public static volatile int activeKey;
    public static volatile boolean enabled;
    public static Threads100kb instance;

    public Threads100kb() {
        threadType = KEY_REPEAT_THREAD;
    }

    public static void toggle(boolean on) {
        enabled = on;
        if (on && (instance == null || !instance.isAlive())) {
            instance = new Threads100kb();
            instance.setPriority(MIN_PRIORITY);
            instance.start();
        }
    }

    // ______________________________
    //
    // IconResizeThread
    // ______________________________
    //
    private static final int[] alphaAndValues = {0x00000000, 0x3FFFFFFF, 0x7FFFFFFF, 0xBFFFFFFF, 0xFFFFFFFF};

    private HasIcon target;
    private Image smallIcon;
    private int size;

    public Threads100kb(HasIcon target, Image smallIcon, int size) {
        this.target = target;
        this.smallIcon = smallIcon;
        this.size = size;
        threadType = ICON_RESIZE_THREAD;
    }

    public static int[] createCircleBuf(int size) {
        // Draw circle to a secondary buffer (bg = white, circle = black)
        Image circleImage = Image.createImage(size, size);
        Graphics cg = circleImage.getGraphics();
        cg.setColor(0);
        cg.fillArc(0, 0, size, size, 0, 360);

        // Get the bitmap data of the circle
        int[] circleData = new int[size*size];
        circleImage.getRGB(circleData, 0, size, 0, 0, size, size);

        return circleData;
    }

    public static int getCircleBufAlpha(int[] buf, int size, int x, int y) {
        int alpha = 0;
        if (buf[(y*4)*size + (x*2)] == 0xFF000000) alpha++;
        if (buf[(y*4)*size + (x*2) + 1] == 0xFF000000) alpha++;
        if (buf[(y*4 + 2)*size + (x*2)] == 0xFF000000) alpha++;
        if (buf[(y*4 + 2)*size + (x*2) + 1] == 0xFF000000) alpha++;
        return alphaAndValues[alpha];
    }

    public static Image circleCutout(Image img) {
        int size = img.getWidth();

        int[] imageData = new int[size*size];
        img.getRGB(imageData, 0, size, 0, 0, size, size);

        // Modify the image data to turn the pixels outside the circle transparent
        if (App.disp.numAlphaLevels() > 2 && Settings.pfpType == Settings.PFP_TYPE_CIRCLE_HQ) {
            // Alpha blending supported and enabled - use anti-aliasing (double resolution circle
            // buffer where 4 neighboring pixels are calculated together -> 5 alpha levels)
            int[] circleData = createCircleBuf(size*2);

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    imageData[y*size + x] &= getCircleBufAlpha(circleData, size, x, y);
                }
            }
        } else {
            // No alpha blending - do basic non-anti-aliased circle cutout
            int[] circleData = createCircleBuf(size);

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (circleData[y*size + x] == 0xFF000000) continue;
                    imageData[y*size + x] = 0;
                }
            }
        }
        // Create a new image from the modified image data
        return Image.createRGBImage(imageData, size, size, true);
    }

    // ______________________________
    //

    public void run() {
        switch (threadType) {
            case HEARTBEAT_THREAD: {
                try {
                    while (true) {
                        if (stop) break;
                        
                        JSONObject hbMsg = new JSONObject();
                        hbMsg.put("op", 1);
                        if (lastReceived >= 0) {
                            hbMsg.put("d", lastReceived);
                        } else {
                            hbMsg.put("d", JSON.json_null);
                        }
                        App.gateway.send(hbMsg);

                        Util.sleep(interval);
                    }
                }
                catch (Exception e) {
                    App.gateway.stopMessage = Locale.get(HEARTBEAT_THREAD_ERROR) + e.toString();
                    App.gateway.stop = true;
                }
                break;
            }

            case STOP_TYPING_THREAD: {
                Util.sleep(10000);

                for (int i = 0; i < App.typingUsers.size(); i++) {
                    if (App.typingUserIDs.elementAt(i).equals(userID)) {
                        App.typingUsers.removeElementAt(i);
                        App.typingUserIDs.removeElementAt(i);
                        App.channelView.repaint();
                        return;
                    }
                }
                break;
            }

            case KEY_REPEAT_THREAD: {
                while (enabled) {
                    try {
                        synchronized (this) {
                            wait(0);
                        }
                    }
                    catch (InterruptedException e) {}

                    while (enabled) {
                        long curr = System.currentTimeMillis();
                        if (curr >= MyCanvas.beginRepeatTime) break;
                        Util.sleep((int) (MyCanvas.beginRepeatTime - curr));
                    }

                    Displayable current = App.disp.getCurrent();
                    if (!(current instanceof MyCanvas)) continue;

                    while (enabled && activeKey != 0) {
                        try {
                            ((MyCanvas) current).keyAction(activeKey);
                            ((MyCanvas) current).repaint();
                            Thread.sleep(50);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }

            case ICON_RESIZE_THREAD: {
                try {
                    Image resized;
                    int width, height;

                    {
                        width = size;
                        height = size;
                    }

                    if (Settings.pfpType == Settings.PFP_TYPE_CIRCLE_HQ) {
                        resized = Util.resizeImageBilinear(smallIcon, width, height);
                    } else {
                        resized = Util.resizeImage(smallIcon, width, height);
                    }

                    Image result;
                    if (
                        (Settings.pfpType == Settings.PFP_TYPE_CIRCLE || Settings.pfpType == Settings.PFP_TYPE_CIRCLE_HQ)
                    ) {
                        result = circleCutout(resized);
                    } else {
                        result = resized;
                    }

                    IconCache.setResized(target.getIconHash() + size, result);
                    target.iconLoaded();
                }
                catch (Exception e) {
                    App.error(e);
                }
            }
        }
    }
}
//#endif