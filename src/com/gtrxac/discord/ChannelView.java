package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends Canvas implements CommandListener {
    private Command backCommand;
    private Command selectCommand;
    private Command sendCommand;
    private Command replyCommand;
    private Command editCommand;
    private Command deleteCommand;
    private Command refreshCommand;

    public Vector items;

    // Parameters for viewing old messages (message IDs)
    int page;
    String before;
    String after;

    boolean haveShown;
    boolean haveDrawn;
    int scroll;
    int maxScroll;
    int pressY;

    boolean touchMode;
    boolean selectionMode;
    int selectedItem;

    int fontHeight;
    int width, height;

    boolean requestedUpdate;

    //                                      Monochrome Dark      Light
    static final int[] backgroundColors =   {0xFFFFFF, 0x313338, 0xFFFFFF};
    static final int[] highlightColors =    {0x000000, 0x1e1f22, 0xBBBBBB};
    static final int[] buttonColors =       {0xFFFFFF, 0x2b2d31, 0xCCCCCC};
    static final int[] selButtonColors =    {0x000000, 0x1e1f22, 0xAAAAAA};
    static final int[] messageColors =      {0x000000, 0xEEEEEE, 0x111111};
    static final int[] selMessageColors =   {0xFFFFFF, 0xFFFFFF, 0x000000};
    static final int[] authorColors =       {0x000000, 0xFFFFFF, 0x000000};
    static final int[] timestampColors =    {0x000000, 0xAAAAAA, 0x777777};
    static final int[] selTimestampColors = {0xFFFFFF, 0xBBBBBB, 0x555555};

    public ChannelView() {
        super();
        setCommandListener(this);

        backCommand = new Command("Back", Command.BACK, 0);
        selectCommand = new Command("Select", Command.OK, 1);
        sendCommand = new Command("Send", Command.ITEM, 2);
        replyCommand = new Command("Reply", Command.ITEM, 3);
        editCommand = new Command("Edit", Command.ITEM, 4);
        deleteCommand = new Command("Delete", Command.ITEM, 5);
        refreshCommand = new Command("Refresh", Command.ITEM, 6);

        fontHeight = App.messageFont.getHeight();
        ChannelViewItem.arrowStringWidth = App.timestampFont.stringWidth(" > ");

        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);
    }

    protected void showNotify() {
        if (haveShown) return;
        haveShown = true;
        width = getWidth();
        height = getHeight();
        requestUpdate();
        repaint();
    }

    public void requestUpdate() {
        requestedUpdate = true;
    }

    private void update(boolean wasResized) {
        items = new Vector(App.messageLoadCount + 2);

        int messageCount = App.messages.size();
        if (messageCount == 0) return;

        int oldMaxScroll = maxScroll;
        maxScroll = -getHeight();

        if (page > 0) {
            ChannelViewItem newerItem = new ChannelViewItem(ChannelViewItem.NEWER_BUTTON);
            items.addElement(newerItem);
            maxScroll += newerItem.calculateHeight();
        }

        Message first = (Message) App.messages.elementAt(messageCount - 1);
        first.showAuthor = true;

        Message above = first;
        String clusterStart = first.id;

        if (messageCount > 1) {
            for (int i = messageCount - 2; i >= 0; i--) {
                Message msg = (Message) App.messages.elementAt(i);
                msg.showAuthor = msg.shouldShowAuthor(above, clusterStart);

                if (msg.showAuthor) clusterStart = msg.id;
                above = msg;
            }
        }

        int availableWidth = getWidth() - fontHeight/5;

        for (int i = 0; i < messageCount; i++) {
            Message msg = (Message) App.messages.elementAt(i);

            if (msg.contentLines == null || wasResized || msg.needUpdate) {
                msg.contentLines = Util.wordWrap(msg.content, availableWidth, App.messageFont);
                msg.needUpdate = false;
            }

            ChannelViewItem msgItem = new ChannelViewItem(ChannelViewItem.MESSAGE);
            msgItem.msg = msg;
            items.addElement(msgItem);
            maxScroll += msgItem.calculateHeight();
        }

        if (messageCount >= App.messageLoadCount) {
            ChannelViewItem olderItem = new ChannelViewItem(ChannelViewItem.OLDER_BUTTON);
            items.addElement(olderItem);
            maxScroll += olderItem.calculateHeight();
        }

        if (haveDrawn && wasResized) {
            // If this channel view has been previously drawn and was just resized
            // (e.g. screen rotate), keep the previous relative scroll value
            int scrollPercent = (scroll*100)/oldMaxScroll;
            scroll = (scrollPercent*maxScroll)/100;
        } else {
            // If user selected Show newer messages, go to the top of the
            // message list, so it's more intuitive to scroll through
            scroll = (after != null) ? 0 : maxScroll;
            selectedItem = (after != null) ? (items.size() - 1) : 0;
            selectionMode = (before != null || after != null);
        }
    }

    private void getMessages() {
        HTTPThread h = new HTTPThread(HTTPThread.FETCH_MESSAGES);
        h.fetchMsgsBefore = before;
        h.fetchMsgsAfter = after;
        h.start();
    }

    // Get the screen Y position that an item will be drawn at
    public int getItemPosition(int index) {
        int y = -scroll;
        for (int i = items.size() - 1; i > index; i--) {
            y += ((ChannelViewItem) items.elementAt(i)).height;
        }
        return y;
    }

    // Ensure that the selected item is visible on screen
    private void makeSelectedItemVisible() {
        if (!selectionMode || touchMode) return;

        ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
        int itemPos = getItemPosition(selectedItem);
        int itemHeight = selected.height;

        if (itemHeight > height) {
            // For items taller than the screen, make sure one screenful of it is visible:
            // Check if item is above the visible area
            if (itemPos + itemHeight < 0) {
                scroll -= height;
            }
            // Check if below the visible area
            else if (itemPos > height) scroll += height;
        } else {
            // For shorter items, make sure the entire item is visible:
            // Check if item is above the visible area
            if (itemPos < 0) {
                scroll += itemPos;
            }
            // Check if below the visible area
            else if (itemPos + itemHeight > height) {
                scroll += (itemPos + itemHeight) - height;
            }
        }
    }

    protected void sizeChanged(int w, int h) {
        repaint();
    }

    private void updateCommands(ChannelViewItem selected) {
        if (selectionMode && (selected.msg == null || !selected.msg.isStatus)) {
            if (selected.type == ChannelViewItem.MESSAGE) {
                removeCommand(selectCommand);

                if (selected.msg.isOwn && !selected.msg.isStatus) {
                    addCommand(editCommand);
                    addCommand(deleteCommand);
                } else {
                    removeCommand(editCommand);
                    removeCommand(deleteCommand);
                }
            } else {
                addCommand(selectCommand);
            }
        } else {
            removeCommand(selectCommand);
        }

        if (selectionMode && selected.shouldShowReplyOption()) {
            addCommand(replyCommand);
        } else {
            removeCommand(replyCommand);
        }
    }

    protected void paint(Graphics g) {
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;
        
        if (width != getWidth() || height != getHeight()) {
            width = getWidth();
            height = getHeight();
            update(true);
        }
        else if (requestedUpdate) {
            update(false);
            requestedUpdate = false;
        }

        if (items.size() > 0) {
            makeSelectedItemVisible();

            ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
            updateCommands(selected);
        }

        g.setFont(App.messageFont);
        g.setColor(backgroundColors[App.theme]);
        g.fillRect(0, 0, width, height);

        if (items.size() == 0) {
            g.setColor(timestampColors[App.theme]);
            g.drawString(
                "Nothing to see here", width/2, height/2 - fontHeight/2,
                Graphics.HCENTER | Graphics.TOP
            );
        } else {
            int y = -scroll;
            for (int i = items.size() - 1; i >= 0; i--) {
                ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
                
                if (y + item.height >= 0) {
                    item.draw(g, y, width, i == selectedItem && selectionMode);
                }
                y += item.height;
                if (y > height) break;
            }
        }

        // g.setColor(0x00ff0000);
        // g.drawString(
        //     "" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()), width, 0,
        //     Graphics.TOP | Graphics.RIGHT
        // );

        haveDrawn = true;
    }

    private void executeItemAction() {
        ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
        switch (selected.type) {
            case ChannelViewItem.NEWER_BUTTON: {
                page--;
                before = null;
                after = ((Message) App.messages.elementAt(0)).id;
                getMessages();
                break;
            }
            case ChannelViewItem.OLDER_BUTTON: {
                page++;
                after = null;
                before = ((Message) App.messages.elementAt(App.messages.size() - 1)).id;
                getMessages();
                break;
            }
        }
    }
    
    private void keyEvent(int keycode) {
        touchMode = false;
        int thisItemHeight = ((ChannelViewItem) items.elementAt(selectedItem)).height;
        int thisItemPos = getItemPosition(selectedItem);

        switch (getGameAction(keycode)) {
            case UP: {
                // No message selected -> enable selection mode (bottom-most will be selected)
                if (!selectionMode) {
                    selectionMode = true;
                }
                // Message is taller than screen -> scroll up by two lines
                else if (thisItemHeight > height && thisItemPos < 0) {
                    scroll -= fontHeight*2;
                }
                // Else go up by one message
                else {
                    int max = items.size() - 1;
                    if (selectedItem > max) selectedItem = max;
                    if (selectedItem == max) return;
                    selectedItem++;
                }
                break;
            }
            case DOWN: {
                // Message is taller than screen -> scroll down by two lines
                if (thisItemHeight > height && thisItemPos + thisItemHeight > height) {
                    scroll += fontHeight*2;
                }
                // Bottom-most message -> disable selection mode
                else if (selectedItem == 0) {
                    selectionMode = false;
                }
                // Else go down by one message
                else {
                    if (selectedItem < 0) selectedItem = 0;
                    if (selectedItem == 0) return;
                    selectedItem--;
                }
                break;
            }
            case FIRE: {
                executeItemAction();
                break;
            }
        }
        repaint();
    }
    protected void keyPressed(int a) { keyEvent(a); }
    protected void keyRepeated(int a) { keyEvent(a); }

    protected void pointerPressed(int x, int y) {
        touchMode = true;
        pressY = y;
    }

    protected void pointerDragged(int x, int y) {
        touchMode = true;
        scroll -= y - pressY;
        pressY = y;
        repaint();
    }

    protected void pointerReleased(int x, int y) {
        if (items == null) return;
        touchMode = true;

        for (int i = 0; i < items.size(); i++) {
            ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
            int itemPos = getItemPosition(i);
            if (y >= itemPos && y <= itemPos + item.height) {
                if (selectionMode && i == selectedItem) {
                    // If this item was already selected, execute its action if it's a button
                    executeItemAction();
                } else {
                    selectionMode = true;
                    selectedItem = i;
                }
                break;
            }
        }
        repaint();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            App.openChannelSelector(false);
        }
        else if (c == sendCommand) {
            App.disp.setCurrent(new MessageBox(null));
        }
        else if (c == refreshCommand) {
            App.openChannelView(true);
        }
        else if (c == selectCommand) {
            executeItemAction();
        }
        else {
            Message selected = ((ChannelViewItem) items.elementAt(selectedItem)).msg;

            if (c == replyCommand) {
                App.disp.setCurrent(new ReplyForm(selected));
            }
            else if (c == editCommand) {
                App.disp.setCurrent(new MessageBox(selected));
            }
            else if (c == deleteCommand) {
                HTTPThread h = new HTTPThread(HTTPThread.DELETE_MESSAGE);
                h.editMessage = selected;
                h.start();
            }
        }
    }
}