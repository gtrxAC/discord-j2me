// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public abstract class FormattedStringPart {
    int x;
    int y;

    public abstract int getWidth();

    public abstract void draw(Graphics g, int yOffset);
}
// endif