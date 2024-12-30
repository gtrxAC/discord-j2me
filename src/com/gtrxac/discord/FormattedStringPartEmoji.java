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

    private static Hashtable loadedEmoji;
    private static Vector loadedEmojiNames;
    private static Spritesheet loadedSheet;
    private static int loadedSheetId = -1;

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
     * Load emoji names/indexes into hash table. Must be called before using emojis.
     * @param fontSize Pixel size of font used for drawing message contents.
     */
    public static void loadEmoji(int fontSize) {
        emojiSize = getEmojiSize(fontSize);
        imageYOffset = fontSize/2 - emojiSize/2;
        fontSize *= 2;
        largeEmojiSize = getEmojiSize(fontSize);
        largeImageYOffset = fontSize/2 - largeEmojiSize/2;

        // TODO: dynamic load from RMS
        JSONArray emojiArray;
        try {
            emojiArray = JSON.getArray(Util.readFile("/emoji.json"));
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        emojiTable = new Hashtable();
        for (int i = 0; i < emojiArray.size(); i++) {
            JSONArray emojiNames = emojiArray.getArray(i);
            Integer intObj = new Integer(i);
            for (int j = 0; j < emojiNames.size(); j++) {
                emojiTable.put(emojiNames.get(j), intObj);
            }
        }

        loadedEmoji = new Hashtable(10);
        loadedEmojiNames = new Vector(10);
    }

    FormattedStringPartEmoji(String name) {
        Object imgObj = loadedEmoji.get(name);
        if (imgObj != null) {
            this.image = (Image) imgObj;
            return;
        }

        int emojiId = ((Integer) emojiTable.get(name)).intValue();
        int sheetId = emojiId/45;

        if (loadedSheetId != sheetId) {
            try {
                loadedSheet = new Spritesheet("/emoji" + sheetId + ".png", emojiSize);
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
            loadedSheetId = sheetId;
        } else {
            loadedSheet.reset();
        }

        loadedSheet.skip(emojiId%45);
        Image img = loadedSheet.next();

        Util.hashtablePutWithLimit(loadedEmoji, loadedEmojiNames, name, img, 10);
        this.image = img;
    }

    public int getWidth() {
        return emojiSize;
    }

    public void draw(Graphics g, int yOffset) {
        g.drawImage(image, x, y + yOffset, Graphics.TOP | Graphics.LEFT);
    }
}