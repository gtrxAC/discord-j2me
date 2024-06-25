package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    private State s;
    private Hashtable icons;
    private Hashtable largeIcons;
    private Vector iconHashes;
    private Vector largeIconHashes;
    private Vector activeRequests;
    private Vector activeResizes;

    public int scaleSize;

    public IconCache(State s) {
        this.s = s;
        icons = new Hashtable();
        largeIcons = new Hashtable();
        iconHashes = new Vector();
        largeIconHashes = new Vector();
        activeRequests = new Vector();
        activeResizes = new Vector();
    }

    public Image get(HasIcon target) {
        if (s.iconType == State.ICON_TYPE_NONE || s.iconSize == 0) return null;
        
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

    public void removeResize(String hash) {
        int index = activeResizes.indexOf(hash);
        if (index != -1) activeResizes.removeElementAt(index);
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

    public Image getLarge(HasIcon target) {
        String hash = target.getIconHash();
        if (hash == null) return null;
        
        Image result = (Image) largeIcons.get(hash);
        if (result != null && result.getWidth() == scaleSize) return result;

        Image smallIcon = (Image) get(target);

        if (smallIcon != null && !activeResizes.contains(hash)) {
            activeResizes.addElement(hash);
            new IconResizeThread(s, target, smallIcon, scaleSize).start();
        }
        return null;
    }

    public void setLarge(String hash, Image icon) {
        removeResize(hash);
        
        if (!largeIcons.containsKey(hash) && largeIcons.size() >= s.messageLoadCount) {
            String firstHash = (String) largeIconHashes.elementAt(0);
            largeIcons.remove(firstHash);
            largeIconHashes.removeElementAt(0);
        }
        largeIcons.put(hash, icon);
        largeIconHashes.addElement(hash);
    }
}
