package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    public static final int UNSCALED_CACHE_SIZE = 25;  // enough to hold icons of all DMs (20) and a bit more

    private static int cacheSize;
    private static Hashtable icons;
    public static Hashtable unscaledIcons;  // managed by httpthread
    private static Vector activeRequests;

    public static void init() {
        cacheSize = Settings.messageLoadCount*2;
        icons = new Hashtable(cacheSize);
        unscaledIcons = new Hashtable(UNSCALED_CACHE_SIZE);
        activeRequests = new Vector();
    }

    public static Image get(HasIcon target, int size) {
        String hash = target.getIconHash();
        if (hash == null) return null;

        String resizedHash = hash + size;

        Object result = icons.get(resizedHash);
        if (result != null) {
            return ((CachedImage) result).getImage();
        }

        if (!activeRequests.contains(resizedHash)) {
            activeRequests.addElement(resizedHash);
            HTTPThread http = new HTTPThread(HTTPThread.FETCH_ICON);
            http.iconTarget = target;
            http.iconSize = size;
            http.start();
        }
        return null;
    }

    public static void set(HasIcon target, int size, CachedImage icon) {
        String hash = target.getIconHash() + size;
        removeRequest(hash);
        Util.hashtablePutCachedImageWithLimit(icons, hash, icon, cacheSize);
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
