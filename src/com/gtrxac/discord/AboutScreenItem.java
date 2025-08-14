// ifdef OLD_ABOUT_SCREEN
// else
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class AboutScreenItem {
    public String str;
    public String[] strLines;
    public int size;
    private boolean isTitle;
    private boolean isDeveloper;

    private Font font;
    private int fontHeight;
    public int height;
    private int margin;

    private static int width;
    public static int titleColor;
    public static int contentColor;

    public AboutScreenItem(String str, int size, int margin, boolean isTitle, boolean isDeveloper) {
        this.str = str;
        this.size = size;
        this.margin = margin + height/8;
        this.isTitle = isTitle;
        this.isDeveloper = isDeveloper;

        font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, size);
        fontHeight = font.getHeight();
    }

    public void recalc(int width) {
        AboutScreenItem.width = width;
        if (!isDeveloper) {
            strLines = Util.wordWrap(str, width, font);
            height = fontHeight*strLines.length + margin;
        } else {
            height = fontHeight + margin;
        }
        AboutScreen.maxScroll += height;
    }

    public void draw(Graphics g, boolean draw) {
        int color = (isTitle ? titleColor : contentColor);
        if (draw && color != 0x000000) {
            g.setFont(font);
            
            if (isDeveloper) {
                g.setColor(color & 0x00FFFF);
                int timer = str.length()*AboutScreen.bounceTimer/30;

                if (timer < str.length()) {
                    String leftPart = str.substring(0, timer);
                    char bouncePart = str.charAt(timer);
                    String rightPart = str.substring(timer + 1);

                    int x = width/2 - font.stringWidth(str)/2;
                    g.drawString(leftPart, x, 0, Graphics.TOP | Graphics.LEFT);

                    x += font.stringWidth(leftPart);
                    g.drawChar(bouncePart, x, -height/6, Graphics.TOP | Graphics.LEFT);

                    x += font.charWidth(bouncePart);
                    g.drawString(rightPart, x, 0, Graphics.TOP | Graphics.LEFT);
                } else {
                    g.drawString(str, width/2, 0, Graphics.TOP | Graphics.HCENTER);
                }
                g.translate(0, height);
            } else {
                g.setColor(color);
                for (int i = 0; i < strLines.length; i++) {
                    g.drawString(strLines[i], width/2, 0, Graphics.TOP | Graphics.HCENTER);
                    g.translate(0, fontHeight);
                }
                g.translate(0, margin);
            }
        } else {
            g.translate(0, height);
        }
    }
}
// endif