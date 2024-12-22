package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class FormattedStringPartText extends FormattedStringPart {
    String content;
    Font font;

    FormattedStringPartText(String content, Font font) {
        this.content = content;
        this.font = font;
    }

    public int getWidth() {
        return font.stringWidth(content);
    }

    public boolean isWhitespace() {
        return content.trim().length() == 0;
    }

    public void draw(Graphics g, int yOffset) {
        g.drawString(content, x, y + yOffset, Graphics.TOP | Graphics.LEFT);
    }
}