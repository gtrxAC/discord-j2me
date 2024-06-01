package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ChannelViewItem {
    public static final int MESSAGE = 0;
    public static final int OLDER_BUTTON = 1;
    public static final int NEWER_BUTTON = 2;
    public static final int ATTACHMENTS_BUTTON = 3;

    State s;
    int type;  // one of the constants defined above
    Message msg;  // message data for MESSAGE type
    int attachCount;  // used in button caption in ATTACHMENTS_BUTTON type

    public ChannelViewItem(State s, int type) {
        this.s = s;
        this.type = type;
    }

    public ChannelViewItem(State s, Message message) {
        this.s = s;
        this.type = MESSAGE;
        this.msg = message;
    }

    /**
     * Gets the amount of vertical pixels that this item will take up on the screen.
     */
    public int getHeight() {
        int messageFontHeight = s.messageFont.getHeight();

        if (type == MESSAGE) {
            int authorFontHeight = s.authorFont.getHeight();

            // Each content line + one line for message author + little bit of spacing between messages
            return messageFontHeight*msg.contentLines.length + authorFontHeight + messageFontHeight/4;
        }
        // For buttons
        return 2*messageFontHeight;
    }

    /**
     * Draws this channel view item on the screen.
     * @param g Graphics object to use for drawing.
     * @param y Vertical position (offset) to draw at, in pixels.
     * @param width Horizontal area available for drawing, in pixels.
     */
    public void draw(Graphics g, int y, int width) {
        if (type == MESSAGE) {
            y += s.messageFont.getHeight()/8;

            // Draw author (and recipient if applicable)
            g.setColor(ChannelView.authorColors[s.theme]);
            g.setFont(s.authorFont);
            String authorStr = msg.author + (msg.recipient != null ? (" -> " + msg.recipient) : "");
            g.drawString(authorStr, 1, y, Graphics.TOP|Graphics.LEFT);

            // Draw timestamp
            g.setColor(ChannelView.timestampColors[s.theme]);
            g.setFont(s.timestampFont);
            g.drawString(
                "  " + msg.timestamp, 1 + s.authorFont.stringWidth(authorStr), y,
                Graphics.TOP|Graphics.LEFT
            );
            y += s.authorFont.getHeight();

            // Draw message content
            g.setColor(ChannelView.messageColors[s.theme]);
            g.setFont(s.messageFont);
            for (int i = 0; i < msg.contentLines.length; i++) {
                g.drawString(msg.contentLines[i], 1, y, Graphics.TOP|Graphics.LEFT);
                y += s.messageFont.getHeight();
            }
            return;
        }
        
        // Buttons
        g.setColor(ChannelView.authorColors[s.theme]);
        g.setFont(s.messageFont);
        String caption = "";

        switch (type) {
            case OLDER_BUTTON: {
                caption = "Older";
                if (s.messageFont.stringWidth("View older") <= width) caption = "View older";
                if (s.messageFont.stringWidth("Older messages") <= width) caption = "Older messages";
                if (s.messageFont.stringWidth("View older messages") <= width) caption = "View older messages";
                break;
            }
            case NEWER_BUTTON: {
                caption = "Newer";
                if (s.messageFont.stringWidth("View newer") <= width) caption = "View newer";
                if (s.messageFont.stringWidth("Newer messages") <= width) caption = "Newer messages";
                if (s.messageFont.stringWidth("View newer messages") <= width) caption = "View newer messages";
                break;
            }
            case ATTACHMENTS_BUTTON: {
                caption = "Attachments";
                String longCaption = attachCount + " attachments";
                if (s.messageFont.stringWidth(longCaption) <= width) caption = longCaption;
                break;
            }
        }

        g.drawString(
            caption, width/2, y + s.messageFont.getHeight()/2,
            Graphics.TOP | Graphics.HCENTER
        );
    }
}
