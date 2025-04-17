package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ChannelViewItem {
    static final int MESSAGE = 0;
    static final int OLDER_BUTTON = 1;
    static final int NEWER_BUTTON = 2;

    static int arrowStringWidth;

    int type;  // one of the constants defined above
    Message msg;  // message data for MESSAGE type
    int height;

    public ChannelViewItem(int type) {
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
    public int calculateHeight() {
        int fontHeight = App.messageFont.getHeight();
        int result;

        if (type == MESSAGE) {
            // Each content line + little bit of spacing between messages
            result = fontHeight*msg.contentLines.length + fontHeight/4;

            // One line for message author
            if (msg.showAuthor) result += App.authorFont.getHeight();
        } else {
            // For buttons
            result = fontHeight*5/3;
        }
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

        switch (type) {
            case MESSAGE: {
                // Highlight background if message is selected
                if (selected) {
                    g.setColor(ChannelView.highlightColors[App.theme]);
                    g.fillRect(0, y, width, height);
                }
                
                int x = fontHeight/5;
                y += fontHeight/8;

                if (msg.showAuthor) {
                    // Draw author name
                    int authorX = x;

                    if (selected) g.setColor(ChannelView.selMessageColors[App.theme]);
                    else g.setColor(ChannelView.authorColors[App.theme]);

                    g.setFont(App.authorFont);
                    g.drawString(msg.author, authorX, y, Graphics.TOP | Graphics.LEFT);
                    authorX += App.authorFont.stringWidth(msg.author);

                    // Draw recipient name if applicable
                    if (msg.recipient != null) {
                        authorX += arrowStringWidth;
                        g.drawString(msg.recipient, authorX, y, Graphics.TOP | Graphics.LEFT);
                    }

                    g.setFont(App.timestampFont);
                    if (selected) {
                        g.setColor(ChannelView.selTimestampColors[App.theme]);
                    } else {
                        g.setColor(ChannelView.timestampColors[App.theme]);
                    }

                    // Draw arrow between author and recipient if applicable
                    if (msg.recipient != null) {
                        g.drawString(" > ", authorX, y, Graphics.TOP | Graphics.RIGHT);
                        authorX += App.authorFont.stringWidth(msg.recipient);
                    }
                    // Draw timestamp
                    g.drawString(msg.timestamp, authorX, y, Graphics.TOP | Graphics.LEFT);
                    y += App.authorFont.getHeight();
                }

                // Draw message content
                // Use timestamp color for status messages to distinguish them from normal messages
                if (msg.isStatus) {
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
                for (int i = 0; i < msg.contentLines.length; i++) {
                    g.drawString(msg.contentLines[i], x, y, Graphics.TOP | Graphics.LEFT);
                    y += fontHeight;
                }
                break;
            }

            case OLDER_BUTTON:
            case NEWER_BUTTON: {
                g.setFont(App.messageFont);
                String caption = (type == OLDER_BUTTON) ? "Older messages" : "Newer messages";

                if (selected) {
                    g.setColor(ChannelView.selButtonColors[App.theme]);
                } else {
                    g.setColor(ChannelView.buttonColors[App.theme]);
                }

                int textWidth = App.messageFont.stringWidth(caption);
                g.fillRoundRect(
                    width/2 - textWidth/2 - fontHeight,
                    y + fontHeight/6,
                    textWidth + fontHeight*2,
                    fontHeight*4/3,
                    fontHeight/2,
                    fontHeight/2
                );

                if (selected) {
                    g.setColor(ChannelView.selMessageColors[App.theme]);
                } else {
                    g.setColor(ChannelView.messageColors[App.theme]);
                }

                g.drawString(
                    caption, width/2, y + fontHeight/3,
                    Graphics.TOP | Graphics.HCENTER
                );
                break;
            }
        }
    }
}
