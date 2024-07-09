package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    private State s;
    private Hashtable icons;
    private Vector iconHashes;
    private Vector activeRequests;

    public int scaleSize;

    public IconCache(State s) {
        this.s = s;
        icons = new Hashtable();
        iconHashes = new Vector();
        activeRequests = new Vector();
    }

    public Image get(HasIcon target) {
        if (s.iconSize == 0) return null;
        
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

    public void removeRequest(String hash) {
        int index = activeRequests.indexOf(hash);
        if (index != -1) activeRequests.removeElementAt(index);
    }

    public void set(String hash, Image icon) {
        removeRequest(hash);

        if (!icons.containsKey(hash) && icons.size() >= 100) {
            String firstHash = (String) iconHashes.elementAt(0);
            icons.remove(firstHash);
            iconHashes.removeElementAt(0);
        }
        icons.put(hash, icon);
        iconHashes.addElement(hash);
    }
}
