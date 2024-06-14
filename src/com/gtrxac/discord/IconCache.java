package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    private State s;
    private Hashtable icons;
    private Hashtable largeIcons;
    private Vector activeRequests;

    public int scaleSize;

    public IconCache(State s) {
        this.s = s;
        icons = new Hashtable();
        largeIcons = new Hashtable();
        activeRequests = new Vector();
    }

    public Image get(HasIcon target) {
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

    public void set(String hash, Image icon) {
        int reqIndex = activeRequests.indexOf(hash);
        if (reqIndex != -1) activeRequests.removeElementAt(reqIndex);

        if (!icons.containsKey(hash) && icons.size() >= 100) {
            icons.remove(icons.keys().nextElement());
        }
        icons.put(hash, icon);
    }

    public Image getLarge(HasIcon target) {
        String hash = target.getIconHash();
        if (hash == null) return null;
        
        Image result = (Image) largeIcons.get(hash);
        if (result != null && result.getWidth() == scaleSize) return result;

        Image smallIcon = (Image) get(target);
        if (smallIcon == null) return null;

        result = Util.resizeImage(smallIcon, scaleSize, scaleSize);
        setLarge(hash, result);
        return result;
    }

    public void setLarge(String hash, Image icon) {
        if (!largeIcons.containsKey(hash) && largeIcons.size() >= s.messageLoadCount) {
            largeIcons.remove(largeIcons.keys().nextElement());
        }
        largeIcons.put(hash, icon);
    }
}
