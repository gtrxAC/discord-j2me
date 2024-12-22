package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class FormattedStringPartEmoji extends FormattedStringPart {
    Image image;

    private static Hashtable emojiTable;
    private static int emojiSize;
    private static int imageYOffset;

    /**
     * Calculate best emoji size (multiple of 16) for a given font size.
     */
    private static int getEmojiSize(int fontSize) {
        System.out.println(fontSize);
        if (fontSize < 12) return 8;
        if (fontSize < 24) return 16;
        if (fontSize < 40) return 32;
        if (fontSize < 56) return 48;
        if (fontSize < 72) return 64;
        return 80;
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

    /**
     * Try to create a formatted string part (emoji).
     * If the emoji name is not recognized, a formatted string part (text) is created instead.
     * @param name Name of the emoji, without colons
     * @return FormattedStringPartEmoji corresponding to the specified emoji name, or a FormattedStringPartText containing the name of the emoji surrounded by colons
     */
    public static FormattedStringPart create(String name, Font font) {
        if (emojiTable.containsKey(name)) {
            return new FormattedStringPartEmoji((Image) emojiTable.get(name));
        }
        return new FormattedStringPartText(":" + name + ":", font);
    }

    private FormattedStringPartEmoji(Image image) {
        this.image = image;
    }

    public int getWidth() {
        return emojiSize;
    }

    public void draw(Graphics g, int yOffset) {
        g.drawImage(image, x, y + yOffset + imageYOffset, Graphics.TOP | Graphics.LEFT);
    }
}