package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class Spritesheet {
    private int spriteSize;
    protected Image sheet;
    private int x;
    private int y;

    /**
     * Create new spritesheet. Spritesheet class is used for reading sequential 16x16 blocks from a larger image.
     * @param fileName Name of image file inside JAR to read from
     * @param spriteSize Pixel size (width and height, which are the same) to scale each of the sprites (16x16 blocks) to.
     */
    Spritesheet(String fileName, int spriteSize) throws Exception {
        sheet = Image.createImage(fileName);
        this.spriteSize = spriteSize;
    }
    
    public Image next() {
        Image result = Image.createImage(sheet, x, y, 16, 16, Sprite.TRANS_NONE);
        
        // Integer scale to nearest multiple of 16px, rounding up
        if (spriteSize > 16) {
            int multiple = spriteSize/16*16;
            if (multiple < spriteSize) multiple += 16;
            result = Util.resizeImage(result, multiple, multiple);
        }
        // If requested icon size is not an integer multiple, scale down to requested size with bilinear filter
        if (spriteSize % 16 != 0) {
            result = Util.resizeImageBilinear(result, spriteSize, spriteSize);
        }

        x += 16;
        if (x >= sheet.getWidth()) {
            x = 0;
            y += 16;
        }
        return result;
    }
}