package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class NameColorCache {
    private State s;
    private Hashtable colors;
    private Vector keys;
    public boolean activeRequest;

    public NameColorCache(State s) {
        this.s = s;
        colors = new Hashtable();
        keys = new Vector();
    }

    private void fetch() {
        JSONObject reqData = new JSONObject();
        reqData.put("guild_id", s.selectedGuild.id);

        JSONArray requestIds = new JSONArray();

        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            String userId = msg.author.id;
            if (requestIds.indexOf(userId) == -1 && !has(msg.author)) {
                requestIds.add(userId);
            }
        }

        reqData.put("user_ids", requestIds);

        JSONObject msg = new JSONObject();
        msg.put("op", 8);
        msg.put("d", reqData);
        s.gateway.send(msg);
    }

    public boolean active() {
        return s.useNameColors &&
            // name colors are not applicable in non-guild contexts
            !s.isDM && s.selectedGuild != null &&
            // name colors cannot be fetched without gateway (technically can but isn't practical)
            s.gatewayActive();
    }

    public int get(String userId) {
        if (!active()) return 0;

        String key = userId + s.selectedGuild.id;

        Integer result = (Integer) colors.get(key);
        if (result != null) return result.intValue();

        if (!activeRequest) {
            activeRequest = true;
            fetch();
        }
        return 0;
    }
    
    public int get(User user) {
        return get(user.id);
    }

    public void set(String key, int color) {
        Util.hashtablePutWithLimit(colors, keys, key, new Integer(color), 50);
        s.channelView.repaint();
    }

    public boolean has(User user, boolean def) {
        if (!active()) return def;
        return colors.containsKey(user.id + s.selectedGuild.id);
    }

    public boolean has(User user) {
        return has(user, false);
    }
}
