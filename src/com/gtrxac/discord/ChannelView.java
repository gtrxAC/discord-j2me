package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends Canvas implements CommandListener {
    private Command selectCommand;
    private Command replyCommand;
    private Command editCommand;
    private Command deleteCommand;

    public Vector items;

    // Parameters for viewing old messages (message IDs)
    int page;
    String before;
    String after;

    boolean haveDrawn;
    int scroll;
    int maxScroll;
    int pressY;

    boolean touchMode;
    boolean selectionMode;
    int selectedItem;

    int width, height;

    boolean requestedUpdate;

    /**
     * Create new channel view.
     * @param dummy If true, this instance is not used as an actual channel view, rather as a basic Canvas instance that is only used for getting the width/height of the display
     */
    public ChannelView(boolean dummy) {
        super();
        if (dummy) return;
        setCommandListener(this);

        // commands that get added/removed based on selected item
        selectCommand = new Command("Select", Command.OK, 1);
        replyCommand = new Command("Reply", Command.ITEM, 3);
        editCommand = new Command("Edit", Command.ITEM, 4);
        deleteCommand = new Command("Delete", Command.ITEM, 5);

        // commands that are always shown (dont need to be saved in a field)
        addCommand(new Command("Back", Command.BACK, 0));
        addCommand(new Command("Send", Command.SCREEN, 2));
        addCommand(new Command("Refresh", Command.SCREEN, 6));
    }

    public void requestUpdate() {
        requestedUpdate = true;
    }

    /**
     * Calculate message layouts and contents to be rendered on the channel view.
     * @param wasResized If true, this update occurred because the screen was resized.
     *   If false, this update occurred for another reason, for example loading another page of older messages.
     */
    private void update(boolean wasResized) {
        items = new Vector(App.messageLoadCount + 2);

        int messageCount = App.messages.size();
        if (messageCount == 0) return;

        int oldMaxScroll = maxScroll;
        maxScroll = -height;

        if (page > 0) {
            items.addElement(ChannelViewItem.newerMessagesButton);
            maxScroll += ChannelViewItem.newerMessagesButton.height;
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

        int availableWidth = width - Message.screenMargin;

        for (int i = 0; i < messageCount; i++) {
            Message msg = (Message) App.messages.elementAt(i);

            if (msg.contentLines == null || wasResized || msg.needUpdate) {
                msg.contentLines = App.wordWrap(msg.content, availableWidth, App.messageFont);
                msg.needUpdate = false;
            }
            items.addElement(msg);
            maxScroll += msg.calculateHeight();
        }

        if (messageCount >= App.messageLoadCount) {
            items.addElement(ChannelViewItem.olderMessagesButton);
            maxScroll += ChannelViewItem.olderMessagesButton.height;
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
    private void makeSelectedItemVisible(ChannelViewItem selected) {
        if (!selectionMode || touchMode) return;

        int itemPos = getItemPosition(selectedItem);
        int itemHeight = selected.height;

        if (selected instanceof Message && ((Message) selected).showAuthor) {
            itemPos += Message.groupSpacing;
            itemHeight -= Message.groupSpacing;
        }

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
        boolean isMessage = (selected instanceof Message);

        if (selectionMode && (!isMessage || !((Message) selected).isStatus)) {
            if (isMessage) {
                removeCommand(selectCommand);

                if (((Message) selected).isOwn) {
                    addCommand(editCommand);
                    addCommand(deleteCommand);
                } else {
                    removeCommand(editCommand);
                    removeCommand(deleteCommand);
                }
            } else {
                addCommand(selectCommand);
                removeCommand(editCommand);
                removeCommand(deleteCommand);
            }
        } else {
            removeCommand(selectCommand);
            removeCommand(editCommand);
            removeCommand(deleteCommand);
        }

        if (selectionMode && isMessage) {
            addCommand(replyCommand);
        } else {
            removeCommand(replyCommand);
        }
    }

    protected void paint(Graphics g) {
        if (width != getWidth() || height != getHeight()) {
            width = getWidth();
            height = getHeight();
            update(true);
        }
        else if (requestedUpdate) {
            update(false);
            requestedUpdate = false;
        }

        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;

        if (items.size() > 0) {
            ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
            makeSelectedItemVisible(selected);
            updateCommands(selected);
        }

        g.setFont(App.messageFont);
        g.setColor(ChannelViewItem.backgroundColor);
        g.fillRect(0, 0, width, height);

        if (items.size() == 0) {
            g.setColor(ChannelViewItem.timestampColor);
            g.drawString(
                "Nothing to see here", width/2, height/2 - ChannelViewItem.fontHeight/2,
                Graphics.HCENTER | Graphics.TOP
            );
        } else {
            int y = -scroll;
            for (int i = items.size() - 1; i >= 0; i--) {
                ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
                
                if (y + item.height > 0) {
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

        if (selected == ChannelViewItem.newerMessagesButton) {
            page--;
            before = null;
            after = ((Message) App.messages.elementAt(0)).id;
            getMessages();
        }
        else if (selected == ChannelViewItem.olderMessagesButton) {
            page++;
            after = null;
            before = ((Message) App.messages.elementAt(App.messages.size() - 1)).id;
            getMessages();
        }
    }
    
    private void keyEvent(int keycode) {
        touchMode = false;
        int thisItemHeight = ((ChannelViewItem) items.elementAt(selectedItem)).height;
        int thisItemPos = getItemPosition(selectedItem);

        if (keycode == KEY_NUM2) {
            // jump to top-most item
            selectionMode = true;
            selectedItem = items.size() - 1;
        }
        else if (keycode == KEY_NUM8) {
            // jump to bottom-most item
            selectionMode = true;
            selectedItem = 0;
        }
        else switch (getGameAction(keycode)) {
            case UP: {
                // No message selected -> enable selection mode (bottom-most will be selected)
                if (!selectionMode) {
                    selectionMode = true;
                }
                // Message is taller than screen -> scroll up by two lines
                else if (thisItemHeight > height && thisItemPos < 0) {
                    scroll -= ChannelViewItem.fontHeight*2;
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
                    scroll += ChannelViewItem.fontHeight*2;
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
        switch (c.getPriority()) {
            case 0: {  // back
                App.openChannelSelector(false);
                break;
            }
            
            case 1: {  // select
                executeItemAction();
                break;
            }
            
            case 2: {  // send
                App.disp.setCurrent(new MessageBox(null));
                break;
            }
            
            case 3: {  // reply
                Message selected = (Message) items.elementAt(selectedItem);
                App.disp.setCurrent(new ReplyForm(selected));
                break;
            }
            
            case 4: {  // edit
                Message selected = (Message) items.elementAt(selectedItem);
                App.disp.setCurrent(new MessageBox(selected));
                break;
            }
            
            case 5: {  // delete
                Message selected = (Message) items.elementAt(selectedItem);
                HTTPThread h = new HTTPThread(HTTPThread.DELETE_MESSAGE);
                h.editMessage = selected;
                h.start();
                break;
            }
            
            case 6: {  // refresh
                App.openChannelView(true);
                break;
            } 
        }
    }
}