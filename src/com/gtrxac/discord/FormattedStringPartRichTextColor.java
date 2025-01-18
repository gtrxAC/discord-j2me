// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class FormattedStringPartRichTextColor extends FormattedStringPartRichText {
    int color;

    public FormattedStringPartRichTextColor(String content, Font font, int style, int color) {
        super(content, font, style);
        this.color = color;
    }

    public void draw(Graphics g, int yOffset) {
        int lastColor = g.getColor();
        g.setColor(color);
        super.draw(g, yOffset);
        g.setColor(lastColor);
    }

    public FormattedStringPartText copy(String newContent) {
        return new FormattedStringPartRichTextColor(newContent, font, font.getStyle(), color);
    }

    // public String toString() {
    //     return "richtextcolor (" + content + ")";
    // }
}
// endif