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
    public String author;
    public String timestamp;
    public String content;
    public String[] contentLines;

    // is status message? (user joined/left/boosted) - affects rendering
    public boolean isStatus;

    // fields for non-status messages
    public String recipient;
    public boolean showAuthor;

    public boolean needUpdate;  // does this message's contentlines need to be updated before next draw

    public Message(State s, JSONArray data) {
        id = data.getString(0);
        author = data.getString(1);
        recipient = data.getString(3);
        if (recipient.length() == 0) recipient = null;

        int t = data.getInt(4);
        if (t >= TYPE_ADDED && t <= TYPE_BOOSTED_LEVEL_3) {
            isStatus = true;
        }

        // Status message -> determine content by message type
        if (isStatus) {
            switch (t) {
                case TYPE_ADDED: {
                    content = "added " + recipient + " to the group";
                    break;
                }
                case TYPE_REMOVED: {
                    if (author.equals(recipient)) {
                        content = "left the group";
                    } else {
                        content = "removed " + recipient + " from the group";
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
            content = data.getString(2);
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

        if (content.length() == 0 && !isStatus) {
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
        if (!above.author.equals(author)) return true;

        // This message is a reply -> true
        if (recipient != null) return true;

        // This message or above message is a status message -> true
        if (isStatus || above.isStatus) return true;

        // Finally, check if message was sent more than 7 minutes after the first message of the cluster
        long thisMsgTime = Long.parseLong(id) >> 22;
        long firstMsgTime = Long.parseLong(clusterStart) >> 22;
        return (thisMsgTime - firstMsgTime > 7*60*1000);
    }
}