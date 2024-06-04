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
    public Vector attachments;
    public boolean showAuthor;

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

        attachments = new Vector();
        try {
            JSONArray attachArray = data.getArray("attachments");
            if (attachArray.size() >= 1) {
                for (int i = 0; i < attachArray.size(); i++) {
                    JSONObject attach = attachArray.getObject(i);

                    // Skip attachments that aren't images
                    if (!attach.has("width")) continue;

                    attachments.addElement(new Attachment(s, attach));
                }
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

        try {
            JSONArray embeds = data.getArray("embeds");
            if (embeds.size() >= 1) {
                String title = embeds.getObject(0).getString("title");
                if (title != null) {
                    if (content.length() > 0) content += "\n";
                    content += "(embed: " + title + ")";
                }
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

        if (content.length() == 0 && attachments.size() == 0) content = "(no content)";
    }

    /**
     * Determine whether or not the author/timestamp row should be shown for this message.
     * @param lastAuthor The author of the message shown above this message.
     * @param lastRecipient The recipient of the message shown above this message.
     * @return true if author should be shown, false if messages are "merged"
     */
    public boolean shouldShowAuthor(String lastAuthor, String lastRecipient) {
        // First (topmost) message shown in channel view -> true
        if (lastAuthor == null) return true;
        // Different authors -> true
        if (!lastAuthor.equals(author)) return true;

        // Authors are same, now determine based on recipient:

        // Neither message is a reply -> false
        if (lastRecipient == null && recipient == null) return false;
        // One of the messages is a reply (but not both) -> true
        if ((lastRecipient == null) != (recipient == null)) return true;
        // Different recipients -> true
        return lastRecipient.equals(recipient);
    }
}