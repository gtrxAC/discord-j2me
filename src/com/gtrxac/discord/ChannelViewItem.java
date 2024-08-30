package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ChannelViewItem {
    static final int MESSAGE = 0;
    static final int OLDER_BUTTON = 1;
    static final int NEWER_BUTTON = 2;

    State s;
    int type;  // one of the constants defined above
    Message msg;  // message data for MESSAGE type

    public ChannelViewItem(State s, int type) {
        this.s = s;
        this.type = type;
    }

    /**
     * Determine whether or not the 'reply' menu option should be displayed when this item is selected.
     */
    public boolean shouldShowReplyOption() {
        // Don't show reply option for status messages
        if (msg != null && msg.isStatus) return false;

        // Show reply option for message item
        return (type == ChannelViewItem.MESSAGE);
    }

    /**
     * Gets the amount of vertical pixels that this item will take up on the screen.
     */
    public int getHeight() {
        int messageFontHeight = s.messageFont.getHeight();

        if (type == MESSAGE) {
            // Each content line + little bit of spacing between messages
            int result = messageFontHeight*msg.contentLines.length + messageFontHeight/4;

            // One line for message author
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
                    // Draw author name
                    int authorX = 2;

                    if (selected) g.setColor(ChannelView.selMessageColors[s.theme]);
                    else g.setColor(ChannelView.messageColors[s.theme]);

                    g.setFont(s.authorFont);
                    g.drawString(msg.author, authorX, y, Graphics.TOP | Graphics.LEFT);

                    authorX += s.authorFont.stringWidth(msg.author);

                    // Draw recipient if applicable
                    if (msg.recipient != null) {
                        // Draw arrow between author and recipient
                        g.setFont(s.timestampFont);
                        g.drawString(" -> ", authorX, y, Graphics.TOP | Graphics.LEFT);
                        
                        authorX += s.timestampFont.stringWidth(" -> ");

                        // Draw recipient name
                        g.setFont(s.authorFont);
                        g.drawString(msg.recipient, authorX, y, Graphics.TOP | Graphics.LEFT);

                        authorX += s.authorFont.stringWidth(msg.recipient);
                    }

                    // Draw timestamp
                    if (selected) {
                        g.setColor(ChannelView.selTimestampColors[s.theme]);
                    } else {
                        g.setColor(ChannelView.timestampColors[s.theme]);
                    }
                    
                    g.setFont(s.timestampFont);
                    g.drawString("  " + msg.timestamp, authorX, y, Graphics.TOP | Graphics.LEFT);
                    y += s.authorFont.getHeight();
                }

                // Draw message content
                // Use timestamp color for status messages to distinguish them from normal messages
                if (msg.isStatus) {
                    if (selected) {
                        g.setColor(ChannelView.selTimestampColors[s.theme]);
                    } else {
                        g.setColor(ChannelView.timestampColors[s.theme]);
                    }
                } else {
                    if (selected) {
                        g.setColor(ChannelView.selMessageColors[s.theme]);
                    } else {
                        g.setColor(ChannelView.messageColors[s.theme]);
                    }
                }

                g.setFont(s.messageFont);
                for (int i = 0; i < msg.contentLines.length; i++) {
                    g.drawString(msg.contentLines[i], 2, y, Graphics.TOP | Graphics.LEFT);
                    y += messageFontHeight;
                }
                break;
            }

            case OLDER_BUTTON:
            case NEWER_BUTTON: {
                g.setFont(s.messageFont);
                String caption = (type == OLDER_BUTTON) ? "Older messages" : "Newer messages";

                if (selected) {
                    g.setColor(ChannelView.selButtonColors[s.theme]);
                } else {
                    g.setColor(ChannelView.buttonColors[s.theme]);
                }

                int textWidth = s.messageFont.stringWidth(caption);
                g.fillRoundRect(
                    width/2 - textWidth/2 - messageFontHeight,
                    y + messageFontHeight/6,
                    textWidth + messageFontHeight*2,
                    messageFontHeight*4/3,
                    messageFontHeight/2,
                    messageFontHeight/2
                );

                if (selected) {
                    g.setColor(ChannelView.selMessageColors[s.theme]);
                } else {
                    g.setColor(ChannelView.messageColors[s.theme]);
                }

                g.drawString(
                    caption, width/2, y + messageFontHeight/3,
                    Graphics.TOP | Graphics.HCENTER
                );
                break;
            }
        }
    }
}
