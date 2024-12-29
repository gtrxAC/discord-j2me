package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class FormattedStringPartEmoji extends FormattedStringPart {
    Image image;

    static Hashtable emojiTable;
    static int emojiSize;
    static int largeEmojiSize;
    static int imageYOffset;
    static int largeImageYOffset;

    /**
     * Calculate best emoji size (multiple of 16) for a given font size.
     */
    public static int getEmojiSize(int fontSize) {
        if (fontSize < 12) return 8;
        for (int i = 16; ; i += 16) {
            if (fontSize < i + 8) return i;
        }
    }

    /**
     * Load emoji images into hash table. Must be called before using emojis.
     * @param fontSize Pixel size of font used for drawing message contents.
     */
    public static void loadEmoji(int fontSize) {
        emojiTable = new Hashtable();
        try {
            emojiSize = getEmojiSize(fontSize);
            imageYOffset = fontSize/2 - emojiSize/2;
            fontSize *= 2;
            largeEmojiSize = getEmojiSize(fontSize);
            largeImageYOffset = fontSize/2 - largeEmojiSize/2;

            Spritesheet emojiSheet = new Spritesheet("/emoji.png", emojiSize);
            JSONArray emojiArray = JSON.getArray(Util.readFile("/emoji.json"));

            for (int i = 0; i < emojiArray.size(); i++) {
                JSONArray emojiNames = emojiArray.getArray(i);
                Image emojiImage = emojiSheet.next();

                for (int j = 0; j < emojiNames.size(); j++) {
                    emojiTable.put(emojiNames.getString(j), emojiImage);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    FormattedStringPartEmoji(Image image) {
        this.image = image;
    }

    public int getWidth() {
        return emojiSize;
    }

    public void draw(Graphics g, int yOffset) {
        g.drawImage(image, x, y + yOffset, Graphics.TOP | Graphics.LEFT);
    }
}