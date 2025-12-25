package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends KineticScrollingCanvas implements CommandListener, Strings {
    public Command backCommand;
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

//#ifdef OVER_100KB
    JSONObject pendingTheme;
    static String draftMessage;
//#endif

//#ifdef TOUCH_SUPPORT
//#ifndef BLACKBERRY
    boolean showBackButton;
    int backButtonStringWidth;
//#endif
    static Image messageBarLeft;
    static Image messageBarCenter;
    static Image messageBarRight;
    static Image messageBarAttachmentIcon;
    static Image messageBarEmojiIcon;
    static int messageBarIconHash;
    int messageBarWidth;
    static int messageBarHash;
    Image messageBar;
    String title;
//#endif

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

        addCommand(backCommand);
        addCommand(refreshCommand);
//#ifndef TOUCH_SUPPORT
        // In touch-enabled builds, these are shown as commands only when the bottom message bar is not shown
        addCommand(sendCommand);
        addCommand(uploadCommand);
//#endif

//#ifdef OVER_100KB
        draftMessage = "";
//#endif

//#ifdef BLACKBERRY
        if (shouldShowBottomBar()) setFullScreenMode(true);
//#else
//#ifdef MIDP2_GENERIC
        if (!Util.isKemulator)
//#endif
        {
            setFullScreenMode(Settings.fullscreenDefault);
            fullscreen = Settings.fullscreenDefault;

            fullScreenCommand = Locale.createCommand(TOGGLE_FULLSCREEN, Command.ITEM, 10);
            addCommand(fullScreenCommand);
        }
//#endif

//#ifdef J2ME_LOADER
        commands = new Vector();
//#endif
    }

//#ifdef TOUCH_SUPPORT
//#ifndef BLACKBERRY
    public void setFullScreenMode(boolean mode) {
        super.setFullScreenMode(mode);
        
//#ifdef MIDP2_GENERIC
        if (!Util.isKemulator)
//#endif
        {
            showBackButton = (mode && hasPointerEvents());

            if (showBackButton && backButtonStringWidth == 0) {
                backButtonStringWidth = App.messageFont.stringWidth(Locale.get(BACK));
            }
        }
    }
//#endif

    private boolean shouldShowBottomBar() {
        return hasPointerEvents() &&
            (Settings.messageBarMode == Settings.MESSAGE_BAR_ON || (Settings.messageBarMode == Settings.MESSAGE_BAR_AUTO && super.getHeight() > getWidth())) &&
            checkAndLoadMessageBar();
    }

    // 2x smooth downscaling algorithm specifically for message bar icons
    // the ones in Util are not smooth enough
    // makes several assumptions:
    // - alpha doesnt matter (but shouldn't be difficult to add if you need)
    // - image is square, same width and height
    // - width/height is an even number
    // - you want to downscale exactly to half resolution
    // TODO: we are currently calling this twice per image to do a 4x downscale, so we could optimize this to do 4x directly
    private static Image downscale(Image img) {
        int size = img.getWidth();
        int[] input = new int[size*size];
        img.getRGB(input, 0, size, 0, 0, size, size);

        size /= 2;
        int[] result = new int[size*size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int tl = input[y*4*size + x*2];
                int tlr = (tl & 0x00FF0000) >> 16;
                int tlg = (tl & 0x0000FF00) >> 8;
                int tlb = (tl & 0x000000FF);

                int tr = input[y*4*size + x*2 + 1];
                int trr = (tr & 0x00FF0000) >> 16;
                int trg = (tr & 0x0000FF00) >> 8;
                int trb = (tr & 0x000000FF);

                int bl = input[(y*4 + 2)*size + x*2];
                int blr = (bl & 0x00FF0000) >> 16;
                int blg = (bl & 0x0000FF00) >> 8;
                int blb = (bl & 0x000000FF);

                int br = input[(y*4 + 2)*size + x*2 + 1];
                int brr = (br & 0x00FF0000) >> 16;
                int brg = (br & 0x0000FF00) >> 8;
                int brb = (br & 0x000000FF);

                result[y*size + x] =
                    ((tlr + trr + blr + brr)/4) << 16 |
                    ((tlg + trg + blg + brg)/4) << 8 |
                    ((tlb + trb + blb + brb)/4);
            }
        }
        return Image.createRGBImage(result, size, size, false);
    }

    private Image getBarAttachmentIcon() {
        int size = Math.max(fontHeight, 9);
        int hash = size + Theme.messageBarBackgroundColor + Theme.messageBarColor;
        if (messageBarAttachmentIcon != null && hash == messageBarIconHash) {
            return messageBarAttachmentIcon;
        }
        messageBarAttachmentIcon = null;
        messageBarEmojiIcon = null;

        int renderSize = size*4;
        Image result = Image.createImage(renderSize, renderSize);
        Graphics g = result.getGraphics();

        // circle
        g.setColor(Theme.messageBarBackgroundColor);
        g.fillRect(0, 0, renderSize, renderSize);
        g.setColor(Theme.messageBarColor);
        g.fillArc(0, 0, renderSize, renderSize, 0, 360);

        // plus icon inside circle
        int padding = renderSize/5;
        int thickness = Math.max(renderSize/9, 1);
        if (renderSize%2 != thickness%2) thickness++;
        int offset = renderSize/2 - thickness/2;
        int length = renderSize - padding*2;
        g.setColor(Theme.messageBarBackgroundColor);
        g.fillRect(padding, offset, length, thickness);  // - line
        g.fillRect(offset, padding, thickness, length);  // | line

        result = downscale(result);
        result = downscale(result);
        messageBarAttachmentIcon = result;
        messageBarIconHash = hash;
        return result;
    }

    private Image getBarEmojiIcon() {
        int size = Math.max(fontHeight, 9);
        int hash = size + Theme.messageBarBackgroundColor + Theme.messageBarColor;
        if (messageBarEmojiIcon != null && hash == messageBarIconHash) {
            return messageBarEmojiIcon;
        }
        int renderSize = size*4;
        Image result = Image.createImage(renderSize, renderSize);
        Graphics g = result.getGraphics();

        // circle
        g.setColor(Theme.messageBarBackgroundColor);
        g.fillRect(0, 0, renderSize, renderSize);
        g.setColor(Theme.messageBarColor);
        g.fillArc(0, 0, renderSize, renderSize, 0, 360);

        // small circle inside the circle to make it an outline
        int outline = Math.max(renderSize/11, 1);
        int innerCircleSize = renderSize - outline*2;
        g.setColor(Theme.messageBarBackgroundColor);
        g.fillArc(outline, outline, innerCircleSize, innerCircleSize, 0, 360);

        // smile
        int smileOffset = renderSize/4;
        int smileWidth = renderSize - smileOffset*2;
        int smileHeight = smileWidth*3/4;
        int smileOffsetY = smileOffset - smileHeight + smileWidth;
        g.setColor(Theme.messageBarColor);
        g.fillArc(smileOffset, smileOffsetY, smileWidth, smileHeight, 180, 180);

        // eyes
        int eyeSize = Math.max(renderSize/7, 1);
        int eyeOffsetX = smileOffset + Math.max(eyeSize/5, 1);
        int eyeOffsetY = smileOffsetY + smileHeight/2 - eyeSize*2;
        g.fillArc(eyeOffsetX, eyeOffsetY, eyeSize, eyeSize, 0, 360);
        g.fillArc(renderSize - eyeOffsetX - eyeSize, eyeOffsetY, eyeSize, eyeSize, 0, 360);

        result = downscale(result);
        result = downscale(result);
        messageBarEmojiIcon = result;
        messageBarIconHash = hash;
        return result;
    }

    private boolean checkAndLoadMessageBar() {
        int fullWidth = getFullWidth();
        
        if (messageBarWidth != fullWidth) {
            final int imgWidth = 15;
            final int imgHeight = 32;
            final int imgPartWidth = 7;

            int barHeight = fontHeight*2;
            int newBarHash = barHeight + Theme.messageBarBackgroundColor + Theme.messageBarColor;
            int partWidth = barHeight*imgPartWidth/imgHeight;

            if (messageBarHash != newBarHash) {
                Image messageBarImg;
                try {
                    messageBarImg = Image.createImage("/bar.png");

                    // recolor the image (pixels which have non-zero alpha and are close to white)
                    int[] input = new int[imgWidth*imgHeight];
                    messageBarImg.getRGB(input, 0, imgWidth, 0, 0, imgWidth, imgHeight);

                    for (int i = 0; i < imgWidth*imgHeight; i++) {
                        int alpha = input[i] & 0xFF000000;

                        if (alpha != 0 && (input[i] & 0xFF) > 127) {
                            input[i] = alpha | Theme.messageBarBackgroundColor;
                        }
                    }
                    messageBarImg = Image.createRGBImage(input, imgWidth, imgHeight, true);
                }
                catch (java.io.IOException e) {
                    e.printStackTrace();
                    return false;
                }
                messageBarLeft = Image.createImage(messageBarImg, 0, 0, imgPartWidth, imgHeight, Sprite.TRANS_NONE);
                messageBarRight = Image.createImage(messageBarImg, imgPartWidth + 1, 0, imgPartWidth, imgHeight, Sprite.TRANS_NONE);
                messageBarCenter = Image.createImage(messageBarImg, imgPartWidth, 0, 1, imgHeight, Sprite.TRANS_NONE);
                messageBarLeft = Util.resizeImageBilinear(messageBarLeft, partWidth, barHeight);
                messageBarRight = Util.resizeImageBilinear(messageBarRight, partWidth, barHeight);

                messageBarHash = newBarHash;
            }

            Image messageBarCenterScaled = Util.resizeImageBilinear(messageBarCenter, fullWidth - partWidth*2, barHeight);

            messageBar = Image.createImage(fullWidth, barHeight);
            Graphics barG = messageBar.getGraphics();

            barG.setColor(Theme.channelViewBackgroundColor);
            barG.fillRect(0, 0, fullWidth, barHeight);
            
            barG.drawImage(messageBarLeft, 0, 0, Graphics.TOP | Graphics.LEFT);
            barG.drawImage(messageBarCenterScaled, barHeight*imgPartWidth/imgHeight, 0, Graphics.TOP | Graphics.LEFT);
            barG.drawImage(messageBarRight, fullWidth, 0, Graphics.TOP | Graphics.RIGHT);
            barG.drawImage(getBarAttachmentIcon(), fontHeight/2, fontHeight/2, Graphics.TOP | Graphics.LEFT);
            barG.drawImage(getBarEmojiIcon(), fullWidth - fontHeight/2, fontHeight/2, Graphics.TOP | Graphics.RIGHT);

            String label;
            if (draftMessage.length() > 0) {
                barG.setColor(Theme.messageBarDraftColor);
                label = draftMessage;
                if (label.indexOf("\n") != -1) {
                    label = label.substring(0, label.indexOf("\n")) + "...";
                }
            }
            else {
                barG.setColor(Theme.messageBarColor);
                label =
//#ifndef BLACKBERRY
                    (width < fontHeight*15) ? Locale.get(MESSAGE) :
//#endif
                    Locale.get(MESSAGE_WITH_CHANNEL) + title;
            }

            barG.setFont(App.messageFont);
            barG.drawString(
                Util.stringToWidth(label, App.messageFont, fullWidth - fontHeight*4),
                barHeight*19/20, fontHeight/2, Graphics.TOP | Graphics.LEFT
            );
            messageBarWidth = fullWidth;
        }
        return true;
    }
    
    public int getHeight() {
        if (!shouldShowBottomBar()) return super.getHeight();
        
        return super.getHeight() - fontHeight*2;
    }
//#endif

    public void showNotify() {
        if (haveShown) return;
        haveShown = true;
        width = getWidth();
        height = getHeight();
        requestUpdate(false, false);
        repaint();
    }

//#ifdef TOUCH_SUPPORT
    public void hideNotify() {
        Object curr = App.disp.getCurrent();
        if (curr instanceof MessageBox) {
            ((MessageBox) curr).showEmojiPicker();
        }
    }
//#endif

//#ifdef OVER_100KB
    public void setDraftMessage(String msg) {
        draftMessage = msg;
//#ifdef TOUCH_SUPPORT
        messageBarWidth = 0;  // force redraw the message bar
        repaint();
//#endif
    }
//#endif

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

//#ifdef TOUCH_SUPPORT
        title = resultBuf.toString();
//#endif
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
        int iconAreaWidth = (useIcons ? fontHeight*2 : fontHeight/4);
        int contentWidth = width - iconAreaWidth;
        int embedTextX = iconAreaWidth + fontHeight/3;
        int embedTextWidth = contentWidth - fontHeight/2 - fontHeight*2/3;

        for (int i = 0; i < App.messages.size(); i++) {
            Message msg = (Message) App.messages.elementAt(i);
            boolean needUpdate = msg.needUpdate;

//#ifdef OVER_100KB
            if (msg.contentFormatted == null || wasResized || needUpdate) {
                msg.contentFormatted = new FormattedString(msg.content, App.messageFont, contentWidth, iconAreaWidth, false, msg.isEdited, msg.isForwarded);
                msg.needUpdate = false;
            }
//#else
            if (msg.contentLines == null || wasResized || needUpdate) {
                msg.contentLines = Util.wordWrap(msg.content, contentWidth, App.messageFont);
                msg.needUpdate = false;
            }
//#endif

            if (msg.attachments != null && msg.attachments.size() > 0) {
                ChannelViewItem attachItem = new ChannelViewItem(ChannelViewItem.ATTACHMENTS_BUTTON);
                attachItem.msg = msg;
                items.addElement(attachItem);
                maxScroll += attachItem.getHeight();
            }

            if (msg.embeds != null && msg.embeds.size() > 0) {
                for (int e = 0; e < msg.embeds.size(); e++) {
                    Embed emb = (Embed) msg.embeds.elementAt(e);

//#ifdef OVER_100KB
                    if ((wasResized || emb.titleFormatted == null || needUpdate) && emb.title != null) {
                        emb.titleFormatted = new FormattedString(emb.title, App.titleFont, embedTextWidth, embedTextX, false, false, false);
                        msg.needUpdate = false;
                    }
                    if ((wasResized || emb.descFormatted == null || needUpdate) && emb.description != null) {
                        emb.descFormatted = new FormattedString(emb.description, App.messageFont, embedTextWidth, embedTextX, false, false, false);
                        msg.needUpdate = false;
                    }
//#else
                    if ((wasResized || emb.titleLines == null || needUpdate) && emb.title != null) {
                        emb.titleLines = Util.wordWrap(emb.title, embedTextWidth, App.titleFont);
                        msg.needUpdate = false;
                    }
                    if ((wasResized || emb.descLines == null || needUpdate) && emb.description != null) {
                        emb.descLines = Util.wordWrap(emb.description, embedTextWidth, App.messageFont);
                        msg.needUpdate = false;
                    }
//#endif
                }
            }

            if (
                msg.showAuthor
//#ifdef OVER_100KB
                || msg.contentFormatted.height != 0
//#else
                || msg.contentLines.length != 0
//#endif
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
            // More unreads -> highlight the "show older messages" button for convenience
            scroll = 0;
            selectedItem = items.size() - (hasMoreUnreads ? 2 : 1);
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

    public void sizeChanged(int w, int h) {
        repaint();
    }

//#ifdef J2ME_LOADER
    Vector commands;
//#endif

    private void _removeCommand(Command c) {
//#ifdef J2ME_LOADER
        if (commands.contains(c)) {
            commands.removeElement(c);
            Util.sleep(20);
//#endif
            removeCommand(c);
//#ifdef J2ME_LOADER
        }
//#endif
    }

    private void _addCommand(Command c) {
//#ifdef J2ME_LOADER
        if (!commands.contains(c)) {
            commands.addElement(c);
            Util.sleep(20);
//#endif
            addCommand(c);
//#ifdef J2ME_LOADER
        }
//#endif
    }

    private void updateCommands(ChannelViewItem selected) {
        if (selectionMode && selected.type == ChannelViewItem.MESSAGE && !selected.msg.isStatus) {
            _addCommand(copyCommand);

            if (App.myUserId.equals(selected.msg.author.id)) {
                _addCommand(editCommand);
                _addCommand(deleteCommand);
            } else {
                _removeCommand(editCommand);
                _removeCommand(deleteCommand);
            }

            if (Util.indexOfAny(selected.msg.content, URLList.urlStarts, 0) != -1) {
                _addCommand(openUrlCommand);
            } else {
                _removeCommand(openUrlCommand);
            }
        } else {
            _removeCommand(copyCommand);
            _removeCommand(editCommand);
            _removeCommand(deleteCommand);
            _removeCommand(openUrlCommand);
        }

        if (
//#ifdef MIDP2_GENERIC
            !Util.isTouch &&
//#endif
            selectionMode && selected.type != ChannelViewItem.MESSAGE && selected.type != ChannelViewItem.UNREAD_INDICATOR
        ) {
            _addCommand(selectCommand);
        } else {
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

    public void paint(Graphics g) {
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

        if (items.size() > 0) {
            makeSelectedItemVisible();

            ChannelViewItem selected = (ChannelViewItem) items.elementAt(selectedItem);
            updateCommands(selected);
        }

//#ifdef TOUCH_SUPPORT
        if (shouldShowBottomBar()) {
            _removeCommand(sendCommand);
            _removeCommand(uploadCommand);
        } else {
            _addCommand(sendCommand);
            _addCommand(uploadCommand);
        }
//#endif

        g.setFont(App.messageFont);
        clearScreen(g, Theme.channelViewBackgroundColor);

        if (items.size() == 0) {
            g.setColor(Theme.channelViewEmptyTextColor);
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
            g.setColor(Theme.bannerBackgroundColor);
            g.fillRect(0, 0, width, fontHeight*lines.length + fontHeight/4);

            g.setColor(Theme.bannerTextColor);
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
            g.setColor(Theme.outdatedBannerBackgroundColor);
            g.fillRect(0, bannerY, width, fontHeight*lines.length + fontHeight/4);

            g.setColor(Theme.outdatedBannerTextColor);
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
            g.setColor(Theme.typingBannerBackgroundColor);
            g.fillRect(0, bannerY, width, fontHeight*lines.length + fontHeight/4);

            g.setColor(Theme.typingBannerTextColor);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(
                    lines[i], width/2, bannerY + i*fontHeight + fontHeight/8,
                    Graphics.TOP | Graphics.HCENTER
                );
            }
        }

//#ifdef TOUCH_SUPPORT
//#ifndef BLACKBERRY
        if (showBackButton) {
            int buttonOffset = fontHeight/2;
            int buttonMargin = fontHeight/3;
            int buttonWidth = backButtonStringWidth + buttonMargin*2;
            int buttonHeight = fontHeight + buttonMargin*2;

            g.setColor(Theme.selectedButtonBackgroundColor);  // 'selected' color so it stands out more
            g.fillRoundRect(
                width - buttonWidth - buttonOffset,
                height - buttonHeight - buttonOffset,
                buttonWidth,
                buttonHeight,
                buttonOffset,
                buttonOffset
            );
            g.setColor(Theme.selectedButtonTextColor);
            g.setFont(App.messageFont);
            g.drawString(
                Locale.get(BACK),
                width - buttonMargin - buttonOffset,
                height - buttonMargin - buttonOffset,
                Graphics.BOTTOM | Graphics.RIGHT
            );
        }
//#endif
//#endif

        // g.setColor(0x00ff0000);
        // g.drawString(
        //     "" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()), width, 0,
        //     Graphics.TOP | Graphics.RIGHT
        // );

//#ifdef TOUCH_SUPPORT
        if (shouldShowBottomBar()) {
            g.setClip(0, 0, getFullWidth(), super.getHeight());
            g.drawImage(messageBar, 0, getHeight(), Graphics.TOP | Graphics.LEFT);
        }
//#endif

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

    private void showMessageBox(boolean withEmojiPicker) {
//#ifdef OVER_100KB
        // Show warning if DMing someone for the first time (Discord spam filter)
        if (App.isDM && items.size() == 0) App.disp.setCurrent(new DMWarningDialog());
        else
//#endif
        {
//#ifdef TOUCH_SUPPORT
            MessageBox box = new MessageBox();
            box.showEmojiPicker = withEmojiPicker;
            App.disp.setCurrent(box);
//#else
            App.disp.setCurrent(new MessageBox());
//#endif
        }
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

    private void scrollUp() {
        int max = items.size() - 1;
        if (selectedItem > max) selectedItem = max;
        if (selectedItem == max) return;
        selectedItem++;
        
        // If this item was a "NEW" indicator, skip it
        ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);

        if (item.type == ChannelViewItem.UNREAD_INDICATOR) {
            makeSelectedItemVisible();
            if (selectedItem == max) {
                scrollDown();
            } else {
                scrollUp();
            }
        }
    }

    private void scrollDown() {
        if (selectedItem < 0) selectedItem = 0;
        if (selectedItem == 0) return;
        selectedItem--;
        
        // If this item was a "NEW" indicator, skip it
        ChannelViewItem item = (ChannelViewItem) items.elementAt(selectedItem);

        if (item.type == ChannelViewItem.UNREAD_INDICATOR) {
            makeSelectedItemVisible();
            if (selectedItem == 0) {
                scrollUp();
            } else {
                scrollDown();
            }
        }
    }

    private void navKeyAction(int action, int thisItemHeight, int thisItemPos) {
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
                else scrollUp();
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
                else scrollDown();
                break;
            }
            case FIRE: {
                executeItemAction();
                break;
            }
        }
    }
    
    public void keyAction(int keycode) {
        touchMode = false;
        int thisItemHeight = ((ChannelViewItem) items.elementAt(selectedItem)).getHeight();
        int thisItemPos = getItemPosition(selectedItem);

        int action = getGameAction(keycode);

        // Check if hotkey was pressed
        if (Settings.defaultHotkeys) {
            // default hotkey (j2me game actions A/B/C/D)
            switch (action) {
                case GAME_A: showMessageBox(false); break;
                case GAME_B: replyHotkeyAction(); break;
                case GAME_C: copyHotkeyAction(); break;
                case GAME_D: commandAction(refreshCommand, null); break;
                default: navKeyAction(action, thisItemHeight, thisItemPos); break;
            }
        } else {
            // user bound key (when 'default hotkeys' option disabled)
            if (keycode == Settings.sendHotkey) {
                showMessageBox(false);
            }
            else if (keycode == Settings.replyHotkey) {
                replyHotkeyAction();
            }
            else if (keycode == Settings.copyHotkey) {
                copyHotkeyAction();
            }
            else if (keycode == Settings.refreshHotkey) {
                commandAction(refreshCommand, null);
            }
            else if (keycode == Settings.backHotkey) {
                commandAction(backCommand, null);
            }
            else if (keycode == Settings.fullscreenHotkey) {
                commandAction(fullScreenCommand, null);
            }
//#ifdef OVER_100KB
            else if (keycode == Settings.scrollTopHotkey) {
                selectionMode = true;
                selectedItem = items.size() - 1;
            }
            else if (keycode == Settings.scrollBottomHotkey) {
                selectionMode = true;
                selectedItem = 0;
            }
//#endif
            else {
                navKeyAction(action, thisItemHeight, thisItemPos);
            }
        }
        repaint();
    }

//#ifdef TOUCH_SUPPORT
    private boolean tappedOnBottom;

    public void pointerPressed(int x, int y) {
        tappedOnBottom = (y > getHeight() && shouldShowBottomBar());
//#ifndef BLACKBERRY
        int buttonOffset = fontHeight/2;
        int buttonMargin = fontHeight/3;
        int buttonWidth = backButtonStringWidth + buttonMargin*2;
        int buttonHeight = fontHeight + buttonMargin*2;

        if (showBackButton && !tappedOnBottom && x >= width - buttonWidth - buttonOffset && y >= height - buttonHeight - buttonOffset) {
            commandAction(fullScreenCommand, null);
            return;
        }
//#endif
        touchMode = true;
        super.pointerPressed(x, y);
    }

    public void pointerDragged(int x, int y) {
        touchMode = true;
        super.pointerDragged(x, y);
    }

    public void pointerReleased(int x, int y) {
//#ifdef TOUCH_SUPPORT
        if (tappedOnBottom && y > getHeight() && shouldShowBottomBar()) {
            // Tapped on the bottom message bar, determine which part was pressed:
            // Button on left-most edge: upload file
            if (x < fontHeight*2) {
                commandAction(uploadCommand, null);
            }
            // Button on right-most edge: send emoji (open "send message" box then the emoji picker)
            else if (x > getWidth() - fontHeight*2) {
                showMessageBox(true);
            }
            // Rest of the bar: send message
            else {
                showMessageBox(false);
            }
            return;
        }
//#endif
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
//#endif

//#ifdef OVER_100KB
    private void uploadFileInitial(Message recipientMsg) {
        if (!Settings.hasSeenUploadWarning) {
            App.disp.setCurrent(new UploadWarningDialog(recipientMsg));
        } else {
            uploadFile(recipientMsg);
        }
    }
//#endif

    public void uploadFile(Message recipientMsg) {
        try {
            if (!App.isLiteProxy) {
                App.error(Locale.get(UPLOAD_NOT_SUPPORTED));
            }
            else if (Settings.nativeFilePicker) {
                if (Util.supportsFileConn) {
                    App.disp.setCurrent(new AttachmentPicker(recipientMsg));
                } else {
                    App.error(Locale.get(UPLOAD_ERROR_FILECONN));
                }
            }
            else {
//#ifdef OVER_100KB
                App.gatewaySendTyping();
//#endif
                String id = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;
//#ifdef OVER_100KB
                // Go to 'upload2' page to bypass proxy-side upload warning
                String url = Settings.api + "/upload2?channel=" + id + "&token=" + App.uploadToken;
//#else
                String url = Settings.api + "/upload?channel=" + id + "&token=" + App.uploadToken;
//#endif
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
//#ifdef OVER_100KB
            if (pendingTheme != null) {
                App.disp.setCurrent(new ThemeSaveDialog());
            } else
//#endif
            {
                UnreadManager.save();
                App.channelIsOpen = false;
                if (App.isDM) App.openDMSelector(false, false);
                else if (App.selectedChannel.isThread) App.openThreadSelector(false, false);
                else App.openChannelSelector(false, false);
            }
        }
        else if (c == sendCommand) {
            showMessageBox(false);
        }
        else if (c == refreshCommand) {
            App.dontShowLoadScreen = true;
            App.openChannelView(true);
        }
        else if (c == selectCommand) {
            executeItemAction();
        }
        else if (c == uploadCommand) {
//#ifdef OVER_100KB
            uploadFileInitial(null);
//#else
            uploadFile(null);
//#endif
        }
        else if (c == fullScreenCommand) {
            fullscreen = !fullscreen;
            setFullScreenMode(fullscreen);
        }
        else {
            Message selected = ((ChannelViewItem) items.elementAt(selectedItem)).msg;

            if (c == replyUploadCommand) {
//#ifdef OVER_100KB
                uploadFileInitial(selected);
//#else
                uploadFile(selected);
//#endif
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
//#ifdef OVER_100KB
                App.disp.setCurrent(new DeleteConfirmDialog(selected));
//#else
                App.disp.setCurrent(new Dialogs100kb(selected));
//#endif
            }
            else if (c == editCommand) {
                App.disp.setCurrent(new MessageBox(selected));
            }
        }
    }
}
