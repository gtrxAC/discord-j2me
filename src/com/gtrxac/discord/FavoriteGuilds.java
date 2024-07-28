package com.gtrxac.discord;

import cc.nnproject.json.*;
import javax.microedition.rms.*;
import java.util.*;

public class FavoriteGuilds {
    private static JSONArray guilds;  // array of favorite guild IDs
    private static boolean hasChanged;  // list of fav guilds has changed (items added/removed)?

    static {
        try {
            RecordStore rms = RecordStore.openRecordStore("favguild", false);
            guilds = JSON.getArray(new String(rms.getRecord(1)));
            rms.closeRecordStore();
        }
        catch (Exception e) {
            guilds = new JSONArray();
        }
    }

    private static void save(State s) {
        hasChanged = true;

        try {
            RecordStore rms = RecordStore.openRecordStore("favguild", true);
            byte[] bytes = guilds.build().getBytes();

            if (rms.getNumRecords() >= 1) {
                rms.setRecord(1, bytes, 0, bytes.length);
            } else {
                rms.addRecord(bytes, 0, bytes.length);
            }
            rms.closeRecordStore();
        }
        catch (Exception e) {
            s.error(e);
        }
    }

    public static void add(State s, Guild g) {
        if (has(g)) return;
        guilds.add(g.id);
        save(s);
    }

    public static void remove(State s, int index) {
        guilds.remove(index);
        save(s);
    }

    public static boolean empty() {
        return guilds.size() == 0;
    }

    public static boolean has(Guild g) {
        for (int i = 0; i < guilds.size(); i++) {
            if (guilds.getString(i).equals(g.id)) return true;
        }
        return false;
    }

    public static void openSelector(State s, boolean refresh) {
        // If guilds not loaded, load them. This method is called again by the thread when it's done.
        if (s.guilds == null || refresh) {
            HTTPThread h = new HTTPThread(s, HTTPThread.FETCH_GUILDS);
            h.showFavGuilds = true;
            h.start();
            return;
        }
        if (s.guildSelector == null || !s.guildSelector.isFavGuilds || hasChanged) {
            Vector guildsVec = new Vector();

            for (int i = 0; i < guilds.size(); i++) {
                Guild g = Guild.getById(s, guilds.getString(i));
                if (g != null) guildsVec.addElement(g);
            }

            try {
                s.guildSelector = new GuildSelector(s, guildsVec, true);
                s.guildSelector.isFavGuilds = true;
            }
            catch (Exception e) {
                s.error(e);
                return;
            }
        }
        s.disp.setCurrent(s.guildSelector);

        hasChanged = false;
    }
}