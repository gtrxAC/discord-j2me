package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;

public class IconCache {
    private State s;
    private Hashtable icons;
    private Hashtable resizedIcons;
    private Vector iconHashes;
    private Vector resizedIconHashes;
    private Vector activeRequests;
    private Vector activeResizes;

    public IconCache(State s) {
        this.s = s;
        icons = new Hashtable();
        resizedIcons = new Hashtable();
        iconHashes = new Vector();
        resizedIconHashes = new Vector();
        activeRequests = new Vector();
        activeResizes = new Vector();
    }

    public Image get(HasIcon target) {
        if (s.iconType == State.ICON_TYPE_NONE || s.iconSize == 0) return null;

        // Don't show menu icons (DMChannel and Guild) if menu icons disabled
        if (!s.showMenuIcons && !(target instanceof User)) return null;
        
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

    public Image getResized(HasIcon target, int size) {
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

    public void setResized(String resizedHash, Image icon) {
        removeResize(resizedHash);
        
        if (!resizedIcons.containsKey(resizedHash) && resizedIcons.size() >= s.messageLoadCount) {
            String firstHash = (String) resizedIconHashes.elementAt(0);
            resizedIcons.remove(firstHash);
            resizedIconHashes.removeElementAt(0);
        }
        resizedIcons.put(resizedHash, icon);
        resizedIconHashes.addElement(resizedHash);
    }

    public boolean has(HasIcon target) {
        return icons.containsKey(target.getIconHash());
    }

    public boolean hasResized(HasIcon target, int size) {
        String resizedHash = target.getIconHash() + size;
        return resizedIcons.containsKey(resizedHash);
    }
}
