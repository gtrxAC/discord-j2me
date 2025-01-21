// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

import cc.nnproject.json.JSONArray;

public class EmojiPicker extends KineticScrollingCanvas implements Strings, CommandListener, Runnable {
    Displayable lastScreen;
    private Command selectCommand;
    private Command backCommand;
    private State s;
    private Image[] sheets;
    private Image[] selectors;
    private int selectorIndex;
    private boolean running = true;
    private JSONArray emojiJson;
    
    private int sheetWidth;
    private int emojiSize;
    private int selected;

    public static void show(State s) {
        try {
            RecordStore rms = RecordStore.openRecordStore("emoji", false);
            rms.closeRecordStore();
            s.disp.setCurrent(new EmojiPicker(s));
        }
        catch (Exception e) {
            s.disp.setCurrent(new EmojiDownloadDialog(s));
        }
    }

    EmojiPicker(State s) {
        super();
        setTitle(Locale.get(EMOJI_PICKER_TITLE));
        setCommandListener(this);

        lastScreen = s.disp.getCurrent();
        this.s = s;
        emojiSize = FormattedStringPartEmoji.emojiSize*4/3/16*16;
        sheetWidth = emojiSize*45;
        emojiJson = FormattedStringPartEmoji.loadEmojiJson();

        selectCommand = Locale.createCommand(SELECT, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);
        addCommand(selectCommand);
        addCommand(backCommand);

        try {
            Spritesheet selectorSheet = new Spritesheet("/selector.png", emojiSize/16*20);
            selectorSheet.blockSize = 20;
            selectors = new Image[2];
            selectors[0] = selectorSheet.next();
            selectors[1] = selectorSheet.next();
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
        try {
            rms.closeRecordStore();
        }
        catch (Exception e) {}

        scroll = getMinScroll();
        scrollUnit = s.messageFont.getHeight();
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

        // BlackBerry fix
        // ifdef BLACKBERRY
        g.setClip(0, 0, getWidth(), getHeight());
        // endif

        // Clear screen
        g.setColor(ChannelView.backgroundColors[s.theme]);
        g.fillRect(0, 0, getWidth(), getHeight());

        int x = 0;
        int y = 0;
        int sheet = 0;
        int screenWidth = getAvailableWidth();

        g.translate((getWidth() - screenWidth)/2, -scroll);
        g.setClip(0, 0, screenWidth, getEmojiAreaHeight());

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

    private void handleKey(int keyCode) {
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

    protected void keyPressed(int keyCode) {
        handleKey(keyCode);
    }

    protected void keyRepeated(int keyCode) {
        handleKey(keyCode);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == selectCommand) {
            String emojiName = emojiJson.getArray(selected).getString(0);
            MentionForm.insertTextToMessageBox(s, lastScreen, " :" + emojiName + ":");
        } else {
            s.disp.setCurrent(lastScreen);
        }
    }

    private boolean threadIsForSelectorAnim;

    public void run() {
        if (threadIsForSelectorAnim) {
            threadIsForSelectorAnim = false;
            while (running) {
                selectorIndex = 1 - selectorIndex;
                repaint();
                Util.sleep(500);
            }
        } else {
            super.run();
        }
    }

    protected void hideNotify() {
        running = false;
    }

    protected void showNotify() {
        running = true;
        threadIsForSelectorAnim = true;
        new Thread(this).start();
    }

    protected void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);

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

    protected void pointerReleased(int x, int y) {
        if (!pointerWasTapped(scrollUnit)) {
            super.pointerReleased(x, y);
            return;
        }
        commandAction(selectCommand, this);
    }
}
// endif