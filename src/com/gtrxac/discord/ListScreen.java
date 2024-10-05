package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Canvas-based replacement for LCDUI List.
 * 
 * Notes:
 * - Only supports IMPLICIT list type
 * - In commandAction, use SELECT_COMMAND, not List.SELECT_COMMAND
 * - ListScreen has its own BACK_COMMAND, which is not part of the standard List API.
 *   If you want to use your own back command, or don't need a back command,
 *   remove it (either from this code or by using removeCommand())
 */
public abstract class ListScreen extends KineticScrollingCanvas {
    private Vector items;
    private Vector displayedItems;
    private Vector itemImages;
    private Vector indicators;
    private boolean hasImages;
    private boolean hasIndicators;

    private int selected;

    private static boolean globalTouchMode;
    private boolean touchMode;

    private static Font font;
    private static int fontHeight;
    private static int margin;
    private static int itemHeight;
    private static int itemContentHeight;
    private static int minScroll;
    private static int iconSize;
    private static int iconMargin;
    private static int textOffsetY;

    public static int backgroundColor;
    public static int textColor;
    public static int selectedTextColor;
    public static int highlightColor;
    public static int noItemsColor;

    public static String selectLabel;
    public static String selectLabelLong;
    public static String backLabel;
    public static String backLabelLong;
    public static String noItemsString;

    public Command SELECT_COMMAND;
    public Command BACK_COMMAND;
    private CommandListener listener;

    private static Image indicatorImage;

    /**
     * Sets the font and icon size used by list screens in this app. This method should be called before your app uses any list screens.
     * The font size is used to determine the sizes of layout features like margins.
     * Other appearance-related variables (colors) are accessible via public static fields.
     * @param newFont Font to be used for drawing list screens.
     * @param newIconSize Icon size in pixels to be used by list screens. Some icons can be a different size than this, in which case they get centered without being resized.
     * @param selectLabel Short text label to use for the "Select" softkey in list screens. This can usually just be "Select" unless your app supports other languages.
     * @param selectLabelLong Long label to use for "Select" softkey. If a long label is not needed, this can be the same as selectLabel.
     * @param backLabel Short label to use for "Back" softkey.
     * @param backLabelLong Long label to use for "Back" softkey.
     */
    public static void setAppearance(Font newFont, int newIconSize, String selectLabel, String selectLabelLong, String backLabel, String backLabelLong) {
        font = newFont;
        fontHeight = newFont.getHeight();
        margin = fontHeight/7;
        minScroll = -fontHeight/6;

        iconSize = newIconSize;
        iconMargin = Math.max(fontHeight/3, iconSize/5);
        itemContentHeight = Math.max(fontHeight, iconSize);
        itemHeight = margin*4 + itemContentHeight;
        textOffsetY = itemContentHeight/2 - fontHeight/2;

        indicatorImage = Image.createImage(fontHeight/2, itemContentHeight*2);
        Graphics indG = indicatorImage.getGraphics();

        indG.setColor(backgroundColor);
        indG.fillRect(0, 0, fontHeight/2, itemContentHeight*2);
        indG.setColor(selectedTextColor);
        indG.fillArc(-fontHeight/2, itemContentHeight - fontHeight/2, fontHeight, fontHeight, 0, 360);

        indicatorImage = Util.resizeImageBilinear(indicatorImage, fontHeight/4, itemContentHeight);

        ListScreen.selectLabel = selectLabel;
        ListScreen.selectLabelLong = selectLabelLong;
        ListScreen.backLabel = backLabel;
        ListScreen.backLabelLong = backLabelLong;
    }

    ListScreen(String title, int unusedType) {
        this(title, true);
    }

    ListScreen(String title, boolean hasBackCommand) {
        super();
        setTitle(title);
        items = new Vector();
        displayedItems = new Vector();
        itemImages = new Vector();
        indicators = new Vector();
        selected = 0;
        scroll = minScroll;
        scrollUnit = fontHeight;

        SELECT_COMMAND = new Command(selectLabel, selectLabelLong, Command.OK, 1);
        addCommand(SELECT_COMMAND);
        
        if (hasBackCommand) {
            BACK_COMMAND = new Command(backLabel, backLabelLong, Command.BACK, 0);
            addCommand(BACK_COMMAND);
        }
    }

    void append(String text, Image image, boolean indicator) {
        items.addElement(text);
        displayedItems.addElement(getDisplayedItem(text));
        itemImages.addElement(image);
        indicators.addElement(indicator ? JSON.TRUE : JSON.FALSE);
        checkUpdateDisplayedItems(image, indicator);
    }

    void append(String text, Image image) {
        append(text, image, false);
    }

    void set(int index, String text, Image image, boolean indicator) {
        items.setElementAt(text, index);
        updateDisplayedItem(index);
        itemImages.setElementAt(image, index);
        indicators.setElementAt(indicator ? JSON.TRUE : JSON.FALSE, index);
        checkUpdateDisplayedItems(image, indicator);
    }

    void set(int index, String text, Image image) {
        set(index, text, image, false);
    }

    void insert(int index, String text, Image image, boolean indicator) {
        items.insertElementAt(text, index);
        itemImages.insertElementAt(image, index);
        indicators.insertElementAt(indicator ? JSON.TRUE : JSON.FALSE, index);
        displayedItems.addElement(null);
        checkUpdateDisplayedItems(image, indicator, true);
    }

    void insert(int index, String text, Image image) {
        insert(index, text, image, false);
    }

    void delete(int index) {
        items.removeElementAt(index);
        itemImages.removeElementAt(index);
        indicators.removeElementAt(index);
        updateDisplayedItems();
        repaint();
    }

    private void checkUpdateDisplayedItems(Image image, boolean indicator, boolean alwaysUpdate) {
        boolean shouldUpdate = alwaysUpdate;
        if (image != null && !hasImages) {
            hasImages = true;
            shouldUpdate = true;
        }
        if (indicator && !hasIndicators) {
            hasIndicators = true;
            shouldUpdate = true;
        }
        if (shouldUpdate) updateDisplayedItems();
        repaint();
    }

    private void checkUpdateDisplayedItems(Image image, boolean indicator) {
        checkUpdateDisplayedItems(image, indicator, false);
    }

    void deleteAll() {
        items = new Vector();
        displayedItems = new Vector();
        itemImages = new Vector();
        selected = 0;
        repaint();
    }

    int getSelectedIndex() {
        return selected;
    }

    String getString(int index) {
        return (String) items.elementAt(index);
    }

    public void setCommandListener(CommandListener newListener) {
        listener = newListener;
        super.setCommandListener(newListener);
    }

    private String getDisplayedItem(String item) {
        int leftMargin = fontHeight/2;
        if (hasIndicators) leftMargin += fontHeight/3;
        if (hasImages) leftMargin += iconSize + iconMargin;
        int area = getWidth() - leftMargin - fontHeight/4;

        if (font.stringWidth(item) < area) return item;

        area -= font.stringWidth("...");
        // Reduce string length until it fits on the screen
        while (font.stringWidth(item) >= area && item.length() > 0) {
            item = item.substring(0, item.length() - 1);
        }
        return item + "...";
    }

    private void updateDisplayedItem(int i) {
        String dispItem = (String) items.elementAt(i);
        displayedItems.setElementAt(getDisplayedItem(dispItem), i);
    }

    private void updateDisplayedItems() {
        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            updateDisplayedItem(i);
        }
        repaint();
    }

    // Get the screen Y position that an item will be drawn at
    private int getItemPosition(int index) {
        return -scroll + itemHeight*index;
    }

    private int getMaxScroll() {
        return Math.max(items.size()*itemHeight - getHeight(), minScroll);
    }

    protected void showNotify() {
        touchMode = globalTouchMode;
    }

    protected void sizeChanged(int w, int h) {
        updateDisplayedItems();
    }

    protected void checkScrollInRange() {
        if (scroll < minScroll) scroll = minScroll;
        if (scroll > getMaxScroll()) scroll = getMaxScroll();
    }

    protected void paint(Graphics g) {
        // BlackBerry fix
        g.setClip(0, 0, getWidth(), getHeight());

        // Clear screen
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(font);

        if (items.size() == 0) {
            g.setColor(noItemsColor);
            g.drawString(
                noItemsString, getWidth()/2, getHeight()/2 - fontHeight/2,
                Graphics.HCENTER | Graphics.TOP
            );
            return;
        }

        checkScrollInRange();
        int y = -scroll;

        // int textX = hasImages ? fontHeight/2 + iconSize + iconMargin : fontHeight/2;
        int baseX = hasIndicators ? fontHeight/3 : 0;
        int textX = fontHeight/2 + baseX;
        if (hasImages) textX += iconSize + iconMargin;

        for (int i = 0; i < items.size(); i++) {
            if (y + itemHeight < 0) {
                y += itemHeight;
                continue;
            }

            boolean thisSelected = (selected == i) && !touchMode;

            y += margin;
            if (thisSelected) {
                g.setColor(highlightColor);
                g.fillRoundRect(
                    baseX + fontHeight/4,
                    y,
                    getWidth() - baseX - fontHeight/2,
                    itemContentHeight + fontHeight/3,
                    fontHeight/2,
                    fontHeight/2
                );
            }
            y += margin;
            if (((Boolean) indicators.elementAt(i)).booleanValue()) {
                g.drawImage(indicatorImage, 0, y, Graphics.TOP | Graphics.LEFT);
            }

            Image img = (Image) itemImages.elementAt(i);
            if (img != null) {
                g.drawImage(
                    img,
                    baseX + fontHeight/2 + itemContentHeight/2,
                    y + itemContentHeight/2,
                    Graphics.HCENTER | Graphics.VCENTER
                );
            }

            String item = (String) displayedItems.elementAt(i);
            g.setColor(thisSelected ? selectedTextColor : textColor);
            g.drawString(item, textX, y + textOffsetY, Graphics.TOP | Graphics.LEFT);
            y += itemContentHeight + margin*2;

            if (y > getHeight()) break;
        }
        // g.setColor(0xFF0000);
        // g.drawString(new Integer(velocity).toString(), 0, 0, Graphics.TOP | Graphics.LEFT);
    }
    
    private void keyEvent(int keycode) {
        touchMode = false;
        globalTouchMode = false;
        int action = getGameAction(keycode);

        // set(0, (new Integer(keycode)).toString() + "/" + (new Integer(action)).toString(), null, false);

        switch (action) {
            case UP: {
                if (selected < 0) selected = 0;
                if (selected == 0) {
                    selected = items.size() - 1;
                } else {
                    selected--;
                }
                break;
            }
            case DOWN: {
                int max = items.size() - 1;
                if (selected > max) selected = max;
                if (selected == max) {
                    selected = 0;
                } else {
                    selected++;
                }
                break;
            }
            case FIRE: {
                listener.commandAction(SELECT_COMMAND, this);
                return;
            }
        }
        int itemPos = getItemPosition(selected);

        // Make sure item is visible on screen
        if (itemPos < 0) {
            scroll += itemPos;
        }
        else if (itemPos + itemHeight > getHeight()) {
            scroll += (itemPos + itemHeight) - getHeight();
        }
        repaint();
    }
    protected void keyPressed(int a) { keyEvent(a); }
    protected void keyRepeated(int a) { keyEvent(a); }

    protected void pointerPressed(int x, int y) {
        touchMode = false;
        super.pointerPressed(x, y);

        for (int i = 0; i < items.size(); i++) {
            int itemPos = getItemPosition(i);
            if (y < itemPos || y >= itemPos + itemHeight) continue;
            selected = i;
        }
        repaint();
    }

    protected void pointerReleased(int x, int y) {
        if (totalScroll > fontHeight/4) {
            touchMode = true;
            super.pointerReleased(x, y);
        } else {
            globalTouchMode = true;
            touchMode = false;
            listener.commandAction(SELECT_COMMAND, this);
        }
        repaint();
    }
}