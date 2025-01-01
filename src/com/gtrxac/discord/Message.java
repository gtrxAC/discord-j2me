package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Message implements Strings {
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
    // ifdef OVER_100KB
    public FormattedString contentFormatted;
    // endif
    // ifdef SAMSUNG_100KB
    public String[] contentLines;
    // endif

    // is status message? (user joined/left/boosted) - affects rendering
    public boolean isStatus;

    // fields for non-status messages
    public User recipient;
    public String refContent;
    public Vector attachments;
    public Vector embeds;
    public boolean showAuthor;

    public boolean needUpdate;  // does this message's contentlines need to be updated before next draw

    public Message(State s, JSONObject data) {
        id = data.getString("id");
        author = new User(s, data.getObject("author"));

        // JSON object to use for filling out message content and related fields.
        // For forwarded messages, this is an inner object inside the message object.
        JSONObject contentObj = data;
        boolean isForwarded = false;
        try {
            contentObj = data.getArray("message_snapshots").getObject(0).getObject("message");
            isForwarded = true;
        }
        catch (Exception e) {}

        int t = contentObj.getInt("type", 0);
        if (t >= TYPE_ADDED && t <= TYPE_BOOSTED_LEVEL_3) {
            isStatus = true;
        }

        if (isStatus) {
            // Status message -> determine content by message type
            String target = Locale.get(NAME_UNKNOWN);
            try {
                JSONObject targetData = contentObj.getArray("mentions").getObject(0);
                target = new User(s, targetData).name;
            }
            catch (Exception e) {}

            switch (t) {
                case TYPE_ADDED: {
                    content =
                        Locale.get(STATUS_ADDED_PREFIX) +
                        target +
                        Locale.get(STATUS_ADDED_SUFFIX);
                    break;
                }
                case TYPE_REMOVED: {
                    if (author.name.equals(target)) {
                        content = Locale.get(STATUS_LEFT);
                    } else {
                        content =
                            Locale.get(STATUS_REMOVED_PREFIX) +
                            target +
                            Locale.get(STATUS_REMOVED_SUFFIX);
                    }
                    break;
                }
                case TYPE_CALL: {
                    content = Locale.get(STATUS_CALL);
                    break;
                }
                case TYPE_CHANNEL_NAME_CHANGE: {
                    content = Locale.get(STATUS_CHANNEL_NAME_CHANGE);
                    break;
                }
                case TYPE_CHANNEL_ICON_CHANGE: {
                    content = Locale.get(STATUS_CHANNEL_ICON_CHANGE);
                    break;
                }
                case TYPE_PINNED: {
                    content = Locale.get(STATUS_PINNED);
                    break;
                }
                case TYPE_JOINED: {
                    content = Locale.get(STATUS_JOINED);
                    break;
                }
                case TYPE_BOOSTED: {
                    content = Locale.get(STATUS_BOOSTED);
                    break;
                }
                case TYPE_BOOSTED_LEVEL_1:
                case TYPE_BOOSTED_LEVEL_2:
                case TYPE_BOOSTED_LEVEL_3: {
                    content = Locale.get(STATUS_BOOSTED_LEVEL) + (t - TYPE_BOOSTED);
                    break;
                }
            }
        } else {
            // Normal message -> get actual content
            // (and parse extra fields which don't apply to status messages)
            content = contentObj.getString("content", "");

            // Get raw content (used for keeping formatting like pings intact when editing messages)
            if (s.myUserId != null && s.myUserId.equals(author.id)) {
                rawContent = contentObj.getString("_rc", content);
            }

            if (!isForwarded) {
                try {
                    JSONObject refObj = contentObj.getObject("referenced_message");
    
                    recipient = new User(s, refObj.getObject("author"));
    
                    if (s.showRefMessage) {
                        refContent = refObj.getString("content", null);
                        if (refContent == null) refContent = Locale.get(NO_CONTENT);
                    }
                }
                catch (Exception e) {}
            }

            try {
                JSONArray attachArray = contentObj.getArray("attachments");
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
                JSONArray stickers = contentObj.getArray("sticker_items");
                if (stickers.size() >= 1) {
                    if (content.length() > 0) content += "\n";
                    content +=
                        Locale.get(STICKER_PREFIX) +
                        stickers.getObject(0).getString("name", Locale.get(STICKER_UNKNOWN)) +
                        Locale.get(RIGHT_PAREN);
                }
            }
            catch (Exception e) {}

            try {
                JSONArray embedArray = contentObj.getArray("embeds");
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
                String period = hour < 12 ? Locale.get(TIMESTAMP_AM) : Locale.get(TIMESTAMP_PM);

                // Convert hours to 12-hour format
                hour = hour % 12;
                if (hour == 0) {
                    hour = 12; // 12 AM or 12 PM
                }

                time.append(hour);
                time.append(Locale.get(TIME_SEPARATOR));
                if (minute < 10) time.append("0");
                time.append(minute);
                time.append(period);
            } else {
                time.append(hour);
                time.append(Locale.get(TIME_SEPARATOR));
                if (minute < 10) time.append("0");
                time.append(minute);
            }
        } else {
            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (day < 10) time.append("0");
            time.append(day);
            time.append(Locale.get(DATE_SEPARATOR));
            int month = cal.get(Calendar.MONTH) + 1;
            if (month < 10) time.append("0");
            time.append(month);
        }
        timestamp = time.toString();

        if (isForwarded) {
            String tmpContent = content;
            content = Locale.get(FORWARDED_MESSAGE);
            if (tmpContent.length() > 0) content += "\n";
            content += tmpContent;
        }

        if (
            content.length() == 0 &&
            (attachments == null || attachments.size() == 0) &&
            (embeds == null || embeds.size() == 0) &&
            !isStatus
        ) {
            isStatus = true;
            content = Locale.get(UNSUPPORTED_MESSAGE);
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

        // This message is a reply -> true
        if (recipient != null) return true;

        // This message only consists of an attachment -> true
        if (attachments != null && attachments.size() > 0 && content.length() == 0) return true;

        // This message or above message is a status message -> true
        if (isStatus || above.isStatus) return true;

        // Finally, check if message was sent more than 7 minutes after the first message of the cluster
        long thisMsgTime = Long.parseLong(id) >> 22;
        long firstMsgTime = Long.parseLong(clusterStart) >> 22;
        return (thisMsgTime - firstMsgTime > 7*60*1000);
    }

    public void delete() {
        content = Locale.get(MESSAGE_DELETED);
        isStatus = true;
        embeds = null;
        attachments = null;
        needUpdate = true;
    }
}