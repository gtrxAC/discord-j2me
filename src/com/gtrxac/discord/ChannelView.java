package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends KineticScrollingCanvas implements CommandListener, Strings {
    private Command backCommand;
    private Command selectCommand;
    private Command sendCommand;
    private Command replyCommand;
    private Command uploadCommand;
    private Command replyUploadCommand;
    private Command copyCommand;
    private Command editCommand;
    private Command deleteCommand;
    private Command refreshCommand;
    private Command openUrlCommand;
    private Command fullScreenCommand;

    public Vector items;

    // Parameters for viewing old messages (message IDs)
    int page;
    String before;
    String after;

    boolean fullscreen;
    boolean haveShown;
    boolean haveDrawn;
    boolean outdated;
    String bannerText;
    int maxScroll;

    boolean touchMode;
    boolean selectionMode;
    int selectedItem;

    int fontHeight;
    int authorFontHeight;
    int width, height;

    boolean requestedUpdate;
    boolean reqUpdateGateway;
    boolean reqUpdateGatewayNewMsg;

    //                                     Dark      Light     Black
    static final int[] backgroundColors = {0x313338, 0xFFFFFF, 0x000000};
    static final int[] highlightColors2 = {0x2b2d31, 0xEEEEEE, 0x202020};
    static final int[] highlightColors =  {0x232428, 0xDDDDDD, 0x303030};
    static final int[] darkBgColors =     {0x1e1f22, 0xCCCCCC, 0x404040};
    static final int[] refMessageColors = {0xDDDDDD, 0x333333, 0xCCCCCC};
    static final int[] messageColors =    {0xFFFFFF, 0x111111, 0xEEEEEE};
    static final int[] authorColors =     {0xFFFFFF, 0x000000, 0xFFFFFF};
    static final int[] timestampColors =  {0xAAAAAA, 0x666666, 0x999999};

    public ChannelView() throws Exception {
        super();
        setCommandListener(this);
        App.channelIsOpen = true;
        updateTitle();

        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        selectCommand = Locale.createCommand(SELECT, Command.OK, 1);
        sendCommand = Locale.createCommand(SEND_MESSAGE, Command.ITEM, 2);
        replyCommand = Locale.createCommand(REPLY, Command.ITEM, 3);
        uploadCommand = Locale.createCommand(UPLOAD_FILE, Command.ITEM, 4);
        replyUploadCommand = Locale.createCommand(REPLY_FILE, Command.ITEM, 5);
        copyCommand = Locale.createCommand(COPY_CONTENT, Command.ITEM, 6);
        editCommand = Locale.createCommand(EDIT, Command.ITEM, 7);
        deleteCommand = Locale.createCommand(DELETE, Command.ITEM, 8);
        openUrlCommand = Locale.createCommand(OPEN_URL, Command.ITEM, 9);
        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 11);

        fontHeight = App.messageFont.getHeight();
        authorFontHeight = App.authorFont.getHeight();
        scrollUnit = fontHeight;

        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(uploadCommand);
        addCommand(refreshCommand);

        // ifdef MIDP2_GENERIC
        if (!Util.isKemulator)
        // endif
        {
            setFullScreenMode(Settings.fullscreenDefault);
            fullscreen = Settings.fullscreenDefault;

            fullScreenCommand = Locale.createCommand(TOGGLE_FULLSCREEN, Command.ITEM, 10);
            addCommand(fullScreenCommand);
        }

        // ifdef J2ME_LOADER
        commands = new Vector();
        // endif
    }

    protected void showNotify() {
        if (haveShown) return;
        haveShown = true;
        width = getWidth();
        height = getHeight();
        requestUpdate(false, false);
        repaint();
    }

    public void requestUpdate(boolean wasGateway, boolean wasGatewayNewMsg) {
        requestedUpdate = true;
        reqUpdateGateway = wasGateway;
        reqUpdateGatewayNewMsg = wasGatewayNewMsg;
    }

    public void updateTitle() {
        StringBuffer resultBuf = new StringBuffer();
        if (App.isDM) {
            if (!App.selectedDmChannel.isGroup) {
                resultBuf.append("@");
            } 
            resultBuf.append(App.selectedDmChannel.name);
        } else {
            if (!App.selectedChannel.isThread) resultBuf.append("#");
            resultBuf.append(App.selectedChannel.name);
        }
        if (page > 0) resultBuf.append(Locale.get(CHANNEL_VIEW_TITLE_OLD));

        setTitle(resultBuf.toString());
    }

    private void update(boolean wasResized, boolean wasGateway, boolean wasGatewayNewMsg) {
        items = new Vector();

        if (App.messages.size() == 0) return;

        int oldMaxScroll = maxScroll;
        maxScroll = 0;

        if (page > 0) {
            ChannelViewItem newerItem = new ChannelViewItem(ChannelViewItem.NEWER_BUTTON);
            items.addElement(newerItem);
            maxScroll += newerItem.getHeight();
        }

        Message first = (Message) App.messages.elementAt(App.messages.size() - 1);
        first.showAuthor = true;

        Message above = first;
        String clusterStart = first.id;
        int unreadIndicatorPos = -1;
        int unreadIndicatorPosFinal = -1;

        boolean hasMoreUnreads;
        long lastUnreadTime;
        try {
            lastUnreadTime = Long.parseLong(UnreadManager.lastUnreadTime);
            long firstTime = Long.parseLong(first.id) >> 22;

            // does channel view have more unread messages than what can be shown on one page?
            // i.e. is the top-most message unread?
            hasMoreUnreads = (lastUnreadTime < firstTime);
        }
        catch (Exception e) {
            // channel has not been opened before so there is no last unread time
            lastUnreadTime = 0;
            hasMoreUnreads = false;
        }

        if (App.messages.size() > 1) {
            for (int i = App.messages.size() - 2; i >= 0; i--) {
                Message msg = (Message) App.messages.elementAt(i);

                // Check if the red "NEW" unread indicator should be placed above this message
                // Don't show unread indicator for messages that come from gateway
                if (!hasMoreUnreads && !wasGateway && unreadIndicatorPos == -1) {
                    long aboveTime = Long.parseLong(above.id) >> 22;
                    long msgTime = Long.parseLong(msg.id) >> 22;

                    if (lastUnreadTime >= aboveTime && lastUnreadTime < msgTime) {
                        unreadIndicatorPos = i;
                    }
                }

                // Recalculate "should show author" value for message
                msg.showAuthor = msg.shouldShowAuthor(above, clusterStart);

                if (msg.showAuthor) clusterStart = msg.id;
                above = msg;
            }
        }

        boolean useIcons = Settings.pfpType != Settings.PFP_TYPE_NONE;
        int iconAreaWidth = (useIcons ? fontHeight*2 : fontHeight/5);
        int contentWidth = width - iconAreaWidth;
        int embedTextX = iconAreaWidth + fontHeight/3;
        int embedTextWidth = contentWidth - fontHeight/2 - fontHeight*2/3;

        for (int i = 0; i < App.messages.size(); i++) {
            Message msg = (Message) App.messages.elementAt(i);
            boolean needUpdate = msg.needUpdate;

            // ifdef OVER_100KB
            if (msg.contentFormatted == null || wasResized || needUpdate) {
                msg.contentFormatted = new FormattedString(msg.content, App.messageFont, contentWidth, iconAreaWidth, false, msg.isEdited, msg.isForwarded);
                msg.needUpdate = false;
            }
            // else
            if (msg.contentLines == null || wasResized || needUpdate) {
                msg.contentLines = Util.wordWrap(msg.content, contentWidth, App.messageFont);
                msg.needUpdate = false;
            }
            // endif

            if (msg.attachments != null && msg.attachments.size() > 0) {
                ChannelViewItem attachItem = new ChannelViewItem(ChannelViewItem.ATTACHMENTS_BUTTON);
                attachItem.msg = msg;
                items.addElement(attachItem);
                maxScroll += attachItem.getHeight();
            }

            if (msg.embeds != null && msg.embeds.size() > 0) {
                for (int e = 0; e < msg.embeds.size(); e++) {
                    Embed emb = (Embed) msg.embeds.elementAt(e);

                    // ifdef OVER_100KB
                    if ((wasResized || emb.titleFormatted == null || needUpdate) && emb.title != null) {
                        emb.titleFormatted = new FormattedString(emb.title, App.titleFont, embedTextWidth, embedTextX, false, false, false);
                        msg.needUpdate = false;
                    }
                    if ((wasResized || emb.descFormatted == null || needUpdate) && emb.description != null) {
                        emb.descFormatted = new FormattedString(emb.description, App.messageFont, embedTextWidth, embedTextX, false, false, false);
                        msg.needUpdate = false;
                    }
                    // else
                    if ((wasResized || emb.titleLines == null || needUpdate) && emb.title != null) {
                        emb.titleLines = Util.wordWrap(emb.title, embedTextWidth, App.titleFont);
                        msg.needUpdate = false;
                    }
                    if ((wasResized || emb.descLines == null || needUpdate) && emb.description != null) {
                        emb.descLines = Util.wordWrap(emb.description, embedTextWidth, App.messageFont);
                        msg.needUpdate = false;
                    }
                    // endif
                }
            }

            if (
                msg.showAuthor
                // ifdef OVER_100KB
                || msg.contentFormatted.height != 0
                // else
                || msg.contentLines.length != 0
                // endif
            ) {
                ChannelViewItem msgItem = new ChannelViewItem(ChannelViewItem.MESSAGE);
                msgItem.msg = msg;
                items.addElement(msgItem);
                maxScroll += msgItem.getHeight();
            }

            // If this message is the last one read by the user, show the "NEW" indicator above it
            if (i == unreadIndicatorPos) {
                ChannelViewItem unreadItem = new ChannelViewItem(ChannelViewItem.UNREAD_INDICATOR);
                items.addElement(unreadItem);
                unreadIndicatorPosFinal = items.size();
                maxScroll += unreadItem.getHeight();
            }
        }

        if (App.messages.size() >= Settings.messageLoadCount) {
            ChannelViewItem olderItem = new ChannelViewItem(ChannelViewItem.OLDER_BUTTON);
            items.addElement(olderItem);
            maxScroll += olderItem.getHeight();
        }

        // Place unread indicator above the "older messages" button if there are more unreads than what can be shown on one page
        if (hasMoreUnreads) {
            ChannelViewItem unreadItem = new ChannelViewItem(ChannelViewItem.UNREAD_INDICATOR);
            items.addElement(unreadItem);
            maxScroll += unreadItem.getHeight();
        }

        maxScroll -= height;

        if (haveDrawn && wasResized) {
            // If this channel view has been previously drawn and was just resized
            // (e.g. screen rotate), keep the previous relative scroll value
            int scrollPercent = (scroll*100)/oldMaxScroll;
            scroll = (scrollPercent*maxScroll)/100;
        }
        else if (wasGateway) {
            // If update request was received via gateway, keep the previously selected message selected.
            // If the request was because of a new message, move the selection one position up, because the previously selected message is now one position above.
            // If the request was because of a message edit/deletion, the selection stays at the same position.
            if (wasGatewayNewMsg && selectionMode) {
                selectedItem++;
                if (selectedItem >= items.size()) {
                    selectedItem = items.size() - 1;
                }
            }
            // If we were at the bottom of the message list, stay at the bottom
            if (scroll == oldMaxScroll) scroll = maxScroll;
        }
        else if (before != null) {
            // User selected Show older messages - go to bottom of message list
            scroll = maxScroll;
            selectedItem = 0;
            selectionMode = true;
        }
        else if (after != null || hasMoreUnreads) {
            // If user selected Show newer messages, or there are more unreads, go to the top of the
            // message list, so it's more intuitive to scroll through
            scroll = 0;
            selectedItem = items.size() - 1;
            selectionMode = true;
        }
        // Channel view was updated for the first time - check if there is an unread message indicator, and if so, scroll to it
        else if (unreadIndicatorPosFinal != -1) {
            // Note: scroll is set to maxScroll (bottom of screen).
            // If unread indicator is offscreen, it will be shown because makeSelectedItemVisible is called by paint.
            scroll = maxScroll;
            selectedItem = unreadIndicatorPosFinal - 1;
            selectionMode = true;
        }
        else {
            // Channel was opened normally (no page change, no unread indicator) - go to bottom
            scroll = maxScroll;
            selectedItem = 0;
            selectionMode = false;
        }
    }

    public void getMessages() throws Exception {
        HTTPThread h = new HTTPThread(HTTPThread.FETCH_MESSAGES);
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
    private void makeSelectedItemVisible() {
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

    // ifdef J2ME_LOADER
    Vector commands;
    // endif

    private void _removeCommand(Command c) {
        // ifdef J2ME_LOADER
        if (commands.contains(c)) {
            commands.removeElement(c);
            Util.sleep(20);
        // endif
            removeCommand(c);
        // ifdef J2ME_LOADER
        }
        // endif
    }

    private void _addCommand(Command c) {
        // ifdef J2ME_LOADER
        if (!commands.contains(c)) {
            commands.addElement(c);
            Util.sleep(20);
        // endif
            addCommand(c);
        // ifdef J2ME_LOADER
        }
        // endif
    }

    private void updateCommands(ChannelViewItem selected) {
        if (selectionMode && (selected.msg == null || !selected.msg.isStatus)) {
            if (selected.type == ChannelViewItem.MESSAGE) {
                _removeCommand(selectCommand);

                if (selected.msg.isStatus) {
                    _removeCommand(copyCommand);
                } else {
                    _addCommand(copyCommand);
                }

                if (Util.indexOfAny(selected.msg.content, URLList.urlStarts, 0) != -1) {
                    _addCommand(openUrlCommand);
                } else {
                    _removeCommand(openUrlCommand);
                }

                if (App.myUserId.equals(selected.msg.author.id) && !selected.msg.isStatus) {
                    _addCommand(editCommand);
                    _addCommand(deleteCommand);
                } else {
                    _removeCommand(editCommand);
                    _removeCommand(deleteCommand);
                }
            } else {
                _removeCommand(openUrlCommand);
                _removeCommand(copyCommand);
                _addCommand(selectCommand);
            }
        } else {
            _removeCommand(openUrlCommand);
            _removeCommand(copyCommand);
            _removeCommand(selectCommand);
        }

        if (selectionMode && selected.shouldShowReplyOption()) {
            _addCommand(replyCommand);
            _addCommand(replyUploadCommand);
        } else {
            _removeCommand(replyCommand);
            _removeCommand(replyUploadCommand);
        }
    }

    // Also used by old channel view
    public static String getTypingString() {
        switch (App.typingUsers.size()) {
            case 1:
                return
                    App.typingUsers.elementAt(0) +
                    Locale.get(TYPING_ONE); 

            case 2:
                return
                    App.typingUsers.elementAt(0) +
                    Locale.get(COMMA) +
                    App.typingUsers.elementAt(1) +
                    Locale.get(TYPING_MANY);

            case 3:
                return
                    App.typingUsers.elementAt(0) +
                    Locale.get(COMMA) +
                    App.typingUsers.elementAt(1) +
                    Locale.get(COMMA) +
                    App.typingUsers.elementAt(2) +
                    Locale.get(TYPING_MANY);

            default:
                return
                    App.typingUsers.size() +
                    Locale.get(TYPING_SEVERAL);
        }
    }

    protected int getMinScroll() {
        return 0;
    }
    protected int getMaxScroll() {
        return maxScroll;
    }

    protected void paint(Graphics g) {
        checkScrollInRange();

        if (width != getWidth() || height != getHeight()) {
            width = getWidth();
            height = getHeight();
            update(true, false, false);
        }
        else if (requestedUpdate) {
            update(false, reqUpdateGateway, reqUpdateGatewayNewMsg);
            requestedUpdate = false;
        }

        // BlackBerry fix
        // ifdef BLACKBERRY
        g.setClip(0, 0, width, height);
        // endif

        if (items.size() > 0) {
            makeSelectedItemVisible();

            ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
            updateCommands(selected);
        }

        g.setFont(App.messageFont);
        g.setColor(backgroundColors[Settings.theme]);
        g.fillRect(0, 0, width, height);

        if (items.size() == 0) {
            g.setColor(timestampColors[Settings.theme]);
            g.drawString(
                Locale.get(CHANNEL_VIEW_EMPTY), width/2, height/2 - fontHeight/2,
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
            g.setFont(App.messageFont);
            String[] lines = Util.wordWrap(bannerText, width, App.messageFont);
            g.setColor(0x005865f2);
            g.fillRect(0, 0, width, fontHeight*lines.length + fontHeight/4);

            g.setColor(0x00FFFFFF);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], width/2, i*fontHeight + fontHeight/8,
                    Graphics.TOP | Graphics.HCENTER
                );
            }
            bannerY += fontHeight*lines.length + fontHeight/4;
        }

        if (outdated) {
            g.setFont(App.messageFont);
            String[] lines = Util.wordWrap(Locale.get(CHANNEL_VIEW_OUTDATED), width, App.messageFont);
            g.setColor(0x00AA1122);
            g.fillRect(0, bannerY, width, fontHeight*lines.length + fontHeight/4);

            g.setColor(0x00FFFFFF);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], width/2, bannerY + i*fontHeight + fontHeight/8,
                    Graphics.TOP | Graphics.HCENTER
                );
            }
            bannerY += fontHeight*lines.length + fontHeight/4;
        }

        if (App.typingUsers.size() > 0) {
            String typingStr = getTypingString();

            g.setFont(App.messageFont);
            String[] lines = Util.wordWrap(typingStr, width, App.messageFont);
            g.setColor(darkBgColors[Settings.theme]);
            g.fillRect(0, bannerY, width, fontHeight*lines.length + fontHeight/4);

            g.setColor(authorColors[Settings.theme]);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], width/2, bannerY + i*fontHeight + fontHeight/8,
                    Graphics.TOP | Graphics.HCENTER
                );
            }
        }

        // g.setColor(0x00ff0000);
        // g.drawString(
        //     "" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()), width, 0,
        //     Graphics.TOP | Graphics.RIGHT
        // );

        drawScrollbar(g);
        haveDrawn = true;
    }

    private void executeItemAction() {
        ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
        switch (selected.type) {
            case ChannelViewItem.NEWER_BUTTON: {
                page--;
                before = null;
                after = ((Message) App.messages.elementAt(0)).id;
                try {
                    getMessages();
                }
                catch (Exception e) {
                    App.error(e);
                }
                break;
            }
            case ChannelViewItem.OLDER_BUTTON: {
                page++;
                after = null;
                before = ((Message) App.messages.elementAt(App.messages.size() - 1)).id;
                try {
                    getMessages();
                }
                catch (Exception e) {
                    App.error(e);
                }
                break;
            }
            case ChannelViewItem.ATTACHMENTS_BUTTON: {
                App.openAttachmentView(false, selected.msg);
                break;
            }
        }
    }

    private void sendHotkeyAction() {
        App.dontShowLoadScreen = true;
        App.disp.setCurrent(new MessageBox());
    }

    private void replyHotkeyAction() {
        if (!selectionMode || items.size() == 0) return;
        ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);
        if (!item.shouldShowReplyOption()) return;
        App.disp.setCurrent(new ReplyForm(item.msg));
    }

    private void copyHotkeyAction() {
        if (!selectionMode || items.size() == 0) return;
        ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);
        if (item.type != ChannelViewItem.MESSAGE) return;
        App.disp.setCurrent(new MessageCopyBox(item.msg.content));
    }
    
    private void keyEvent(int keycode) {
        touchMode = false;
        int thisItemHeight = ((ChannelViewItem) items.elementAt(selectedItem)).getHeight();
        int thisItemPos = getItemPosition(selectedItem);

        int action = getGameAction(keycode);

        switch (action) {
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
            // not up/down/select - check if it's a hotkey
            default: {
                if (Settings.defaultHotkeys) {
                    // default hotkey (j2me game actions A/B/C/D)
                    switch (action) {
                        case GAME_A: sendHotkeyAction(); break;
                        case GAME_B: replyHotkeyAction(); break;
                        case GAME_C: copyHotkeyAction(); break;
                        case GAME_D: commandAction(refreshCommand, this); break;
                    }
                } else {
                    // user bound key (when 'default hotkeys' option disabled)
                    if (keycode == Settings.sendHotkey) {
                        sendHotkeyAction();
                    }
                    else if (keycode == Settings.replyHotkey) {
                        replyHotkeyAction();
                    }
                    else if (keycode == Settings.copyHotkey) {
                        copyHotkeyAction();
                    }
                    else if (keycode == Settings.refreshHotkey) {
                        commandAction(refreshCommand, this);
                    }
                    else if (keycode == Settings.backHotkey) {
                        commandAction(backCommand, this);
                    }
                    else if (keycode == Settings.fullscreenHotkey) {
                        commandAction(fullScreenCommand, this);
                    }
                    // ifdef OVER_100KB
                    else if (keycode == Settings.scrollTopHotkey) {
                        selectionMode = true;
                        selectedItem = items.size() - 1;
                    }
                    else if (keycode == Settings.scrollBottomHotkey) {
                        selectionMode = true;
                        selectedItem = 0;
                    }
                    // endif
                }
                break;
            }
        }
        repaint();
    }
    protected void keyPressed(int a) { keyEvent(a); }
    protected void keyRepeated(int a) { keyEvent(a); }

    // ifdef OVER_100KB
    protected void pointerPressed(int x, int y) {
        touchMode = true;
        super.pointerPressed(x, y);
    }

    protected void pointerDragged(int x, int y) {
        touchMode = true;
        super.pointerDragged(x, y);
    }

    protected void pointerReleased(int x, int y) {
        touchMode = true;

        if (!pointerWasTapped(fontHeight)) {
            super.pointerReleased(x, y);
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            ChannelViewItem item = (ChannelViewItem) items.elementAt(i);
            int itemPos = getItemPosition(i);
            int itemHeight = item.getHeight();
            if (y >= itemPos && y <= itemPos + itemHeight) {
                selectionMode = true;
                selectedItem = i;
                executeItemAction();
                break;
            }
        }
        repaint();
    }
    // endif

    private void uploadFile(Message recipientMsg) {
        try {
            if (!App.isLiteProxy) {
                App.error(Locale.get(UPLOAD_NOT_SUPPORTED));
            }
            else if (Settings.nativeFilePicker) {
                if (Util.supportsFileConn) {
                    // ifdef SAMSUNG_100KB
                    App.disp.setCurrent(new AttachmentPickerOld(recipientMsg));
                    // else
                    App.disp.setCurrent(new AttachmentPicker(recipientMsg));
                    // endif
                } else {
                    App.error(Locale.get(UPLOAD_ERROR_FILECONN));
                }
            }
            else {
                // ifdef OVER_100KB
                App.gatewaySendTyping();
                // endif
                String id = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;
                String url = Settings.api + "/upload?channel=" + id + "&token=" + App.uploadToken;
                if (recipientMsg != null) url += "&reply=" + recipientMsg.id;
                App.platRequest(url);
            }
        }
        catch (Exception e) {
            App.error(e);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            UnreadManager.save();
            App.channelIsOpen = false;
            if (App.isDM) App.openDMSelector(false, false);
            else if (App.selectedChannel.isThread) App.openThreadSelector(false, false);
            else App.openChannelSelector(false, false);
        }
        else if (c == sendCommand) {
            App.disp.setCurrent(new MessageBox());
        }
        else if (c == refreshCommand) {
            App.dontShowLoadScreen = true;
            App.openChannelView(true);
        }
        else if (c == selectCommand) {
            executeItemAction();
        }
        else if (c == uploadCommand) {
            uploadFile(null);
        }
        else if (c == fullScreenCommand) {
            fullscreen = !fullscreen;
            setFullScreenMode(fullscreen);
        }
        else {
            Message selected = ((ChannelViewItem) items.elementAt(selectedItem)).msg;

            if (c == replyUploadCommand) {
                uploadFile(selected);
            }
            else if (c == replyCommand) {
                App.disp.setCurrent(new ReplyForm(selected));
            }
            else if (c == copyCommand) {
                App.disp.setCurrent(new MessageCopyBox(selected.content));
            }
            else if (c == openUrlCommand) {
                App.disp.setCurrent(new URLList(selected.content));
            }
            else if (c == deleteCommand) {
                if (!App.isLiteProxy) {
                    App.error(Locale.get(DELETE_NOT_SUPPORTED));
                } else {
                    App.disp.setCurrent(new DeleteConfirmDialog(selected));
                }
            }
            else if (c == editCommand) {
                if (!App.isLiteProxy) {
                    App.error(Locale.get(EDIT_NOT_SUPPORTED));
                } else {
                    App.disp.setCurrent(new MessageBox(selected));
                }
            }
        }
    }
}