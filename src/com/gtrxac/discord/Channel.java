package com.gtrxac.discord;

import java.util.Vector;
import cc.nnproject.json.*;

/**
 * Data structure used for channels (guild channels and threads). For direct message channels, see DMChannel.
 */
public class Channel extends HasUnreads {
    public String name;
    public long lastMessageID;
    public Channel[] threads;
    public boolean isForum;
    public boolean isThread;

    public Channel(JSONObject data) {
        this(data, false);
    }

    public Channel(JSONObject data, boolean isThread) {
        id = data.getString("id");
        name = data.getString("name");
        this.isThread = isThread;
        
        int type = data.getInt("type", 0);
        isForum = (type == 15 || type == 16);

        try {
            lastMessageID = Long.parseLong(data.getString("last_message_id"));
        }
        catch (Exception e) {
            lastMessageID = 0L;
        }
    }
    
    public String toString() {
        if (isThread) return name;
        return "#" + name;
    }

    public static Channel getByID(String id) {
        if (App.guilds != null) {
            for (int g = 0; g < App.guilds.length; g++) {
                Guild guild = App.guilds[g];
                if (guild.channels == null) continue;

                for (int c = 0; c < guild.channels.length; c++) {
                    Channel ch = guild.channels[c];
                    if (id.equals(ch.id)) return ch;
                }
            }
        }
        return null;
    }
    
    public static Channel[] parseChannels(JSONArray arr) {
        Vector resultVec = new Vector();

        for (int i = 0; i < arr.size(); i++) {
            for (int a = 0; a < arr.size(); a++) {
                JSONObject ch = arr.getObject(a);
                if (ch.getInt("position", i) != i) continue;

                int type = ch.getInt("type", 0);
                if (type != 0 && type != 5 && type != 15 && type != 16) continue;

                resultVec.addElement(new Channel(ch));
            }
        }
        // Add text chats of voice channels at the end
        // (could add them at their correct locations but it's more complicated, we'd have to parse the structure of the channel categories)
        int vcCount = arr.size() - resultVec.size();

        for (int i = 0; i < vcCount; i++) {
            for (int a = 0; a < arr.size(); a++) {
                JSONObject ch = arr.getObject(a);
                if (ch.getInt("position", i) != i) continue;
                if (ch.getInt("type", 0) != 2) continue;

                resultVec.addElement(new Channel(ch));
            }
        }

        Channel[] result = new Channel[resultVec.size()];
        resultVec.copyInto(result);
        return result;
    }

    public boolean hasUnreads() {
        return UnreadManager.hasUnreads(id, lastMessageID);
    }

    public void markRead() {
        UnreadManager.markRead(id, lastMessageID);
    }
}
