package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    private static Hashtable icons;
    private static Vector iconHashes;
    private static Vector activeRequests;

    public static void init() {
        icons = new Hashtable();
        iconHashes = new Vector();
        activeRequests = new Vector();
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
        Util.hashtablePutWithLimit(icons, iconHashes, hash, icon, Settings.messageLoadCount*2);
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
