// ifdef EMOJI_SUPPORT
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class FormattedStringPartGuildEmoji extends FormattedStringPart implements HasIcon {
    String id;
    int size;
    private boolean hasLoaded;

    FormattedStringPartGuildEmoji(String id) {
        this.id = id;
        this.size = FormattedStringPartEmoji.emojiSize;
        hasLoaded = false;
    }

    public String getIconID() { return id; }
    public String getIconHash() { return id; }
    public String getIconType() { return "/emojis/"; }
    
    public boolean isDisabled() {
        return false;
    }

    public void iconLoaded() {
		if (App.channelView != null) App.channelView.repaint();
    }

    public int getWidth() {
        return FormattedStringPartEmoji.emojiSize;
    }

    public void draw(Graphics g, int yOffset) {
        Image img = IconCache.getResized(this, size);
        if (img != null) {
            if (!hasLoaded) {
                // Fix positioning for emojis that aren't square (center within square container)
                x += (size - img.getWidth())/2;
                y += (size - img.getHeight())/2;
                hasLoaded = true;
            }
            g.drawImage(img, x, y + yOffset, Graphics.TOP | Graphics.LEFT);
        }
    }

    // public String toString() {
    //     return "guildemoji (" + id + ")";
    // }
}
// endif