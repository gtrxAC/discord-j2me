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
    private Command replyCommand;
    private Command uploadCommand;
    private Command copyCommand;
    private Command editCommand;
    private Command deleteCommand;
    private Command refreshCommand;
    private Command openUrlCommand;

    public Vector items;

    // Parameters for viewing old messages (message IDs)
    int page;
    String before;
    String after;

    boolean haveShown;
    boolean haveDrawn;
    boolean outdated;
    String bannerText;
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
    static final int[] backgroundColors = {0x00313338, 0x00FFFFFF, 0x00000000};
    static final int[] highlightColors2 = {0x002b2d31, 0x00EEEEEE, 0x00202020};
    static final int[] highlightColors =  {0x00232428, 0x00DDDDDD, 0x00303030};
    static final int[] darkBgColors =     {0x001e1f22, 0x00CCCCCC, 0x00404040};
    static final int[] messageColors =    {0x00FFFFFF, 0x00111111, 0x00EEEEEE};
    static final int[] authorColors =     {0x00FFFFFF, 0x00000000, 0x00FFFFFF};
    static final int[] timestampColors =  {0x00AAAAAA, 0x00888888, 0x00999999};

    public ChannelView(State s) throws Exception {
        super();

        setCommandListener(this);
        this.s = s;
        s.channelIsOpen = true;

        backCommand = new Command("Back", Command.BACK, 0);
        selectCommand = new Command("Select", Command.OK, 1);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 2);
        replyCommand = new Command("Reply", Command.ITEM, 3);
        uploadCommand = new Command("Upload", "Upload file", Command.ITEM, 4);
        copyCommand = new Command("Copy", "Copy content", Command.ITEM, 5);
        editCommand = new Command("Edit", Command.ITEM, 6);
        deleteCommand = new Command("Delete", Command.ITEM, 7);
        openUrlCommand = new Command("Open URL", Command.ITEM, 8);
        refreshCommand = new Command("Refresh", Command.ITEM, 9);

        messageFontHeight = s.messageFont.getHeight();
        authorFontHeight = s.authorFont.getHeight();

        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(uploadCommand);
        addCommand(refreshCommand);
    }

    protected void showNotify() {
        if (haveShown) return;
        haveShown = true;
        width = getWidth();
        height = getHeight();
        update(false, false);
        repaint();
    }

    public void update(boolean wasResized, boolean wasGateway) {
        if (!wasGateway) {
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
        }

        items = new Vector();

        if (s.messages.size() == 0) return;

        int oldMaxScroll = maxScroll;
        maxScroll = 0;

        if (page > 0) {
            ChannelViewItem newerItem = new ChannelViewItem(s, ChannelViewItem.NEWER_BUTTON);
            items.addElement(newerItem);
            maxScroll += newerItem.getHeight();
        }

        Message first = (Message) s.messages.elementAt(s.messages.size() - 1);
        first.showAuthor = true;

        Message above = first;
        String clusterStart = first.id;

        if (s.messages.size() > 1) {
            for (int i = s.messages.size() - 2; i >= 0; i--) {
                Message msg = (Message) s.messages.elementAt(i);
                msg.showAuthor = msg.shouldShowAuthor(above, clusterStart);

                if (msg.showAuthor) clusterStart = msg.id;
                above = msg;
            }
        }

        boolean useIcons = s.iconType != State.ICON_TYPE_NONE;
        int width = getWidth() - (useIcons ? messageFontHeight*2 : 0);
        int embedTextWidth = width - messageFontHeight/2 - messageFontHeight*2/3;

        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            boolean needUpdate = msg.needUpdate;

            if (msg.contentLines == null || wasResized || needUpdate) {
                msg.contentLines = Util.wordWrap(msg.content, width, s.messageFont);
                msg.needUpdate = false;
            }

            if (msg.attachments != null && msg.attachments.size() > 0) {
                ChannelViewItem attachItem = new ChannelViewItem(s, ChannelViewItem.ATTACHMENTS_BUTTON);
                attachItem.msg = msg;
                items.addElement(attachItem);
                maxScroll += attachItem.getHeight();
            }

            if (msg.embeds != null && msg.embeds.size() > 0) {
                for (int e = 0; e < msg.embeds.size(); e++) {
                    Embed emb = (Embed) msg.embeds.elementAt(e);

                    if ((wasResized || emb.titleLines == null || needUpdate) && emb.title != null) {
                        emb.titleLines = Util.wordWrap(emb.title, embedTextWidth, s.messageFont);
                        msg.needUpdate = false;
                    }
                    if ((wasResized || emb.descLines == null || needUpdate) && emb.description != null) {
                        emb.descLines = Util.wordWrap(emb.description, embedTextWidth, s.messageFont);
                        msg.needUpdate = false;
                    }
                }
            }

            if (msg.showAuthor || msg.contentLines.length != 0) {
                ChannelViewItem msgItem = new ChannelViewItem(s, ChannelViewItem.MESSAGE);
                msgItem.msg = msg;
                items.addElement(msgItem);
                maxScroll += msgItem.getHeight();
            }
        }

        if (s.messages.size() >= s.messageLoadCount) {
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
        }
        else if (wasGateway) {
            // If new message was received via gateway, keep the previously selected message selected.
            if (selectionMode) {
                selectedItem++;
                if (selectedItem >= items.size()) {
                    selectedItem = items.size() - 1;
                }
            }
            // If we were at the bottom of the message list, stay at the bottom
            if (scroll == oldMaxScroll) scroll = maxScroll;
        }
        else {
            // If user selected Show newer messages, go to the top of the
            // message list, so it's more intuitive to scroll through
            scroll = (after != null) ? 0 : maxScroll;
            selectedItem = (after != null) ? (items.size() - 1) : 0;
            selectionMode = (before != null || after != null);
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

    private ChannelViewItem lastSelected;

    private void updateCommands(ChannelViewItem selected) {
        if (selectionMode && (selected.msg == null || !selected.msg.isStatus)) {
            if (selected.type == ChannelViewItem.MESSAGE) {
                removeCommand(selectCommand);

                if ("(no content)".equals(selected.msg.content) || selected.msg.isStatus) {
                    removeCommand(copyCommand);
                } else {
                    addCommand(copyCommand);
                }

                if (Util.indexOfAny(selected.msg.content, URLList.urlStarts, 0) != -1) {
                    addCommand(openUrlCommand);
                } else {
                    removeCommand(openUrlCommand);
                }

                if (s.myUserId.equals(selected.msg.author.id) && !selected.msg.isStatus) {
                    addCommand(editCommand);
                    addCommand(deleteCommand);
                } else {
                    removeCommand(editCommand);
                    removeCommand(deleteCommand);
                }
            } else {
                removeCommand(openUrlCommand);
                removeCommand(copyCommand);
                addCommand(selectCommand);
            }
        } else {
            removeCommand(openUrlCommand);
            removeCommand(copyCommand);
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
            update(true, false);
        }

        // BlackBerry fix
        g.setClip(0, 0, width, height);

        if (items.size() > 0) {
            makeSelectedItemVisible();

            ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
            updateCommands(selected);
        }

        g.setFont(s.messageFont);
        g.setColor(backgroundColors[s.theme]);
        g.fillRect(0, 0, width, height);

        if (items.size() == 0) {
            g.setColor(timestampColors[s.theme]);
            g.drawString(
                "Nothing to see here", width/2, height/2 - messageFontHeight/2,
                Graphics.HCENTER | Graphics.TOP
            );
        } else {
            int y = -scroll;
            for (int i = items.size() - 1; i >= 0; i--) {
                ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
                int itemHeight = item.getHeight();
                
                if (y + itemHeight >= 0) {
                    item.draw(g, y, width, i == selectedItem && selectionMode);
                }
                y += itemHeight;
                if (y > height) break;
            }
        }

        int bannerY = 0;

        if (bannerText != null) {
            g.setFont(s.messageFont);
            String[] lines = Util.wordWrap(bannerText, width, s.messageFont);
            g.setColor(0x005865f2);
            g.fillRect(0, 0, width, messageFontHeight*lines.length + messageFontHeight/4);

            g.setColor(0x00FFFFFF);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], width/2, i*messageFontHeight + messageFontHeight/8,
                    Graphics.TOP | Graphics.HCENTER
                );
            }
            bannerY += messageFontHeight*lines.length + messageFontHeight/4;
        }

        if (outdated) {
            g.setFont(s.messageFont);
            String[] lines = Util.wordWrap("Refresh to read new messages", width, s.messageFont);
            g.setColor(0x00AA1122);
            g.fillRect(0, bannerY, width, messageFontHeight*lines.length + messageFontHeight/4);

            g.setColor(0x00FFFFFF);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], width/2, bannerY + i*messageFontHeight + messageFontHeight/8,
                    Graphics.TOP | Graphics.HCENTER
                );
            }
            bannerY += messageFontHeight*lines.length + messageFontHeight/4;
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
            String[] lines = Util.wordWrap(typingStr, width, s.messageFont);
            g.setColor(darkBgColors[s.theme]);
            g.fillRect(0, bannerY, width, messageFontHeight*lines.length + messageFontHeight/4);

            g.setColor(authorColors[s.theme]);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], width/2, bannerY + i*messageFontHeight + messageFontHeight/8,
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
            case ChannelViewItem.ATTACHMENTS_BUTTON: {
                s.openAttachmentView(false, selected.msg);
                break;
            }
        }
    }
    
    private void keyEvent(int keycode) {
        touchMode = false;
        int thisItemHeight = ((ChannelViewItem) items.elementAt(selectedItem)).getHeight();
        int thisItemPos = getItemPosition(selectedItem);

        // user bound key
        if (keycode == s.sendHotkey) {
            s.dontShowLoadScreen = true;
            s.disp.setCurrent(new MessageBox(s));
        }
        else if (keycode == s.replyHotkey) {
            if (!selectionMode || items.size() == 0) return;
            ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);
            if (!item.shouldShowReplyOption()) return;
            s.disp.setCurrent(new ReplyForm(s, item.msg));
        }
        else if (keycode == s.copyHotkey) {
            if (!selectionMode || items.size() == 0) return;
            ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);
            if (item.type != ChannelViewItem.MESSAGE) return;
            s.disp.setCurrent(new MessageCopyBox(s, item.msg.content));
        }
        else if (keycode == s.refreshHotkey) {
            commandAction(refreshCommand, this);
        }
        else if (keycode == s.backHotkey) {
            commandAction(backCommand, this);
        }
        // game action key
        else {
            int action = getGameAction(keycode);
            switch (action) {
                case Canvas.UP: {
                    // No message selected -> enable selection mode (bottom-most will be selected)
                    if (!selectionMode) {
                        selectionMode = true;
                    }
                    // Message is taller than screen -> scroll up by two lines
                    else if (thisItemHeight > getHeight() && thisItemPos < 0) {
                        scroll -= messageFontHeight*2;
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
                case Canvas.DOWN: {
                    // Message is taller than screen -> scroll down by two lines
                    if (thisItemHeight > getHeight() && thisItemPos + thisItemHeight > getHeight()) {
                        scroll += messageFontHeight*2;
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
                case Canvas.FIRE: {
                    executeItemAction();
                    break;
                }
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
            s.unreads.save();
            s.channelIsOpen = false;
            if (s.isDM) s.openDMSelector(false);
            else s.openChannelSelector(false);
        }
        else if (c == sendCommand) {
            s.disp.setCurrent(new MessageBox(s));
        }
        else if (c == refreshCommand) {
            s.dontShowLoadScreen = true;
            s.openChannelView(true);
        }
        else if (c == selectCommand) {
            executeItemAction();
        }
        else if (c == replyCommand) {
            ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);
            s.disp.setCurrent(new ReplyForm(s, item.msg));
        }
        else if (c == uploadCommand) {
            try {
                if (!s.isLiteProxy) {
                    s.error("This proxy does not support file uploading");
                }
                else if (s.nativeFilePicker) {
                    if (System.getProperty("microedition.io.file.FileConnection.version") != null) {
                        s.disp.setCurrent(new AttachmentPicker(s));
                    } else {
                        s.error("FileConnection not supported");
                    }
                }
                else {
                    String id = s.isDM ? s.selectedDmChannel.id : s.selectedChannel.id;
                    String url = s.http.api + "/upload?channel=" + id + "&token=" + s.http.token;
                    s.platformRequest(url);
                }
            }
            catch (Exception e) {
                s.error(e.toString());
            }
        }
        else {
            ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);

            if (c == copyCommand) {
                s.disp.setCurrent(new MessageCopyBox(s, item.msg.content));
            }
            else if (c == openUrlCommand) {
                s.disp.setCurrent(new URLList(s, item.msg.content));
            }
            else if (c == deleteCommand) {
                if (!s.isLiteProxy) {
                    s.error("This proxy does not support deleting messages");
                } else {
                    s.disp.setCurrent(new DeleteConfirmAlert(s, item.msg));
                }
            }
            else if (c == editCommand) {
                if (!s.isLiteProxy) {
                    s.error("This proxy does not support editing messages");
                } else {
                    s.disp.setCurrent(new MessageEditBox(s, item.msg));
                }
            }
        }
    }
}