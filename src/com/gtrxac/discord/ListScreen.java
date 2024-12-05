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
public class ListScreen extends KineticScrollingCanvas {
    private Vector items;
    private Vector displayedItems;

    private boolean hasImages;
    private Vector itemImages;

    private boolean useIndicators;
    private boolean hasIndicators;
    private Vector indicators;

    private boolean useRightItems;
    private boolean separateRightItems;
    private Vector rightItems;

    private int selected;

    private static boolean globalTouchMode;
    private boolean touchMode;
    
    private int itemContentHeight;
    private int itemHeight;
    private int textOffsetY;
    private int iconY;

    public static Font font;  // do not overwrite! use setAppearance instead
    private static int fontHeight;
    private static int margin;
    private static int baseContentHeight;
    private static int minScroll;
    private static int iconSize;
    private static int iconMargin;

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
        iconMargin = Math.max(fontHeight*3/8, iconSize/5);
        baseContentHeight = Math.max(fontHeight, iconSize);

        int[] indicatorCircleBuf = IconResizeThread.createCircleBuf(fontHeight);
        int[] indicatorImageData = new int[(fontHeight/4)*baseContentHeight];

        for (int y = 0; y < baseContentHeight; y++) {
            for (int x = 0; x < fontHeight/4; x++) {
                int circleBufX = x + fontHeight/4;
                int circleBufY = y - (baseContentHeight/2 - fontHeight/4);
                if (circleBufY < 0 || circleBufY >= fontHeight/2) continue;

                indicatorImageData[y*(fontHeight/4) + x] =
                    (0xFF000000 | selectedTextColor) & IconResizeThread.getCircleBufAlpha(indicatorCircleBuf, fontHeight/2, circleBufX, circleBufY);
            }
        }

        indicatorImage = Image.createRGBImage(indicatorImageData, fontHeight/4, baseContentHeight, true);

        ListScreen.selectLabel = selectLabel;
        ListScreen.selectLabelLong = selectLabelLong;
        ListScreen.backLabel = backLabel;
        ListScreen.backLabelLong = backLabelLong;
    }

    ListScreen(String title, int unusedType) {
        this(title, true, false, false);
    }

    ListScreen(String title, boolean useBackCommand, boolean useIndicators, boolean useRightItems) {
        super();
        setTitle(title);
        items = new Vector();
        displayedItems = new Vector();
        itemImages = new Vector();
        selected = 0;
        scroll = minScroll;
        scrollUnit = fontHeight;

        SELECT_COMMAND = new Command(selectLabel, selectLabelLong, Command.OK, 1);
        addCommand(SELECT_COMMAND);
        
        if (useBackCommand) {
            BACK_COMMAND = new Command(backLabel, backLabelLong, Command.BACK, 0);
            addCommand(BACK_COMMAND);
        }
        if (useIndicators) {
            indicators = new Vector();
            this.useIndicators = true;
        }
        if (useRightItems) {
            rightItems = new Vector();
            this.useRightItems = true;
        }
        setItemHeight(getWidth());
    }

    private void setItemHeight(int screenWidth) {
        separateRightItems = screenWidth < font.stringWidth("A")*30;

        if (!useRightItems || !separateRightItems) {
            // Right side items are not used or screen is wide enough: show everything on one line
            itemContentHeight = baseContentHeight;
            textOffsetY = itemContentHeight/2 - fontHeight/2;
        } else {
            // Screen isn't wide enough for right side items: show item and right item on separate lines
            int textHeight = fontHeight*2 + margin;
            itemContentHeight = Math.max(baseContentHeight, textOffsetY + textHeight);
            textOffsetY = itemContentHeight/2 - textHeight/2;
        }
        if (iconSize > fontHeight) {
            iconY = itemContentHeight/2;
        } else {
            iconY = baseContentHeight/2;
        }
        itemHeight = margin*4 + itemContentHeight;
    }

    void append(String text, String rightText, Image image, boolean indicator) {
        items.addElement(text);
        displayedItems.addElement(getDisplayedItem(text, rightText));
        itemImages.addElement(image);
        if (useIndicators) indicators.addElement(indicator ? JSON.TRUE : JSON.FALSE);
        if (useRightItems) rightItems.addElement(rightText);
        checkUpdateDisplayedItems(image, indicator);
    }

    void append(String text, Image image) {
        append(text, null, image, false);
    }

    void set(int index, String text, String rightText, Image image, boolean indicator) {
        items.setElementAt(text, index);
        itemImages.setElementAt(image, index);
        if (useIndicators) indicators.setElementAt(indicator ? JSON.TRUE : JSON.FALSE, index);
        if (useRightItems) rightItems.setElementAt(rightText, index);
        updateDisplayedItem(index);
        checkUpdateDisplayedItems(image, indicator);
    }

    void set(int index, String text, Image image) {
        set(index, text, null, image, false);
    }

    void insert(int index, String text, String rightText, Image image, boolean indicator) {
        items.insertElementAt(text, index);
        itemImages.insertElementAt(image, index);
        if (useIndicators) indicators.insertElementAt(indicator ? JSON.TRUE : JSON.FALSE, index);
        if (useRightItems) rightItems.insertElementAt(rightText, index);
        displayedItems.addElement(null);
        checkUpdateDisplayedItems(image, indicator, true);
    }

    void insert(int index, String text, Image image) {
        insert(index, text, null, image, false);
    }

    void delete(int index) {
        items.removeElementAt(index);
        itemImages.removeElementAt(index);
        if (useIndicators) indicators.removeElementAt(index);
        if (useRightItems) rightItems.removeElementAt(index);
        updateDisplayedItems();
        repaint();
    }

    private void checkUpdateDisplayedItems(Image image, boolean indicator, boolean alwaysUpdate) {
        boolean shouldUpdate = alwaysUpdate;
        if (image != null && !hasImages) {
            hasImages = true;
            shouldUpdate = true;
        }
        if (useIndicators && indicator && !hasIndicators) {
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
        if (useIndicators) indicators = new Vector();
        if (useRightItems) rightItems = new Vector();
        selected = 0;
        scroll = 0;
        repaint();
    }

    int getSelectedIndex() {
        return selected;
    }

    void setSelectedIndex(int index, boolean unused) {
        selected = index;
        makeSelectedItemVisible();
    }

    String getString(int index) {
        return (String) items.elementAt(index);
    }

    private void makeSelectedItemVisible() {
        // Make sure item is visible on screen
        int itemPos = getItemPosition(selected);
        if (itemPos < 0) {
            scroll += itemPos;
        }
        else if (itemPos + itemHeight > getHeight()) {
            scroll += (itemPos + itemHeight) - getHeight();
        }
    }

    public void setCommandListener(CommandListener newListener) {
        listener = newListener;
        super.setCommandListener(newListener);
    }

    private String getDisplayedItem(String item, String rightItem) {
        int leftMargin = fontHeight/2;
        if (hasIndicators) leftMargin += fontHeight/3;
        if (hasImages) leftMargin += iconSize + iconMargin;

        int area = getWidth() - leftMargin - fontHeight/4;
        if (rightItem != null && !separateRightItems) area -= font.stringWidth(rightItem);

        if (font.stringWidth(item) < area) return item;

        area -= font.stringWidth("...");
        // Reduce string length until it fits on the screen
        while (font.stringWidth(item) >= area && item.length() > 0) {
            item = item.substring(0, item.length() - 1);
        }
        return item + "...";
    }

    private void updateDisplayedItem(int i) {
        String item = (String) items.elementAt(i);
        String rightItem = null;
        if (useRightItems) rightItem = (String) rightItems.elementAt(i);
        displayedItems.setElementAt(getDisplayedItem(item, rightItem), i);
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

    protected int getMinScroll() {
        return minScroll;
    }
    protected int getMaxScroll() {
        if (items == null) return minScroll;
        return Math.max(items.size()*itemHeight - getHeight(), minScroll);
    }

    protected void showNotify() {
        touchMode = globalTouchMode;
    }

    protected void sizeChanged(int w, int h) {
        setItemHeight(w);
        updateDisplayedItems();
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
            if (useIndicators && ((Boolean) indicators.elementAt(i)).booleanValue()) {
                g.drawImage(indicatorImage, 0, y, Graphics.TOP | Graphics.LEFT);
            }

            Image img = (Image) itemImages.elementAt(i);
            if (img != null) {
                g.drawImage(
                    img,
                    baseX + fontHeight/2 + iconSize/2,
                    y + iconY,
                    Graphics.HCENTER | Graphics.VCENTER
                );
            }

            String item = (String) displayedItems.elementAt(i);
            g.setColor(thisSelected ? selectedTextColor : textColor);
            g.drawString(item, textX, y + textOffsetY, Graphics.TOP | Graphics.LEFT);

            if (useRightItems) {
                String rightItem = (String) rightItems.elementAt(i);

                if (rightItem != null) {
                    g.setColor(noItemsColor);

                    if (separateRightItems) {
                        // Right items are separate: draw right item left-aligned one row below
                        g.drawString(
                            rightItem,
                            textX,
                            y + textOffsetY + fontHeight + margin,
                            Graphics.TOP | Graphics.LEFT
                        );
                    } else {
                        // Right items not separate: draw right item right-aligned on same row
                        g.drawString(
                            rightItem,
                            getWidth() - fontHeight/2,
                            y + textOffsetY,
                            Graphics.TOP | Graphics.RIGHT
                        );
                    }
                }
            }

            y += itemContentHeight + margin*2;

            if (y > getHeight()) break;
        }
        // g.setColor(0xFF0000);
        // g.drawString(new Integer(velocity).toString(), 0, 0, Graphics.TOP | Graphics.LEFT);

        drawScrollbar(g);
    }

    public void customKeyEvent(int keycode) {}
    
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
            default: {
                customKeyEvent(keycode);
                break;
            }
        }
        makeSelectedItemVisible();
        repaint();
    }
    protected void keyPressed(int a) { keyEvent(a); }
    protected void keyRepeated(int a) { keyEvent(a); }

    private boolean pressedOnBlank;

    protected void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);

        if (y > getItemPosition(items.size())) {
            pressedOnBlank = true;
        }
        else if (!usingScrollBar) {
            pressedOnBlank = false;
            touchMode = false;
            for (int i = 0; i < items.size(); i++) {
                int itemPos = getItemPosition(i);
                if (y < itemPos || y >= itemPos + itemHeight) continue;
                selected = i;
            }
        }
        repaint();
    }

    protected void pointerReleased(int x, int y) {
        if (!pressedOnBlank) {
            if (totalScroll > fontHeight/4) {
                // Scrolled: start kinetic scrolling if needed
                touchMode = true;
                super.pointerReleased(x, y);
            }
            else {
                // Not scrolled and not tapped on empty space: select item
                globalTouchMode = true;
                touchMode = false;
                listener.commandAction(SELECT_COMMAND, this);
            }
        }
        repaint();
    }
}