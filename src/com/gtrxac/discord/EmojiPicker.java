//#ifdef EMOJI_SUPPORT
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;

public class EmojiPicker extends KineticScrollingCanvas implements Strings, CommandListener, Runnable {
    Displayable lastScreen;
    private Command selectCommand;
    private Command backCommand;
    private Image[] sheets;
    private Image[] selectors;
    private int selectorIndex;
    private boolean running = true;
    private JSONArray emojiJson;
    
    private int sheetWidth;
    private int emojiSize;
    private int selected;
    private int caretPos = -1;

    public static void show() {
        try {
            RecordStore rms = RecordStore.openRecordStore("emoji", false);
            Util.closeRecordStore(rms);
            App.disp.setCurrent(new EmojiPicker());
        }
        catch (Exception e) {
            App.disp.setCurrent(new EmojiDownloadDialog());
        }
        catch (OutOfMemoryError e) {
            App.error(Locale.get(EMOJI_PICKER_NO_MEMORY));
        }
    }

    EmojiPicker() {
        super();
        setTitle(Locale.get(EMOJI_PICKER_TITLE));
        setCommandListener(this);

        lastScreen = App.disp.getCurrent();

//#ifdef MIDP2_GENERIC
        if (Util.isKemulator) {
            emojiSize = FormattedStringPartEmoji.emojiSize*4/3/16*16;
        } else
//#endif
        {
            int w = getWidth(), h = getHeight();
            if (w < h) emojiSize = w/9;
            else emojiSize = h/6;

            emojiSize = (emojiSize + 8)/16*16;
            if (emojiSize < 16) emojiSize = 16;
        }

        sheetWidth = emojiSize*45;
        emojiJson = FormattedStringPartEmoji.loadEmojiJson();

        selectCommand = Locale.createCommand(SELECT, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);
//#ifdef MIDP2_GENERIC
        if (!Util.isTouch)
//#endif
        addCommand(selectCommand);
        addCommand(backCommand);

        try {
            Spritesheet selectorSheet = new Spritesheet("/selector.png", emojiSize/16*20);
            selectorSheet.blockSize = 20;
            selectors = new Image[4];
            for (int i = 0; i < 4; i++) selectors[i] = selectorSheet.next();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("emoji", false);
            int numRecs = rms.getNumRecords();
    
            sheets = new Image[numRecs - 2];
            for (int i = 3; i <= numRecs; i++) {
                byte[] imageData = rms.getRecord(i);

                sheets[i - 3] = Util.resizeImage(
                    Image.createImage(imageData, 0, imageData.length),
                    sheetWidth, emojiSize
                );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Util.closeRecordStore(rms);

        scroll = getMinScroll();
    }

    private int getEmojiPerRow() {
        return (getWidth() - emojiSize/4)/emojiSize;
    }

    private int getAvailableWidth() {
        return getEmojiPerRow()*emojiSize;
    }

    private int getEmojiAreaHeight() {
        int emojiPerRow = getEmojiPerRow();
        return ((emojiJson.size() + emojiPerRow - 1)/emojiPerRow)*emojiSize;
    }

    protected int getMinScroll() {
        return -emojiSize/6;
    }

    protected int getMaxScroll() {
        return getEmojiAreaHeight() - getHeight();
    }

    protected void paint(Graphics g) {
        checkScrollInRange();

        clearScreen(g, Theme.emojiPickerBackgroundColor);

        int x = 0;
        int y = 0;
        int sheet = 0;
        int screenWidth = getAvailableWidth();

        g.translate((getWidth() - screenWidth)/2, -scroll);
        g.setClip(0, scroll, screenWidth, getHeight());

        while (sheet < sheets.length) {
            g.drawImage(sheets[sheet], x, y, Graphics.TOP | Graphics.LEFT);

            // remaining screen space on current row
            int remain = screenWidth - x - sheetWidth;

            if (remain == 0) {
                // this line fit perfectly - go to next sheet and start of next row
                y += emojiSize;
                // if (y >= getHeight()) break;
                x = 0;
                sheet++;
                continue;
            }
            if (remain > 0) {
                // remaining space on current row - draw next sheet after this one
                x += sheetWidth;
                sheet++;
                continue;
            }
            // part of this sheet went past the screen edge - draw more of it on the next row
            y += emojiSize;
            x = -sheetWidth - remain;
        }

        int emojiPerRow = screenWidth/emojiSize;
        int cursorY = (selected/emojiPerRow)*emojiSize;
        int cursorX = (selected%emojiPerRow)*emojiSize;
        
        g.setClip(-g.getTranslateX(), scroll, getWidth(), getHeight());
        g.drawImage(
            selectors[selectorIndex],
            cursorX - emojiSize/8, cursorY - emojiSize/8,
            Graphics.TOP | Graphics.LEFT
        );
        
        g.translate(-g.getTranslateX(), scroll);
        drawScrollbar(g);
    }

    private void makeSelectedItemVisible() {
        // Make sure item is visible on screen
        int itemPos = selected/getEmojiPerRow()*emojiSize - scroll;
        if (itemPos < 0) {
            scroll += itemPos - emojiSize/8;
        }
        else if (itemPos + emojiSize > getHeight()) {
            scroll += (itemPos + emojiSize) - getHeight() + emojiSize/8;
        }
    }

    protected void keyAction(int keyCode) {
        switch (getGameAction(keyCode)) {
            case UP: {
                selected = Math.max(selected - getEmojiPerRow(), 0);
                break;
            }
            case DOWN: {
                selected = Math.min(selected + getEmojiPerRow(), emojiJson.size() - 1);
                break;
            }
            case LEFT: {
                selected = Math.max(selected - 1, 0);
                break;
            }
            case RIGHT: {
                selected = Math.min(selected + 1, emojiJson.size() - 1);
                break;
            }
            case FIRE: {
                commandAction(selectCommand, this);
                return;
            }
        }
        makeSelectedItemVisible();
        repaint();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == selectCommand) {
            JSONArray emojiNames = emojiJson.getArray(selected);
            String emojiName = emojiNames.getString(emojiNames.size() - 1);
            caretPos = MentionForm.insertTextToMessageBox(lastScreen, ":" + emojiName + ":", caretPos);

            selectorIndex |= 2;
            repaint();
            threadIsForSelectorConfirm = true;
            new Thread(this).start();
        } else {
            App.disp.setCurrent(lastScreen);
        }
    }

    private boolean threadIsForSelectorAnim;
    private boolean threadIsForSelectorConfirm;

    public void run() {
        if (threadIsForSelectorAnim) {
            threadIsForSelectorAnim = false;
            while (running) {
                selectorIndex ^= 1;
                repaint();
                Util.sleep(500);
            }
        }
        else if (threadIsForSelectorConfirm) {
            threadIsForSelectorConfirm = false;
            Util.sleep(200);
            selectorIndex &= 1;
            repaint();
        }
//#ifdef TOUCH_SUPPORT
        // for kineticscrollingcanvas scroll thread
        else {
            super.run();
        }
//#endif
    }

    protected void hideNotify() {
        running = false;
    }

    protected void showNotify() {
        running = true;
        threadIsForSelectorAnim = true;
        new Thread(this).start();
    }

//#ifdef TOUCH_SUPPORT
    protected void _pointerPressed(int x, int y) {
        super._pointerPressed(x, y);

        if (!usingScrollBar) {
            int screenWidth = getAvailableWidth();
            x -= (getWidth() - screenWidth)/2;
            if (x < 0 || x >= screenWidth) return;
            y += scroll;
    
            int emojiPerRow = screenWidth/emojiSize;
            x /= emojiSize;
            y /= emojiSize;
    
            int index = x + y*emojiPerRow;
            selected = Math.min(index, emojiJson.size() - 1);
        }
    }

    protected void _pointerReleased(int x, int y) {
        if (!pointerWasTapped(Util.fontSize)) {
            super._pointerReleased(x, y);
            return;
        }
        commandAction(selectCommand, this);
    }
//#endif
}
//#endif