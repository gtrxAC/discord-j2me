// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class FormattedStringPartMonospace extends FormattedStringPartRichText {
    public FormattedStringPartMonospace(String content, Font font) {
        super(content, font, 0);
        this.font = Font.getFont(Font.FACE_MONOSPACE, font.getStyle(), font.getSize());
    }

    public void draw(Graphics g, int yOffset) {
        int lastColor = g.getColor();
        g.setColor(ChannelView.darkBgColors[Settings.theme]);
        g.fillRect(x, y + yOffset, getWidth(), font.getHeight());
        g.setColor(lastColor);

        super.draw(g, yOffset);
    }

    public boolean isWhitespace() {
        return false;
    }

    public FormattedStringPartText copy(String newContent) {
        return new FormattedStringPartMonospace(newContent, font);
    }

    // public String toString() {
    //     return "monospace (" + content + ")";
    // }
}
// endif