package com.gtrxac.discord;

import cc.nnproject.json.*;
import javax.microedition.rms.*;
import java.util.*;

public class FavoriteGuilds {
    private static JSONArray guilds;  // array of favorite guild IDs
    private static boolean hasChanged;  // list of fav guilds has changed (items added/removed)?
    public static final String label;  // "Favorite" or "Favourite"
    public static final String label2;  // "Favorites" or "Favourites"

    static {
        label = ("en-US".equals(System.getProperty("microedition.locale"))) ? "Favorite" : "Favourite";
        label2 = label + "s";

        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("favguild", false);
        }
        catch (Exception e) {}
        
        try {
            guilds = JSONObject.parseArray(new String(rms.getRecord(1)));
        }
        catch (Exception e) {
            guilds = new JSONArray();
        }

		try {
			rms.closeRecordStore();
		}
		catch (Exception e) {}
    }

    private static void save() {
        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("favguild", true);
            byte[] bytes = guilds.build().getBytes();
            
            if (rms.getNumRecords() >= 1) {
                rms.setRecord(1, bytes, 0, bytes.length);
            } else {
                rms.addRecord(bytes, 0, bytes.length);
            }
        }
        catch (Exception e) {
            App.error(e);
        }

		try {
			rms.closeRecordStore();
		}
		catch (Exception e) {}
    }

    public static void add(DiscordObject g) {
        if (has(g)) return;
        JSONArray gJson = new JSONArray();
        gJson.add(g.id);
        gJson.add(g.name);
        guilds.add(gJson);
        hasChanged = true;
        save();
    }

    public static void remove(int index) {
        guilds.remove(index);
        hasChanged = true;
        save();
    }

    public static boolean empty() {
        return guilds.size() == 0;
    }

    public static boolean has(DiscordObject g) {
        for (int i = 0; i < guilds.size(); i++) {
            if (guilds.getArray(i).getString(0).equals(g.id)) return true;
        }
        return false;
    }

    public static void openSelector() {
        if (App.guildSelector == null || !App.guildSelector.isFavGuilds || hasChanged) {
            Vector guildsVec = new Vector();

            for (int i = 0; i < guilds.size(); i++) {
                guildsVec.addElement(new DiscordObject(guilds.getArray(i)));
            }

            try {
                App.guildSelector = new GuildSelector(guildsVec, true);
            }
            catch (Exception e) {
                App.error(e);
                return;
            }
        }
        App.disp.setCurrent(App.guildSelector);

        hasChanged = false;
    }
}