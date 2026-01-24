package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Item that is displayed in the ChannelView screen. Items are shown on top of each other and can be highlighted by scrolling.
 * This class is used for buttons ('Older messages' and 'Newer messages'). The 'Message' subclass is used for messages.
 */
public class ChannelViewItem {
    static ChannelViewItem olderMessagesButton;
    static ChannelViewItem newerMessagesButton;

    static int fontHeight;
    static int authorFontHeight;
    static int fontYOffset;

    static int backgroundColor;
    static int highlightColor;
    static int buttonColor;
    static int selButtonColor;
    static int messageColor;
    static int selMessageColor;
    static int authorColor;
    static int timestampColor;
    static int selTimestampColor;

    String label;
    int labelOrAuthorWidth;
    int height;
    int pos;

    public ChannelViewItem() {}

    public ChannelViewItem(String label) {
        this.label = label;
        labelOrAuthorWidth = App.messageFont.stringWidth(label);
        height = fontHeight*5/3;
    }

    /**
     * Draws this channel view item on the screen.
     * @param g Graphics object to use for drawing.
     * @param y Vertical position (offset) to draw at, in pixels.
     * @param width Horizontal area available for drawing, in pixels.
     */
    public void draw(Graphics g, int y, int width, boolean selected) {
        g.setColor(selected ? selButtonColor : buttonColor);
        g.fillRoundRect(
            width/2 - labelOrAuthorWidth/2 - fontHeight,
            y + fontHeight/6,
            labelOrAuthorWidth + fontHeight*2,
            fontHeight*4/3,
            fontHeight/2,
            fontHeight/2
        );

        g.setColor(selected ? selMessageColor : messageColor);
        g.drawString(
            label,
            width/2,
            y + fontHeight/3 + fontYOffset,
            Graphics.TOP | Graphics.HCENTER
        );
    }
}
