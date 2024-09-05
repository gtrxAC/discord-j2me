package com.gtrxac.discord;

import java.util.Vector;
import cc.nnproject.json.*;

public class Channel {
    public String id;
    public String name;
    public long lastMessageID;
    // public Vector messages;

    public Channel(JSONObject data) {
        id = data.getString("id");
        name = data.getString("name");

        try {
            lastMessageID = Long.parseLong(data.getString("last_message_id"));
        }
        catch (Exception e) {
            lastMessageID = 0L;
        }
    }
    
    public String toString(State s) {
        if (s.unreads.hasUnreads(this)) return "* #" + name;
        return "#" + name;
    }

    public static Channel getByID(State s, String id) {
        if (s.guilds != null) {
            for (int g = 0; g < s.guilds.size(); g++) {
                Guild guild = (Guild) s.guilds.elementAt(g);
                if (guild.channels == null) continue;

                for (int c = 0; c < guild.channels.size(); c++) {
                    Channel ch = (Channel) guild.channels.elementAt(c);
                    if (id.equals(ch.id)) return ch;
                }
            }
        }
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
