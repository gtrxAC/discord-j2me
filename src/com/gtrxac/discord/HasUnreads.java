package com.gtrxac.discord;

import cc.nnproject.json.*;

public abstract class HasUnreads {
    public String id;

    public Object getMenuIndicator() {
        // ifdef OVER_100KB
        if (FavoriteGuilds.isMuted(id)) {
            return ListScreen.INDICATOR_MUTED;
        } else
        // endif
        if (hasUnreads()) {
            return ListScreen.INDICATOR_UNREAD;
        }
        return ListScreen.INDICATOR_NONE;
    }
    
    public abstract boolean hasUnreads();
    public abstract void markRead();
}