//#ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

import jtube.ui.nokia.DirectFontUtil;

public class FormattedStringPartRichText extends FormattedStringPartText {
    public FormattedStringPartRichText(String content, Font font, int style) {
        super(content, DirectFontUtil.getFont(font.getFace(), style, FormattedStringParser.fontSize, font.getSize()));
    }

    public void draw(Graphics g, int yOffset) {
        Font lastFont = g.getFont();
        g.setFont(font);
        g.drawString(content, x, y + yOffset, Graphics.TOP | Graphics.LEFT);
        g.setFont(lastFont);
    }

    public FormattedStringPartText copy(String newContent) {
        return new FormattedStringPartRichText(newContent, font, font.getStyle());
    }

    // public String toString() {
    //     return "richtext (" + content + ")";
    // }
}
//#endif