package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class ChannelViewItem implements Strings {
    static final int MESSAGE = 0;
    static final int OLDER_BUTTON = 1;
    static final int NEWER_BUTTON = 2;
    static final int ATTACHMENTS_BUTTON = 3;
    static final int UNREAD_INDICATOR = 4;

    int type;  // one of the constants defined above
    Message msg;  // message data for MESSAGE and ATTACHMENTS_BUTTON types

    // Ref message (referenced message) = recipient message of a reply

    Image refImg;  // cached ref message image for MESSAGE type if it is a reply
    // ifdef SAMSUNG
    Image refImg2;  // second (right-side) part of ref message image
    // endif
    // ifdef OVER_100KB
    FormattedString refImgFormatStr;
    // endif
    boolean refImgHasPfp;  // cached ref image has profile picture loaded
    boolean refImgHasColor;  // cached ref image has name color loaded
    boolean refImgSelected;  // was the cached ref image selected? used for determining correct background color
    long refImgLastDrawn;    // timestamp when ref image was last drawn, used for preventing too frequent redraws

    public ChannelViewItem(int type) {
        this.type = type;
    }

    private static Image unreadIndicatorImage;

    public static void createUnreadIndicatorImage() {
        int fontHeight = App.titleFont.getHeight();
        int stringWidth = App.titleFont.stringWidth(Locale.get(NEW_MARKER));
        int totalWidth = fontHeight/2 + stringWidth + fontHeight/4;

        unreadIndicatorImage = Image.createImage(totalWidth, fontHeight);
        Graphics g = unreadIndicatorImage.getGraphics();

        g.setColor(ChannelView.backgroundColors[Settings.theme]);
        g.fillRect(0, 0, totalWidth, fontHeight);

        g.setColor(0xf23f43);
        g.fillRect(
            fontHeight/2,
            0,
            stringWidth + fontHeight/4,
            fontHeight
        );
        g.fillTriangle(
            fontHeight/2,
            0,
            fontHeight/2,
            fontHeight,
            0,
            fontHeight/2
        );
        g.setColor(0xFFFFFF);
        g.setFont(App.titleFont);
        g.drawString(
            Locale.get(NEW_MARKER),
            fontHeight/2,
            0,
            Graphics.TOP | Graphics.LEFT
        );

        unreadIndicatorImage = Util.resizeImageBilinear(unreadIndicatorImage, totalWidth*2/3, fontHeight*2/3);
    }

    /**
     * Determine whether or not the 'reply' menu option should be displayed when this item is selected.
     */
    public boolean shouldShowReplyOption() {
        // Don't show reply option for status messages
        if (msg != null && msg.isStatus) return false;

        // Show reply option for message item
        if (type == ChannelViewItem.MESSAGE) return true;

        // Also show reply option if message is merged (no author row is shown)
        // and the message only consists of attachments (only shown as a 'view attachments' button)
        return (
            type == ChannelViewItem.ATTACHMENTS_BUTTON &&
            !msg.showAuthor && msg.content.length() == 0
        );
    }

    /**
     * Gets the amount of vertical pixels that this item will take up on the screen.
     */
    public int getHeight() {
        int messageFontHeight = App.messageFont.getHeight();
        switch (type) {
            case MESSAGE: {
                // Each content line + little bit of spacing between messages
                int result =
                    // ifdef OVER_100KB
                    msg.contentFormatted.height +
                    // else
                    messageFontHeight*msg.contentLines.length +
                    // endif
                    messageFontHeight/4;
    
                // One line for message author
                if (msg.showAuthor) result += App.authorFont.getHeight();
    
                // Each embed's height + top margin
                if (msg.embeds != null && msg.embeds.size() > 0) {
                    for (int i = 0; i < msg.embeds.size(); i++) {
                        Embed emb = (Embed) msg.embeds.elementAt(i);
                        result += emb.getHeight(messageFontHeight) + messageFontHeight/4;
                    }
                }
    
                // Referenced message if message is a reply and option is enabled
                if (msg.recipient != null && Settings.showRefMessage) {
                    result += messageFontHeight*5/4;
                }
    
                return result;
            }

            case UNREAD_INDICATOR: {
                return messageFontHeight;
            }

            default: {
                // For buttons
                return messageFontHeight*5/3;
            }
        }
    }

    private boolean shouldRedrawRefMessage(boolean selected) {
        // Ref message has not been rendered yet
        if (refImg == null) return true;

        // Rendered ref image's selection status (= background color) has changed
        if (refImgSelected != selected) return true;

        // Finally, check if profile pic or name color for recipient has not been shown/loaded yet
        // In this case, also check if the last redraw was not too recent
        return ((!refImgHasPfp || !refImgHasColor) && System.currentTimeMillis() > refImgLastDrawn + 250);
    }

    /**
     * Draws this channel view item on the screen.
     * @param g Graphics object to use for drawing.
     * @param y Vertical position (offset) to draw at, in pixels.
     * @param width Horizontal area available for drawing, in pixels.
     */
    public void draw(Graphics g, int y, int width, boolean selected) {
        int messageFontHeight = App.messageFont.getHeight();
        boolean useIcons = Settings.pfpType != Settings.PFP_TYPE_NONE;
        int iconSize = messageFontHeight*4/3;

        switch (type) {
            case MESSAGE: {
                // Horizontal position where recipient message is drawn
                // pfps on = same as where message contents begin
                // pfps off = leave some room for the line connecting the recipient and reply messages
                int refDrawX = useIcons ? messageFontHeight*2 : messageFontHeight;

                // Horizontal position where message contents begin
                // pfps on = leave room for pfp and its margins
                // pfps off = leave a small left margin so text isnt stuck to left edge of screen
                int x = useIcons ? messageFontHeight*2 : messageFontHeight/4;

                // Highlight background if message is selected
                if (selected) {
                    g.setColor(ChannelView.highlightColors[Settings.theme]);
                    g.fillRect(0, y, width, getHeight());
                }
                
                y += messageFontHeight/8;

                if (msg.showAuthor) {
                    int recipientColor = 0;
                    
                    if (msg.recipient != null) {
                        boolean hasColor = NameColorCache.has(msg.recipient, true);
                        
                        recipientColor = NameColorCache.get(msg.recipient.id);
                        if (recipientColor == 0) recipientColor = ChannelView.authorColors[Settings.theme];

                        // Draw referenced message if option is enabled
                        if (Settings.showRefMessage) {
                            if (shouldRedrawRefMessage(selected)) {
                                refImgHasPfp =
                                    !useIcons || Settings.pfpSize == Settings.PFP_SIZE_PLACEHOLDER ||
                                    msg.recipient.getIconHash() == null ||
                                    IconCache.hasResized(msg.recipient, messageFontHeight);

                                refImgHasColor = hasColor;

                                // ifdef OVER_100KB
                                boolean useFormattedString =
                                    FormattedString.emojiMode != FormattedString.EMOJI_MODE_OFF ||
                                    FormattedString.useMarkdown;
                                // endif

                                // Create an image where the ref message will be rendered.
                                // This will then be downscaled, giving us a smaller font size than what J2ME normally allows.
                                int refImgFullWidth = (width - refDrawX)*4/3;
                                // On Samsung (JBlend), the image has to be drawn in two parts due to a bug
                                // ifdef SAMSUNG
                                int refImgFull2Width = refImgFullWidth - width;
                                refImgFullWidth = width;
                                // endif
                                Image refImgFull = Image.createImage(refImgFullWidth, messageFontHeight);
                                Graphics refG = refImgFull.getGraphics();

                                // Fill ref message image with the same background color that the rest of the message has
                                int refImgBackgroundColor = selected ?
                                    ChannelView.highlightColors[Settings.theme] : ChannelView.backgroundColors[Settings.theme];
                                refG.setColor(refImgBackgroundColor);
                                refG.fillRect(0, 0, refImgFullWidth, messageFontHeight);

                                int refX = 0;

                                if (useIcons) {
                                    Image icon = IconCache.getResized(msg.recipient, messageFontHeight);

                                    if (icon != null) {
                                        refG.drawImage(icon, refX, 0, Graphics.TOP | Graphics.LEFT);
                                    } else {
                                        refG.setColor(msg.recipient.iconColor);

                                        if (Settings.pfpType == Settings.PFP_TYPE_CIRCLE || Settings.pfpType == Settings.PFP_TYPE_CIRCLE_HQ) {
                                            refG.fillArc(refX, 0, messageFontHeight, messageFontHeight, 0, 360);
                                        } else {
                                            refG.fillRect(refX, 0, messageFontHeight, messageFontHeight);
                                        }
                                    }

                                    refX += messageFontHeight;
                                }
                                refX += messageFontHeight/3;

                                refG.setFont(App.titleFont);
                                refG.setColor(recipientColor);
                                refG.drawString(msg.recipient.name, refX, 0, Graphics.TOP | Graphics.LEFT);

                                refX += App.titleFont.stringWidth(msg.recipient.name) + messageFontHeight/3;
                                
                                refG.setFont(App.messageFont);
                                refG.setColor(ChannelView.refMessageColors[Settings.theme]);
                                // ifdef OVER_100KB
                                if (useFormattedString) {
                                    if (refImgFormatStr == null) {
                                        refImgFormatStr = new FormattedString(msg.refContent, App.messageFont, refImgFullWidth, refX, true, false, false);
                                    }
                                    refImgFormatStr.draw(refG, 0);
                                } else
                                // endif
                                {
                                    refG.drawString(msg.refContent, refX, 0, Graphics.TOP | Graphics.LEFT);
                                }

                                int refImgResizeWidth = width - refDrawX;
                                // ifdef SAMSUNG
                                refImgResizeWidth = width*3/4;
                                // endif
                                refImg = Util.resizeImageBilinear(refImgFull, refImgResizeWidth, messageFontHeight*3/4);

                                // ifdef SAMSUNG
                                Image refImgFull2 = Image.createImage(refImgFull2Width, messageFontHeight);
                                refG = refImgFull2.getGraphics();

                                refG.setColor(refImgBackgroundColor);
                                refG.fillRect(0, 0, refImgFull2Width, messageFontHeight);

                                refG.setFont(App.messageFont);
                                refG.setColor(ChannelView.refMessageColors[Settings.theme]);
                                // ifdef OVER_100KB
                                if (useFormattedString) {
                                    refG.translate(-refImgFullWidth, 0);
                                    refImgFormatStr.draw(refG, 0);
                                } else
                                // endif
                                {
                                    refG.drawString(msg.refContent, -refImgFullWidth + refX, 0, Graphics.TOP | Graphics.LEFT);
                                }

                                refImg2 = Util.resizeImageBilinear(refImgFull2, refImgFull2Width*3/4, messageFontHeight*3/4);
                                // endif

                                refImgSelected = selected;
                                refImgLastDrawn = System.currentTimeMillis();
                            }

                            // draw downscaled refmessage
                            y += messageFontHeight/4;
                            g.drawImage(refImg, refDrawX, y, Graphics.TOP | Graphics.LEFT);
                            // ifdef SAMSUNG
                            g.drawImage(refImg2, refDrawX + width*3/4, y, Graphics.TOP | Graphics.LEFT);
                            // endif

                            // draw connecting line between refmessage and message
                            y += messageFontHeight*3/8;
                            g.setColor(0x00666666);
                            g.drawLine(refDrawX/2, y, refDrawX/2, y + messageFontHeight/2);  // vertical line |
                            g.drawLine(refDrawX/2, y, refDrawX*7/8, y);  // horizontal line -

                            y += messageFontHeight*5/8;
                        }
                    }

                    // Draw icon
                    if (useIcons) {
                        Image icon = IconCache.getResized(msg.author, iconSize);
                        int iconX = messageFontHeight/3;
                        int iconY = y + messageFontHeight/6;

                        if (icon != null) {
                            // Icon is loaded, draw it
                            g.drawImage(icon, iconX, iconY, Graphics.TOP | Graphics.LEFT);
                        } else {
                            // Icon is not loaded, draw a placeholder with the username's
                            // initials on a background whose color is determined by the user ID
                            g.setColor(msg.author.iconColor);

                            if (Settings.pfpType == Settings.PFP_TYPE_CIRCLE || Settings.pfpType == Settings.PFP_TYPE_CIRCLE_HQ) {
                                g.fillArc(iconX, iconY, iconSize, iconSize, 0, 360);
                            } else {
                                g.fillRect(iconX, iconY, iconSize, iconSize);
                            }

                            g.setColor(0x00FFFFFF);
                            g.setFont(App.messageFont);
                            g.drawString(
                                msg.author.initials,
                                iconX + iconSize/2,
                                iconY + iconSize/2 - messageFontHeight/2,
                                Graphics.TOP | Graphics.HCENTER
                            );
                        }
                    }

                    // Draw author (and recipient if applicable)
                    int authorColor = NameColorCache.get(msg.author);
                    if (authorColor == 0) authorColor = ChannelView.authorColors[Settings.theme];
                    
                    int authorX = x;

                    g.setColor(authorColor);
                    g.setFont(App.authorFont);
                    g.drawString(msg.author.name, authorX, y, Graphics.TOP | Graphics.LEFT);

                    authorX += App.authorFont.stringWidth(msg.author.name);

                    if (msg.recipient != null && !Settings.showRefMessage) {
                        g.setFont(App.timestampFont);
                        g.setColor(ChannelView.authorColors[Settings.theme]);
                        g.drawString(" -> ", authorX, y, Graphics.TOP | Graphics.LEFT);
                        
                        authorX += App.timestampFont.stringWidth(" -> ");

                        g.setFont(App.authorFont);
                        g.setColor(recipientColor);
                        g.drawString(msg.recipient.name, authorX, y, Graphics.TOP | Graphics.LEFT);

                        authorX += App.authorFont.stringWidth(msg.recipient.name);
                    }

                    // Draw timestamp
                    g.setColor(ChannelView.timestampColors[Settings.theme]);
                    g.setFont(App.timestampFont);
                    g.drawString("  " + msg.timestamp, authorX, y, Graphics.TOP | Graphics.LEFT);
                    y += App.authorFont.getHeight();
                }

                // Draw message content
                // Use timestamp color for status messages to distinguish them
                if (msg.isStatus) {
                    g.setColor(ChannelView.timestampColors[Settings.theme]);
                } else {
                    g.setColor(ChannelView.messageColors[Settings.theme]);
                }
                g.setFont(App.messageFont);
                // ifdef OVER_100KB
                msg.contentFormatted.draw(g, y);
                y += msg.contentFormatted.height;
                // else
                for (int i = 0; i < msg.contentLines.length; i++) {
                    g.drawString(msg.contentLines[i], x, y, Graphics.TOP | Graphics.LEFT);
                    y += messageFontHeight;
                }
                // endif

                // Draw embeds
                if (msg.embeds != null && msg.embeds.size() > 0) {
                    for (int i = 0; i < msg.embeds.size(); i++) {
                        Embed emb = (Embed) msg.embeds.elementAt(i);
                        y += messageFontHeight/4; // Top margin

                        if (selected) g.setColor(ChannelView.darkBgColors[Settings.theme]);
                        else g.setColor(ChannelView.highlightColors2[Settings.theme]);

                        g.fillRoundRect(
                            x, y,
                            width - x - messageFontHeight/2,
                            emb.getHeight(messageFontHeight),
                            messageFontHeight/2,
                            messageFontHeight/2
                        );

                        y += messageFontHeight/3;  // Top padding
                        x += messageFontHeight/3;  // Left padding

                        if (emb.title != null) {
                            g.setColor(0x0000a8fc);
                            g.setFont(App.titleFont);
                            // ifdef OVER_100KB
                            emb.titleFormatted.draw(g, y);
                            y += emb.titleFormatted.height;
                            // else
                            for (int l = 0; l < emb.titleLines.length; l++) {
                                g.drawString(emb.titleLines[l], x, y, Graphics.TOP|Graphics.LEFT);
                                y += messageFontHeight;
                            }
                            // endif
                            // Spacing between title and desc
                            if (emb.description != null) y += messageFontHeight/4;
                        }

                        if (emb.description != null) {
                            g.setColor(ChannelView.messageColors[Settings.theme]);
                            g.setFont(App.messageFont);
                            // ifdef OVER_100KB
                            emb.descFormatted.draw(g, y);
                            y += emb.descFormatted.height;
                            // else
                            for (int l = 0; l < emb.descLines.length; l++) {
                                g.drawString(emb.descLines[l], x, y, Graphics.TOP|Graphics.LEFT);
                                y += messageFontHeight;
                            }
                            // endif
                        }

                        x -= messageFontHeight/3;  // Undo left padding
                        y += messageFontHeight/3;  // Bottom padding
                    }
                }
                break;
            }

            case OLDER_BUTTON:
            case NEWER_BUTTON: {
                g.setFont(App.messageFont);

                String caption = (type == OLDER_BUTTON) ?
                    Locale.get(VIEW_OLDER_MESSAGES_L) : Locale.get(VIEW_NEWER_MESSAGES_L);

                if (selected) g.setColor(ChannelView.darkBgColors[Settings.theme]);
                else g.setColor(ChannelView.highlightColors2[Settings.theme]);

                int textWidth = App.messageFont.stringWidth(caption);
                g.fillRoundRect(
                    width/2 - textWidth/2 - messageFontHeight,
                    y + messageFontHeight/6,
                    textWidth + messageFontHeight*2,
                    messageFontHeight*4/3,
                    messageFontHeight/2,
                    messageFontHeight/2
                );

                g.setColor(ChannelView.authorColors[Settings.theme]);
                g.drawString(
                    caption, width/2, y + messageFontHeight/3,
                    Graphics.TOP | Graphics.HCENTER
                );
                break;
            }

            // Similar to older/newer button, but left aligned
            case ATTACHMENTS_BUTTON: {
                String caption =
                    Locale.get(VIEW_ATTACHMENTS_PREFIX) +
                    msg.attachments.size() +
                    (msg.attachments.size() > 1 ?
                        Locale.get(VIEW_ATTACHMENTS_SUFFIX) :
                        Locale.get(VIEW_ATTACHMENT_SUFFIX)
                    );

                g.setFont(App.messageFont);

                if (selected) g.setColor(ChannelView.darkBgColors[Settings.theme]);
                else g.setColor(ChannelView.highlightColors2[Settings.theme]);

                int x = useIcons ? messageFontHeight*2 : 0;
                int textWidth = App.messageFont.stringWidth(caption);

                g.fillRoundRect(
                    x + messageFontHeight/2,
                    y + messageFontHeight/6,
                    textWidth + messageFontHeight,
                    messageFontHeight*4/3,
                    messageFontHeight/2,
                    messageFontHeight/2
                );

                g.setColor(ChannelView.authorColors[Settings.theme]);
                g.drawString(caption, x + messageFontHeight, y + messageFontHeight/3, Graphics.TOP | Graphics.LEFT);
                break;
            }

            case UNREAD_INDICATOR: {
                int screenWidth = App.disp.getCurrent().getWidth();
                int imageX = screenWidth - messageFontHeight/4 - unreadIndicatorImage.getWidth();
                g.setColor(0xf23f43);
                g.drawImage(
                    unreadIndicatorImage,
                    imageX,
                    y + messageFontHeight/6,
                    Graphics.TOP | Graphics.LEFT
                );
                g.drawLine(
                    messageFontHeight/4,
                    y + messageFontHeight/2,
                    imageX, // +1?
                    y + messageFontHeight/2
                );
                break;
            }
        }
    }
}
