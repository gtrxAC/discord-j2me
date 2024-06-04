package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ChannelViewItem {
    public static final int MESSAGE = 0;
    public static final int OLDER_BUTTON = 1;
    public static final int NEWER_BUTTON = 2;
    public static final int ATTACHMENTS_BUTTON = 3;

    State s;
    int type;  // one of the constants defined above
    Message msg;  // message data for MESSAGE and ATTACHMENTS_BUTTON types

    public ChannelViewItem(State s, int type) {
        this.s = s;
        this.type = type;
    }

    /**
     * Gets the amount of vertical pixels that this item will take up on the screen.
     */
    public int getHeight() {
        int messageFontHeight = s.messageFont.getHeight();

        if (type == MESSAGE) {
            // Each content line + one line for message author + little bit of spacing between messages
            int result = messageFontHeight*msg.contentLines.length + messageFontHeight/4;
            if (msg.showAuthor) result += s.authorFont.getHeight();
            return result;
        }
        // For buttons
        return messageFontHeight*5/3;
    }

    /**
     * Draws this channel view item on the screen.
     * @param g Graphics object to use for drawing.
     * @param y Vertical position (offset) to draw at, in pixels.
     * @param width Horizontal area available for drawing, in pixels.
     */
    public void draw(Graphics g, int y, int width, boolean selected) {
        int messageFontHeight = s.messageFont.getHeight();

        switch (type) {
            case MESSAGE: {
                // Highlight background if message is selected
                if (selected) {
                    g.setColor(ChannelView.highlightColors[s.theme]);
                    g.fillRect(0, y, width, getHeight());
                }
                
                y += messageFontHeight/8;

                if (msg.showAuthor) {
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
                }

                // Draw message content
                g.setColor(ChannelView.messageColors[s.theme]);
                g.setFont(s.messageFont);
                for (int i = 0; i < msg.contentLines.length; i++) {
                    g.drawString(msg.contentLines[i], 1, y, Graphics.TOP|Graphics.LEFT);
                    y += messageFontHeight;
                }
                break;
            }

            case OLDER_BUTTON:
            case NEWER_BUTTON: {
                g.setFont(s.messageFont);
                String caption;

                if (type == OLDER_BUTTON) {
                    caption = "Older";
                    if (s.messageFont.stringWidth("View older") <= width) caption = "View older";
                    if (s.messageFont.stringWidth("Older messages") <= width) caption = "Older messages";
                    if (s.messageFont.stringWidth("View older messages") <= width) caption = "View older messages";
                } else {
                    caption = "Newer";
                    if (s.messageFont.stringWidth("View newer") <= width) caption = "View newer";
                    if (s.messageFont.stringWidth("Newer messages") <= width) caption = "Newer messages";
                    if (s.messageFont.stringWidth("View newer messages") <= width) caption = "View newer messages";
                }

                if (selected) g.setColor(ChannelView.darkBgColors[s.theme]);
                else g.setColor(ChannelView.highlightColors2[s.theme]);

                int textWidth = s.messageFont.stringWidth(caption);
                g.fillRoundRect(
                    width/2 - textWidth/2 - messageFontHeight,
                    y + messageFontHeight/6,
                    textWidth + messageFontHeight*2,
                    messageFontHeight*4/3,
                    messageFontHeight/2,
                    messageFontHeight/2
                );

                g.setColor(ChannelView.authorColors[s.theme]);
                g.drawString(
                    caption, width/2, y + messageFontHeight/3,
                    Graphics.TOP | Graphics.HCENTER
                );
                break;
            }

            // Similar to older/newer button, but left aligned
            case ATTACHMENTS_BUTTON: {
                String caption = msg.attachments.size() + " attachment" + (msg.attachments.size() > 1 ? "s" : "");
                String longCaption = "View " + caption;
                if (s.messageFont.stringWidth(longCaption) <= width) caption = longCaption;

                g.setFont(s.messageFont);

                if (selected) g.setColor(ChannelView.darkBgColors[s.theme]);
                else g.setColor(ChannelView.highlightColors2[s.theme]);

                int textWidth = s.messageFont.stringWidth(caption);
                g.fillRoundRect(
                    messageFontHeight/2,
                    y + messageFontHeight/6,
                    textWidth + messageFontHeight,
                    messageFontHeight*4/3,
                    messageFontHeight/2,
                    messageFontHeight/2
                );

                g.setColor(ChannelView.authorColors[s.theme]);
                g.drawString(caption, messageFontHeight, y + messageFontHeight/3, Graphics.TOP|Graphics.LEFT);
                break;
            }
        }
    }
}
