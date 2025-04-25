package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Item that is displayed in the ChannelView screen. Items are shown on top of each other and can be highlighted by scrolling.
 * This class is used for buttons ('Older messages' and 'Newer messages'). The 'Message' subclass is used for messages.
 */
public class ChannelViewItem {
    static final ChannelViewItem OLDER_MESSAGES_BUTTON = new ChannelViewItem("Older messages");
    static final ChannelViewItem NEWER_MESSAGES_BUTTON = new ChannelViewItem("Newer messages");

    static int fontYOffset;

    String label;
    int height;

    public ChannelViewItem(String label) {
        this.label = label;
    }

    /**
     * Gets the amount of vertical pixels that this item will take up on the screen.
     */
    public int calculateHeight() {
        int result = App.messageFont.getHeight()*5/3;
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

        g.setFont(App.messageFont);

        if (selected) {
            g.setColor(ChannelView.selButtonColors[App.theme]);
        } else {
            g.setColor(ChannelView.buttonColors[App.theme]);
        }

        int textWidth = App.messageFont.stringWidth(label);
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
            label,
            width/2,
            y + fontHeight/3 + fontYOffset,
            Graphics.TOP | Graphics.HCENTER
        );
    }
}
