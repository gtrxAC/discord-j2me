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

    public Message(State s, JSONObject data) {
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

        Date messageDate = new Date((Long.parseLong(id) >> 22) + State.DISCORD_EPOCH);
        String messageDay = messageDate.toString().substring(0, 10);
        String currentDay = new Date().toString().substring(0, 10);

        Calendar cal = Calendar.getInstance();
        cal.setTime(messageDate);
        StringBuffer time = new StringBuffer();

        if (currentDay.equals(messageDay)) {
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            if (s.use12hTime) {
                String period = hour < 12 ? "A" : "P";

                // Convert hours to 12-hour format
                hour = hour % 12;
                if (hour == 0) {
                    hour = 12; // 12 AM or 12 PM
                }

                time.append(hour);
                time.append(":");
                if (minute < 10) time.append("0");
                time.append(minute);
                time.append(period);
            } else {
                time.append(hour);
                time.append(":");
                if (minute < 10) time.append("0");
                time.append(minute);
            }
        } else {
            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (day < 10) time.append("0");
            time.append(day);
            time.append("/");
            int month = cal.get(Calendar.MONTH) + 1;
            if (month < 10) time.append("0");
            time.append(month);
        }
        timestamp = time.toString();

        if (content.length() == 0) content = "(no content)";
    }
}