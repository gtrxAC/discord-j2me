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

    private Image get(HasIcon target) {
        // Don't show icons if they are disabled
        if (target instanceof User) {
            // For profile pictures
            if (s.pfpType == State.PFP_TYPE_NONE || s.pfpSize == State.PFP_SIZE_PLACEHOLDER) return null;
        } else {
            // For menu icons (DMChannel and Guild)
            if (!s.showMenuIcons || s.menuIconSize == 0) return null;
        }
        
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

    public synchronized void removeRequest(String hash) {
        int index = activeRequests.indexOf(hash);
        if (index != -1) activeRequests.removeElementAt(index);
    }

    public synchronized void removeResize(String hash) {
        int index = activeResizes.indexOf(hash);
        if (index != -1) activeResizes.removeElementAt(index);
    }

    public void set(String hash, Image icon) {
        removeRequest(hash);
        Util.hashtablePutWithLimit(icons, iconHashes, hash, icon, 100);
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
        Util.hashtablePutWithLimit(resizedIcons, resizedIconHashes, resizedHash, icon, s.messageLoadCount);
    }

    public boolean has(HasIcon target) {
        return icons.containsKey(target.getIconHash());
    }

    public boolean hasResized(HasIcon target, int size) {
        String resizedHash = target.getIconHash() + size;
        return resizedIcons.containsKey(resizedHash);
    }
}
