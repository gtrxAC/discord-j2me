package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class Message extends ChannelViewItem {
	public static final long DISCORD_EPOCH = 1420070400000L;

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

    public Message(JSONArray data) {
        super(null);
        id = data.getString(0);
        author = data.getString(1);
        recipient = data.getString(3);

        // bit of padding between author/recipient and message timestamp
        if (recipient.length() == 0) {
            recipient = null;
            author += " ";
        } else {
            recipient += " ";
        }

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

        Date messageDate = new Date((Long.parseLong(id) >> 22) + DISCORD_EPOCH);
        String messageDay = messageDate.toString().substring(0, 10);
        String currentDay = new Date().toString().substring(0, 10);

        Calendar cal = Calendar.getInstance();
        cal.setTime(messageDate);
        StringBuffer time = new StringBuffer();

        if (currentDay.equals(messageDay)) {
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            if (App.use12hTime) {
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

    /**
     * Gets the amount of vertical pixels that this item will take up on the screen.
     */
    public int calculateHeight() {
        int fontHeight = App.messageFont.getHeight();
        
        // Each content line + little bit of spacing between messages
        int result = fontHeight*contentLines.length + fontHeight/4;

        // One line for message author + more spacing
        if (showAuthor) result += App.authorFont.getHeight() + fontHeight/4;

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
        int fontHeight = App.messageFont.getHeight();

        // Highlight background if message is selected
        if (selected) {
            g.setColor(ChannelView.highlightColors[App.theme]);
            g.fillRect(0, y, width, height);
        }
        
        int x = fontHeight/5;
        y += fontHeight/8 + fontYOffset;

        if (showAuthor) {
            y += fontHeight/8;
            int authorX = x;

            if (selected) g.setColor(ChannelView.selMessageColors[App.theme]);
            else g.setColor(ChannelView.authorColors[App.theme]);

            // Draw author name
            g.setFont(App.authorFont);
            g.drawString(author, authorX, y, Graphics.TOP | Graphics.LEFT);
            authorX += App.authorFont.stringWidth(author);

            // Draw recipient name if applicable
            if (recipient != null) {
                authorX += arrowStringWidth;
                g.drawString(recipient, authorX, y, Graphics.TOP | Graphics.LEFT);
            }

            g.setFont(App.timestampFont);
            if (selected) {
                g.setColor(ChannelView.selTimestampColors[App.theme]);
            } else {
                g.setColor(ChannelView.timestampColors[App.theme]);
            }

            // Draw arrow between author and recipient if applicable
            if (recipient != null) {
                g.drawString(" > ", authorX, y, Graphics.TOP | Graphics.RIGHT);
                authorX += App.authorFont.stringWidth(recipient);
            }
            // Draw timestamp
            g.drawString(timestamp, authorX, y, Graphics.TOP | Graphics.LEFT);
            y += App.authorFont.getHeight() + fontHeight/8;
        }

        // Draw message content
        // Use timestamp color for status messages to distinguish them from normal messages
        if (isStatus) {
            if (selected) {
                g.setColor(ChannelView.selTimestampColors[App.theme]);
            } else {
                g.setColor(ChannelView.timestampColors[App.theme]);
            }
        } else {
            if (selected) {
                g.setColor(ChannelView.selMessageColors[App.theme]);
            } else {
                g.setColor(ChannelView.messageColors[App.theme]);
            }
        }

        g.setFont(App.messageFont);
        for (int i = 0; i < contentLines.length; i++) {
            g.drawString(contentLines[i], x, y, Graphics.TOP | Graphics.LEFT);
            y += fontHeight;
        }
    }
}