package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Message {
    public String id;
    public String author;
    public String recipient;
    public String content;
    public String[] contentLines;
    public String timestamp;

    public Message(JSONObject data) {
        id = data.getString("id");
        author = data.getObject("author").getString("global_name", "(no name)");
        if (author == null) {
            author = data.getObject("author").getString("username", "(no name)");
        }
        content = data.getString("content", "");

        try {
            recipient = data
                .getObject("referenced_message")
                .getObject("author")
                .getString("global_name", "(no name)");

            if (recipient == null) {
                recipient = data
                    .getObject("referenced_message")
                    .getObject("author")
                    .getString("username", "(no name)");
            }
        }
        catch (Exception e) {}

        try {
            int attachCount = data.getArray("attachments").size();
            if (attachCount >= 1) {
                if (content.length() > 0) content += "\n";
                content += "(attachments: " + attachCount + ")";
            }
        }
        catch (Exception e) {}

        try {
            JSONArray stickers = data.getArray("sticker_items");
            if (stickers.size() >= 1) {
                if (content.length() > 0) content += "\n";
                content += "(sticker: " + stickers.getObject(0).getString("name", "unknown") + ")";
            }
        }
        catch (Exception e) {}

        timestamp = new Date((Long.parseLong(id) >> 22) + State.DISCORD_EPOCH)
            .toString().substring(11, 16);

        if (content.length() == 0) content = "(no content)";
    }

    public static void fetchMessages(State s, String before, String after) throws Exception {
        String id;
        if (s.isDM) id = s.selectedDmChannel.id;
        else id = s.selectedChannel.id;
        
        StringBuffer url = new StringBuffer("/channels/" + id + "/messages?limit=20");
        if (before != null) url.append("&before=" + before);
        if (after != null) url.append("&after=" + after);

        JSONArray messages = JSON.getArray(s.http.get(url.toString()));
        s.messages = new Vector();

        for (int i = 0; i < messages.size(); i++) {
            s.messages.addElement(new Message(messages.getObject(i)));
        }
    }

    public static void send(State s, String message) throws Exception {
        String id;
        if (s.isDM) id = s.selectedDmChannel.id;
        else id = s.selectedChannel.id;

        JSONObject json = new JSONObject();
        json.put("content", message);
        json.put("flags", 0);
        json.put("mobile_network_type", "unknown");
        json.put("tts", false);

        s.http.post("/channels/" + id + "/messages", json.build());
    }
}
