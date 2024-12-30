package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    private static State s;
    private static Hashtable icons;
    private static Hashtable resizedIcons;
    private static Vector iconHashes;
    private static Vector resizedIconHashes;
    private static Vector activeRequests;
    private static Vector activeResizes;

    public static void init(State s) {
        IconCache.s = s;
        icons = new Hashtable();
        resizedIcons = new Hashtable();
        iconHashes = new Vector();
        resizedIconHashes = new Vector();
        activeRequests = new Vector();
        activeResizes = new Vector();
    }

    private static Image get(HasIcon target) {
        // Don't show icons if they are disabled
        if (target.isDisabled(s)) return null;
        
        String hash = target.getIconHash();
        if (hash == null) return null;

        Image result = (Image) icons.get(hash);
        if (result != null) return result;

        if (!activeRequests.contains(hash)) {
            activeRequests.addElement(hash);
            HTTPThread http = new HTTPThread(s, HTTPThread.FETCH_ICON);
            http.iconTarget = target;
            http.start();
        }
        return null;
    }

    public static synchronized void removeRequest(String hash) {
        int index = activeRequests.indexOf(hash);
        if (index != -1) activeRequests.removeElementAt(index);
    }

    public static synchronized void removeResize(String hash) {
        int index = activeResizes.indexOf(hash);
        if (index != -1) activeResizes.removeElementAt(index);
    }

    public static void set(String hash, Image icon) {
        removeRequest(hash);
        Util.hashtablePutWithLimit(icons, iconHashes, hash, icon, 100);
    }

    public static Image getResized(HasIcon target, int size) {
        String hash = target.getIconHash();
        if (hash == null) return null;

        String resizedHash = hash + size;
        
        Image result = (Image) resizedIcons.get(resizedHash);
        if (result != null) return result;

        Image origIcon = (Image) get(target);

        if (origIcon != null && !activeResizes.contains(resizedHash)) {
            activeResizes.addElement(resizedHash);
            new IconResizeThread(s, target, origIcon, size).start();
        }
        return null;
    }

    public static void setResized(String resizedHash, Image icon) {
        removeResize(resizedHash);
        Util.hashtablePutWithLimit(resizedIcons, resizedIconHashes, resizedHash, icon, s.messageLoadCount*2);
    }

    public static boolean has(HasIcon target) {
        return icons.containsKey(target.getIconHash());
    }

    public static boolean hasResized(HasIcon target, int size) {
        String resizedHash = target.getIconHash() + size;
        return resizedIcons.containsKey(resizedHash);
    }
}
