package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends Canvas implements CommandListener {
    State s;
    private Command backCommand;
    private Command selectCommand;
    private Command sendCommand;
    private Command refreshCommand;

    private Vector items;

    // Parameters for viewing old messages (message IDs)
    int page;
    String before;
    String after;

    boolean haveShown;
    boolean haveDrawn;
    boolean outdated;
    int scroll;
    int maxScroll;
    int pressY;

    boolean touchMode;
    boolean selectionMode;
    int selectedItem;

    int messageFontHeight;
    int authorFontHeight;
    int width, height;

    //                                            Dark        Light       Black
    public static final int[] backgroundColors = {0x00313338, 0x00FFFFFF, 0x00000000};
    public static final int[] darkBgColors =     {0x001e1f22, 0x00CCCCCC, 0x00000000};
    public static final int[] highlightColors =  {0x002b2d31, 0x00EEEEEE, 0x00181818};
    public static final int[] messageColors =    {0x00FFFFFF, 0x00111111, 0x00EEEEEE};
    public static final int[] authorColors =     {0x00FFFFFF, 0x00000000, 0x00FFFFFF};
    public static final int[] timestampColors =  {0x00AAAAAA, 0x00888888, 0x00999999};

    public ChannelView(State s) throws Exception {
        super();

        setCommandListener(this);
        this.s = s;

        backCommand = new Command("Back", Command.BACK, 0);
        selectCommand = new Command("Select", Command.OK, 1);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 2);
        refreshCommand = new Command("Refresh", Command.ITEM, 3);

        messageFontHeight = s.messageFont.getHeight();
        authorFontHeight = s.authorFont.getHeight();

        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);
    }

    protected void showNotify() {
        if (haveShown) return;
        haveShown = true;
        width = getWidth();
        height = getHeight();
        update(false);
        repaint();
    }

    public void update(boolean wasResized) {
        if (s.isDM) {
            if (s.selectedDmChannel.isGroup) {
                setTitle(s.selectedDmChannel.name);
            } else {
                setTitle("@" + s.selectedDmChannel.name);
            }
        } else {
            setTitle("#" + s.selectedChannel.name);
        }

        if (page > 0) setTitle(getTitle() + " (old)");
        items = new Vector();

        int oldMaxScroll = maxScroll;
        maxScroll = 0;

        if (s.messages.size() > 0 && page > 0) {
            ChannelViewItem newerItem = new ChannelViewItem(s, ChannelViewItem.NEWER_BUTTON);
            items.addElement(newerItem);
            maxScroll += newerItem.getHeight();
        }

        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            msg.contentLines = WordWrap.getStringArray(msg.content, getWidth(), s.messageFont);

            ChannelViewItem msgItem = new ChannelViewItem(s, msg);
            items.addElement(msgItem);
            maxScroll += msgItem.getHeight();
        }

        if (s.messages.size() > 0) {
            ChannelViewItem olderItem = new ChannelViewItem(s, ChannelViewItem.OLDER_BUTTON);
            items.addElement(olderItem);
            maxScroll += olderItem.getHeight();
        }

        maxScroll -= getHeight();

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
            selectionMode = true;
        }
    }

    public void getMessages() throws Exception {
        HTTPThread h = new HTTPThread(s, HTTPThread.FETCH_MESSAGES);
        h.fetchMsgsBefore = before;
        h.fetchMsgsAfter = after;
        h.start();
    }

    // Get the screen Y position that an item will be drawn at
    public int getItemPosition(int index) {
        int y = -scroll;
        for (int i = items.size() - 1; i >= (index + 1); i--) {
            ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
            y += item.getHeight();
        }
        return y;
    }

    // Ensure that the selected item is visible on screen
    public void makeSelectedItemVisible() {
        if (!selectionMode || touchMode) return;

        ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
        int itemPos = getItemPosition(selectedItem);
        int itemHeight = selected.getHeight();

        if (itemHeight > getHeight()) {
            // For items taller than the screen, make sure one screenful of it is visible:
            // Check if item is above the visible area
            if (itemPos + itemHeight < 0) {
                scroll -= getHeight();
            }
            // Check if below the visible area
            else if (itemPos > getHeight()) scroll += getHeight();
        } else {
            // For shorter items, make sure the entire item is visible:
            // Check if item is above the visible area
            if (itemPos < 0) {
                scroll += itemPos;
            }
            // Check if below the visible area
            else if (itemPos + itemHeight > getHeight()) {
                scroll += (itemPos + itemHeight) - getHeight();
            }
        }
    }

    protected void sizeChanged(int w, int h) {
        repaint();
    }

    protected void paint(Graphics g) {
        if (width != getWidth() || height != getHeight()) {
            width = getWidth();
            height = getHeight();
            update(true);
        }

        makeSelectedItemVisible();

        ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);

        if (selectionMode && selected.type != ChannelViewItem.MESSAGE) {
            addCommand(selectCommand);
        } else {
            removeCommand(selectCommand);
        }

        g.setFont(s.messageFont);
        g.setColor(backgroundColors[s.theme]);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (items.size() == 0) {
            g.setColor(timestampColors[s.theme]);
            g.drawString(
                "Nothing to see here", getWidth()/2, getHeight()/2 - messageFontHeight/2,
                Graphics.HCENTER | Graphics.TOP
            );
        } else {
            int y = -scroll;
            for (int i = items.size() - 1; i >= 0; i--) {
                ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
                int itemHeight = item.getHeight();
                
                if (y + itemHeight >= 0) {
                    // highlight selected item
                    if (selectionMode && i == selectedItem) {
                        g.setColor(highlightColors[s.theme]);
                        g.fillRect(0, y, getWidth(), itemHeight);
                    }
                    item.draw(g, y, getWidth());
                }
                y += itemHeight;
                if (y > getHeight()) break;
            }
        }

        int typingBannerY = 0;
        if (outdated) {
            g.setFont(s.messageFont);
            String[] lines = WordWrap.getStringArray("Refresh to read new messages", getWidth(), s.messageFont);
            g.setColor(0x00AA1122);
            g.fillRect(0, 0, getWidth(), messageFontHeight*lines.length + messageFontHeight/4);

            g.setColor(0x00FFFFFF);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(lines[i], getWidth()/2, i*messageFontHeight + messageFontHeight/8, Graphics.TOP|Graphics.HCENTER);
            }
            typingBannerY = messageFontHeight*lines.length + messageFontHeight/4;
        }

        if (s.typingUsers.size() > 0) {
            String typingStr;
            switch (s.typingUsers.size()) {
                case 1: typingStr = s.typingUsers.elementAt(0) + " is typing"; break;
                case 2: typingStr = s.typingUsers.elementAt(0) + ", " + s.typingUsers.elementAt(1) + " are typing"; break;
                case 3: typingStr = s.typingUsers.elementAt(0) + ", " + s.typingUsers.elementAt(1) + ", " + s.typingUsers.elementAt(2) + " are typing"; break;
                default: typingStr = s.typingUsers.size() + " people are typing"; break;
            }

            g.setFont(s.messageFont);
            String[] lines = WordWrap.getStringArray(typingStr, getWidth(), s.messageFont);
            g.setColor(darkBgColors[s.theme]);
            g.fillRect(0, typingBannerY, getWidth(), messageFontHeight*lines.length + messageFontHeight/4);

            g.setColor(authorColors[s.theme]);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], getWidth()/2, typingBannerY + i*messageFontHeight + messageFontHeight/8,
                    Graphics.TOP | Graphics.HCENTER
                );
            }
        }
        haveDrawn = true;
    }

    private void executeItemAction() {
        ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
        switch (selected.type) {
            case ChannelViewItem.NEWER_BUTTON: {
                page--;
                before = null;
                after = ((Message) s.messages.elementAt(0)).id;
                try {
                    getMessages();
                }
                catch (Exception e) {
                    s.error(e.toString());
                }
                break;
            }
            case ChannelViewItem.OLDER_BUTTON: {
                page++;
                after = null;
                before = ((Message) s.messages.elementAt(s.messages.size() - 1)).id;
                try {
                    getMessages();
                }
                catch (Exception e) {
                    s.error(e.toString());
                }
                break;
            }
        }
    }
    
    private void keyEvent(int keycode) {
        touchMode = false;
        int action = getGameAction(keycode);
        int thisItemHeight = ((ChannelViewItem) items.elementAt(selectedItem)).getHeight();
        int thisItemPos = getItemPosition(selectedItem);

        switch (action) {
            case Canvas.UP: {
                if (!selectionMode) {
                    selectionMode = true;
                    repaint();
                    return;
                }
    
                if (thisItemHeight > getHeight() && thisItemPos < 0) {
                    scroll -= messageFontHeight*2;
                } else {
                    int max = items.size() - 1;
                    if (selectedItem > max) selectedItem = max;
                    if (selectedItem == max) return;
                    selectedItem++;
                }
                break;
            }
            case Canvas.DOWN: {
                if (selectedItem == 0) {
                    selectionMode = false;
                    repaint();
                    return;
                }
                
                if (thisItemHeight > getHeight() && thisItemPos + thisItemHeight > getHeight()) {
                    scroll += messageFontHeight*2;
                } else {
                    if (selectedItem < 0) selectedItem = 0;
                    if (selectedItem == 0) return;
                    selectedItem--;
                }
                break;
            }
            case Canvas.FIRE: {
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
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;
        pressY = y;
        repaint();
    }

    protected void pointerReleased(int x, int y) {
        touchMode = true;

        for (int i = 0; i < items.size(); i++) {
            ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
            int itemPos = getItemPosition(i);
            if (y >= itemPos && y <= itemPos + item.getHeight()) {
                if (i == selectedItem) {
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
            if (s.isDM) s.openDMSelector(false);
            else s.openChannelSelector(false);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageBox(s));
        }
        if (c == refreshCommand) {
            s.openChannelView(true);
        }
        if (c == selectCommand) {
            executeItemAction();
        }
    }
}