package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class Spritesheet {
    private int spriteSize;
    private Image sheet;
    private int x;
    private int y;

    public
    // ifdef SAMSUNG_100KB
    static final
    // endif
    int blockSize = 16;

    /**
     * Create new spritesheet. Spritesheet class is used for reading sequential 16x16 blocks from a larger image.
     * @param fileName Name of image file inside JAR to read from
     * @param spriteSize Pixel size (width and height, which are the same) to scale each of the sprites (16x16 blocks) to.
     */
    Spritesheet(String fileName, int spriteSize) throws Exception {
        sheet = Image.createImage(fileName);
        this.spriteSize = spriteSize;
    }

    /**
     * @param imageData Data of image file to use as the spritesheet (usually PNG)
     */
    Spritesheet(byte[] imageData, int spriteSize) {
        sheet = Image.createImage(imageData, 0, imageData.length);
        this.spriteSize = spriteSize;
    }
    
    /**
     * Get the next sequential sprite (16x16 block) from the spritesheet
     * @return The next 16x16 block from the loaded spritesheet, scaled to the spritesheet's specified sprite size
     */
    public Image next() {
        Image result = Image.createImage(sheet, x, y, blockSize, blockSize, Sprite.TRANS_NONE);
        
        // Integer scale to nearest multiple of 16px, rounding up
        if (spriteSize > blockSize) {
            int multiple = spriteSize/blockSize*blockSize;
            if (multiple < spriteSize) multiple += blockSize;
            result = Util.resizeImage(result, multiple, multiple);
        }
        // If requested icon size is not an integer multiple, scale down to requested size with bilinear filter
        if (spriteSize % blockSize != 0) {
            result = Util.resizeImageBilinear(result, spriteSize, spriteSize);
        }

        skip();
        return result;
    }

    /**
     * Reset the current read position of the spritesheet.
     * After calling this, the next sprite returned by next() will be the first (top-left corner) sprite in the sheet.
     */
    public void reset() {
        x = y = 0;
    }

    /**
     * Move the current read position of the spritesheet to skip past one sprite without reading it.
     */
    public void skip() {
        x += blockSize;
        if (x >= sheet.getWidth()) {
            x = 0;
            y += blockSize;
        }
    }

    /**
     * Move the current read position of the spritesheet to skip past `count` sprites without reading them.
     * @param count Amount of sprites (16x16 blocks) to skip
     */
    public void skip(int count) {
        int width = sheet.getWidth();
        x += blockSize*count;
        y += x/width*blockSize;
        x %= width;
    }
}