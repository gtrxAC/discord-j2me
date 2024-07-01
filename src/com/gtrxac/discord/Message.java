package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Message {
    static final int TYPE_ADDED = 1;  // user added another user to group DM
    static final int TYPE_REMOVED = 2;  // user left (or was removed) from group DM
    static final int TYPE_CALL = 3;
    static final int TYPE_CHANNEL_NAME_CHANGE = 4;  // changed name of group DM
    static final int TYPE_CHANNEL_ICON_CHANGE = 5;
    static final int TYPE_PINNED = 6;
    static final int TYPE_JOINED = 7;  // user joined server
    static final int TYPE_BOOSTED = 8;  // user boosted server
    static final int TYPE_BOOSTED_LEVEL_1 = 9; 
    static final int TYPE_BOOSTED_LEVEL_2 = 10;
    static final int TYPE_BOOSTED_LEVEL_3 = 11;

    public String id;
    public User author;
    public String timestamp;
    public String content;
    public String rawContent;
    public String[] contentLines;

    // is status message? (user joined/left/boosted) - affects rendering
    public boolean isStatus;

    // fields for non-status messages
    public String recipient;
    public String recipientID;  // only used for name colors (null if not needed)
    public Vector attachments;
    public Vector embeds;
    public boolean showAuthor;

    public Message(State s, JSONObject data) {
        id = data.getString("id");
        author = new User(s, data.getObject("author"));

        int t = data.getInt("type", 0);
        if (t >= TYPE_ADDED && t <= TYPE_BOOSTED_LEVEL_3) {
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
                case TYPE_CALL: {
                    content = "started a call";
                    break;
                }
                case TYPE_CHANNEL_NAME_CHANGE: {
                    content = "changed the group name";
                    break;
                }
                case TYPE_CHANNEL_ICON_CHANGE: {
                    content = "changed the group icon";
                    break;
                }
                case TYPE_PINNED: {
                    content = "pinned a message";
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
                case TYPE_BOOSTED_LEVEL_1:
                case TYPE_BOOSTED_LEVEL_2:
                case TYPE_BOOSTED_LEVEL_3: {
                    content = "boosted the server to level " + (t - TYPE_BOOSTED);
                    break;
                }
            }
        } else {
            // Normal message -> get actual content
            // (and parse extra fields which don't apply to status messages)
            content = data.getString("content", "");

            if (s.myUserId.equals(author.id)) rawContent = data.getString("_rc", content);

            try {
                JSONObject recipientObj = data
                    .getObject("referenced_message")
                    .getObject("author");

                if (s.gateway != null && s.gateway.isAlive() && s.useNameColors) {
                    recipientID = recipientObj.getString("id");
                }

                recipient = recipientObj.getString("global_name", null);
                if (recipient == null) {
                    recipient = recipientObj.getString("username", "(no name)");
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
                JSONArray embedArray = data.getArray("embeds");
                if (embedArray.size() >= 1) {
                    embeds = new Vector();

                    for (int i = 0; i < embedArray.size(); i++) {
                        Embed emb = new Embed(embedArray.getObject(i));
                        if (emb.title == null && emb.description == null) continue;
                        embeds.addElement(emb);
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

        if (
            content.length() == 0 &&
            (attachments == null || attachments.size() == 0) &&
            (embeds == null || embeds.size() == 0) &&
            !isStatus
        ) {
            isStatus = true;
            content = "(unsupported message)";
        }
    }

    /**
     * Determine whether or not the author/timestamp row should be shown for this message.
     * @param above The message shown above this message.
     * @param clusterStart The ID of the top-most message in this message cluster.
     * @return true if author should be shown, false if messages are "merged"
     */
    public boolean shouldShowAuthor(Message above, String clusterStart) {
        // Different authors -> true
        if (!above.author.id.equals(author.id)) return true;

        // This message or above message is a status message -> true
        if (isStatus || above.isStatus) return true;

        // Message was sent more than 7 minutes after the first message of the cluster -> true
        long thisMsgTime = Long.parseLong(id) >> 22;
        long firstMsgTime = Long.parseLong(clusterStart) >> 22;
        if (thisMsgTime - firstMsgTime > 7*60*1000) return true;

        // Neither message is a reply -> false
        if (above.recipient == null && recipient == null) return false;
        // One of the messages is a reply (but not both) -> true
        if ((above.recipient == null) != (recipient == null)) return true;
        // Different recipients -> true
        return !above.recipient.equals(recipient);
    }

    public void delete() {
        content = "(message deleted)";
        isStatus = true;
        embeds = null;
        attachments = null;
    }
}