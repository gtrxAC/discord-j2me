package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Message {
    static final int TYPE_ADDED = 1;  // user added another user to group DM
    static final int TYPE_REMOVED = 2;  // user left (or was removed) from group DM
    static final int TYPE_JOINED = 7;  // user joined server
    static final int TYPE_BOOSTED = 8;  // user boosted server

    public String id;
    public User author;
    public String timestamp;
    public String content;
    public String[] contentLines;

    // is status message? (user joined/left/boosted) - affects rendering
    public boolean isStatus;

    // fields for non-status messages
    public String recipient;
    public Vector attachments;
    public boolean showAuthor;

    public Message(State s, JSONObject data) {
        id = data.getString("id");
        author = new User(s, data.getObject("author"));

        int t = data.getInt("type", 0);
        if (t == TYPE_ADDED || t == TYPE_REMOVED || t == TYPE_JOINED || t == TYPE_BOOSTED) {
            isStatus = true;
        }

        if (isStatus) {
            // Status message -> determine content by message type
            String target = "(unknown)";
            try {
                JSONObject targetData = data.getArray("mentions").getObject(0);
                target = new User(s, targetData).name;
            }
            catch (Exception e) {}

            switch (t) {
                case TYPE_ADDED: {
                    content = "added " + target + " to the group";
                    break;
                }
                case TYPE_REMOVED: {
                    if (author.name.equals(target)) {
                        content = "left the group";
                    } else {
                        content = "removed " + target + " from the group";
                    }
                    break;
                }
                case TYPE_JOINED: {
                    content = "joined the server";
                    break;
                }
                case TYPE_BOOSTED: {
                    content = "boosted the server";
                    break;
                }
            }
        } else {
            // Normal message -> get actual content
            // (and parse extra fields which don't apply to status messages)
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
                JSONArray attachArray = data.getArray("attachments");
                if (attachArray.size() >= 1) {
                    attachments = new Vector();

                    for (int i = 0; i < attachArray.size(); i++) {
                        JSONObject attach = attachArray.getObject(i);
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
        }

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

        if (content.length() == 0 && (attachments == null || attachments.size() == 0)) content = "(no content)";
    }

    /**
     * Determine whether or not the author/timestamp row should be shown for this message.
     * @param lastAuthor The author of the message shown above this message.
     * @param lastRecipient The recipient of the message shown above this message.
     * @return true if author should be shown, false if messages are "merged"
     */
    public boolean shouldShowAuthor(User lastAuthor, String lastRecipient) {
        // Status message -> true
        if (isStatus) return true;
        // First (topmost) message shown in channel view -> true
        if (lastAuthor == null) return true;
        // Different authors -> true
        if (!lastAuthor.id.equals(author.id)) return true;

        // Authors are same, now determine based on recipient:

        // Neither message is a reply -> false
        if (lastRecipient == null && recipient == null) return false;
        // One of the messages is a reply (but not both) -> true
        if ((lastRecipient == null) != (recipient == null)) return true;
        // Different recipients -> true
        return !lastRecipient.equals(recipient);
    }
}