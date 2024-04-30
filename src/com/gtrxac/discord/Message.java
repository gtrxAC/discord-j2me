package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Message {
    public String id;
    public String author;
    public String recipient;
    public String content;

    public Message(JSONObject data) {
        id = data.getString("id");
        author = data.getObject("author").getString("global_name", "(no name)");
        content = data.getString("content", "(no content)");
        if (content.length() == 0) content = "(no content)";

        try {
            recipient = data
                .getObject("referenced_message")
                .getObject("author")
                .getString("global_name", "(no name)");
        }
        catch (Exception e) {}

        try {
            int attachCount = data.getArray("attachments").size();
            if (attachCount >= 1) content += "\n(attachments: " + attachCount + ")";
        }
        catch (Exception e) {}
    }

    public static void fetchMessages(State s) throws Exception {
        JSONArray messages = JSON.getArray(s.http.get("/channels/" + s.selectedChannel.id + "/messages?limit=20"));
        s.messages = new Vector();

        for (int i = 0; i < messages.size(); i++) {
            s.messages.addElement(new Message(messages.getObject(i)));
        }
    }

    public static void send(State s, String message) throws Exception {
        JSONObject json = new JSONObject();
        json.put("content", message);
        json.put("flags", 0);
        json.put("mobile_network_type", "unknown");
        json.put("tts", false);

        s.http.post("/channels/" + s.selectedChannel.id + "/messages", json.build());
    }
}
