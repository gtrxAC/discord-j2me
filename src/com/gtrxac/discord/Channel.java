package com.gtrxac.discord;

import java.util.Vector;
import cc.nnproject.json.*;

public class Channel {
    public String id;
    public String name;
    public boolean unread;
    public int pings;

    public Channel(JSONObject data) {
        id = data.getString("id");
        name = data.getString("name");
    }
    
    public String toString() {
        if (pings > 0) return "(" + pings + ") #" + name;
        if (unread) return "* #" + name;
        return "#" + name;
    }

    public static Channel getByID(State s, String id) {
        // Find guild channel
        for (int g = 0; g < s.guilds.size(); g++) {
            Guild guild = (Guild) s.guilds.elementAt(g);

            for (int c = 0; c < guild.channels.size(); c++) {
                Channel ch = (Channel) guild.channels.elementAt(c);
                if (id.equals(ch.id)) return ch;
            }
        }

        // Find DM channel
        // for (int c = 0; c < s.dmChannels.size(); c++) {
        //     DMChannel ch = (DMChannel) s.dmChannels.elementAt(c);
        //     if (id.equals(ch.id)) return ch;
        // }
        return null;
    }
    
    public static Vector parseChannels(JSONArray arr) {
        Vector result = new Vector();

        for (int i = 0; i < arr.size(); i++) {
            for (int a = 0; a < arr.size(); a++) {
                JSONObject ch = arr.getObject(a);
                if (ch.getInt("position", i) != i) continue;

                int type = ch.getInt("type", 0);
                if (type != 0 && type != 5) continue;

                result.addElement(new Channel(ch));
            }
        }
        return result;
    }
}
