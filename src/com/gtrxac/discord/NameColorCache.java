package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class NameColorCache {
    private static Hashtable colors;
    private static Vector keys;
    public static boolean activeRequest;

    public static void init() {
        colors = new Hashtable();
        keys = new Vector();
    }

    private static void fetch() {
        JSONObject reqData = new JSONObject();
        reqData.put("guild_id", App.selectedGuild.id);

        JSONArray requestIds = new JSONArray();

        // Populate requestIds with authors and recipients of all messages that
        // are currently loaded and that don't have name colors already fetched
        for (int i = 0; i < App.messages.size(); i++) {
            Message msg = (Message) App.messages.elementAt(i);
            String userId = msg.author.id;

            if (requestIds.indexOf(userId) == -1 && !has(msg.author)) {
                requestIds.add(userId);
            }

            if (msg.recipient == null) continue;
            userId = msg.recipient.id;

            if (requestIds.indexOf(userId) == -1 && !has(msg.recipient)) {
                requestIds.add(userId);
            }
        }

        reqData.put("user_ids", requestIds);

        JSONObject msg = new JSONObject();
        msg.put("op", 8);
        msg.put("d", reqData);
        App.gateway.send(msg);
    }

    public static boolean active() {
        return Settings.useNameColors &&
            // name colors are not applicable in non-guild contexts
            !App.isDM && App.selectedGuild != null &&
            // name colors cannot be fetched without gateway (technically can but isn't practical)
            App.gatewayActive();
    }

    public static int get(String userId) {
        if (!active()) return 0;

        String key = userId + App.selectedGuild.id;

        Integer result = (Integer) colors.get(key);
        if (result != null) return result.intValue();

        if (!activeRequest) {
            activeRequest = true;
            fetch();
        }
        return 0;
    }
    
    public static int get(User user) {
        return get(user.id);
    }

    public static void set(String key, int color) {
        Util.hashtablePutWithLimit(colors, keys, key, new Integer(color), 50);
        App.channelView.repaint();
    }

    public static boolean has(User user, boolean def) {
        if (!active()) return def;
        return colors.containsKey(user.id + App.selectedGuild.id);
    }

    public static boolean has(User user) {
        return has(user, false);
    }
}
