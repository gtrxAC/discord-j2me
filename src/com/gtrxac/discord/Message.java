package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class Message extends ChannelViewItem {
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

    static int arrowStringWidth;
    static int margin;
    static int groupSpacing;
    static int screenMargin;
    static int timestampDistance;

    public String id;
    public String author;
    public String timestamp;
    public String content;
    public String[] contentLines;

    // message is sent by currently logged in user?
    public boolean isOwn;

    // is status message? (user joined/left/boosted) - affects rendering
    public boolean isStatus;

    // fields for non-status messages
    public String recipient;
    public boolean showAuthor;

    public boolean needUpdate;  // does this message's contentlines need to be updated before next draw

    private int timestampX;

    public Message(JSONArray data) {
        super();
        id = data.getString(0);
        author = data.getString(1);
        recipient = data.getString(3);

        labelOrAuthorWidth = screenMargin;

        if (recipient.length() == 0) {
            recipient = null;
        } else {
            labelOrAuthorWidth += arrowStringWidth;
            timestampX = App.authorFont.stringWidth(recipient);
        }
        labelOrAuthorWidth += App.authorFont.stringWidth(author);
        timestampX += labelOrAuthorWidth + timestampDistance;

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

            // Check if message is sent by us. If it is, enable flag to allow editing/deleting this message.
            isOwn = data.getString(5).equals(App.myUserId);
        }

        timestamp = App.formatTimestamp((Long.parseLong(id) >> 22));

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
        // This message is a reply -> true
        if (recipient != null) return true;

        // Different authors -> true
        if (!above.author.equals(author)) return true;

        // This message or above message is a status message -> true
        if (isStatus || above.isStatus) return true;

        // Finally, check if message was sent more than 7 minutes after the first message of the cluster
        long thisMsgTime = Long.parseLong(id) >> 22;
        long firstMsgTime = Long.parseLong(clusterStart) >> 22;
        return (thisMsgTime - firstMsgTime > 7*60*1000);
    }

    /**
     * Gets the amount of vertical pixels that this item will take up on the screen.
     * Result is stored in 'height' field for fast access.
     */
    public int calculateHeight() {
        // Each content line + little bit of spacing between messages
        int result = fontHeight*contentLines.length + margin*2;

        // One line for message author + more spacing (top margin to separate this message group from others)
        if (showAuthor) result += authorFontHeight + groupSpacing;

        height = result;
        return result;
    }

    /**
     * Draws this channel view item on the screen.
     * @param g Graphics object to use for drawing.
     * @param y Vertical position (offset) to draw at, in pixels.
     * @param width Horizontal area available for drawing, in pixels.
     */
    public void draw(Graphics g, int y, int width, boolean selected) {
        int boxHeight = height;
        if (showAuthor) {
            boxHeight -= groupSpacing;
            y += groupSpacing;
        }

        // Highlight background if message is selected
        if (selected) {
            g.setColor(highlightColor);
            g.fillRect(0, y, width, boxHeight);
        }
        y += margin + fontYOffset;

        if (showAuthor) {
            // Draw author name
            g.setColor(selected ? selMessageColor : authorColor);
            g.setFont(App.authorFont);
            g.drawString(author, screenMargin, y, Graphics.TOP | Graphics.LEFT);

            // Draw recipient name if applicable
            if (recipient != null) {
                g.drawString(recipient, labelOrAuthorWidth, y, Graphics.TOP | Graphics.LEFT);
            }

            g.setFont(App.timestampFont);
            g.setColor(selected ? selTimestampColor : timestampColor);

            // Draw arrow between author and recipient if applicable
            if (recipient != null) {
                g.drawString(" > ", labelOrAuthorWidth, y, Graphics.TOP | Graphics.RIGHT);
            }
            // Draw timestamp
            g.drawString(timestamp, timestampX, y, Graphics.TOP | Graphics.LEFT);
            y += authorFontHeight;

            g.setFont(App.messageFont);
        }

        // For normal messages, use message color
        // For status messages, the timestamp color (which was already set above) is used to distinguish them from normal messages
        if (!isStatus) {
            g.setColor(selected ? selMessageColor : messageColor);
        }

        // Draw message content
        for (int i = 0; i < contentLines.length; i++) {
            g.drawString(contentLines[i], screenMargin, y, Graphics.TOP | Graphics.LEFT);
            y += fontHeight;
        }
    }
}