package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    public static final int UNSCALED_CACHE_SIZE = 25;  // enough to hold icons of all DMs (20) and a bit more

    private static Hashtable icons;
    private static Vector iconHashes;
    private static Vector activeRequests;

    public static Hashtable unscaledIcons;  // managed by httpthread
    public static Vector unscaledIconHashes;  // managed by httpthread

    private static int cacheSize;
    private static int usageCounter;

    public static void init() {
        cacheSize = Settings.messageLoadCount*2;

        icons = new Hashtable(cacheSize);
        iconHashes = new Vector(cacheSize);
        activeRequests = new Vector();

        unscaledIcons = new Hashtable(UNSCALED_CACHE_SIZE);
        unscaledIconHashes = new Vector(UNSCALED_CACHE_SIZE);

        usageCounter = 0;
    }

    public static Image get(HasIcon target, int size) {
        String hash = target.getIconHash();
        if (hash == null) return null;

        String resizedHash = hash + size;

        Image result = (Image) icons.get(resizedHash);
        if (result != null) return result;

        if (!activeRequests.contains(resizedHash)) {
            activeRequests.addElement(resizedHash);
            HTTPThread http = new HTTPThread(HTTPThread.FETCH_ICON);
            http.iconTarget = target;
            http.iconSize = size;
            http.start();
        }
        return null;
    }

    public static void set(HasIcon target, int size, Image icon) {
        String hash = target.getIconHash() + size;
        removeRequest(hash);
        Util.hashtablePutWithLimit(icons, iconHashes, hash, icon, cacheSize);
    }

    public static boolean has(HasIcon target, int size) {
        String hash = target.getIconHash() + size;
        return icons.containsKey(hash);
    }

    public static synchronized void removeRequest(String hash) {
        int index = activeRequests.indexOf(hash);
        if (index != -1) activeRequests.removeElementAt(index);
    }
}
