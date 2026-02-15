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
//#ifdef SAMSUNG
    Image refImg2;  // second (right-side) part of ref message image
//#endif
//#ifdef OVER_100KB
    FormattedString refImgFormatStr;
//#endif
    boolean refImgHasPfp;  // cached ref image has profile picture loaded
    boolean refImgHasColor;  // cached ref image has name color loaded
    boolean refImgSelected;  // was the cached ref image selected? used for determining correct background color
    long refImgLastDrawn;    // timestamp when ref image was last drawn, used for preventing too frequent redraws

    private static Font smallFont;
    private static Font smallBoldFont;

    public ChannelViewItem(int type) {
        this.type = type;
    }

    private static Image unreadIndicatorImage;

    public static int drawUnreadIndicatorImage(Graphics directG, int baseX, int baseY) {
        Font font = (directG == null) ? App.titleFont : smallFont;
        int fontHeight = (font.getHeight() + 1)/2*2;
        int stringWidth = font.stringWidth(Locale.get(NEW_MARKER));
        int totalWidth = fontHeight/2 + stringWidth + fontHeight/4;

        Graphics g = directG;
        if (g == null) {
            unreadIndicatorImage = Image.createImage(totalWidth, fontHeight);
            g = unreadIndicatorImage.getGraphics();

            g.setColor(Theme.channelViewBackgroundColor);
            g.fillRect(0, 0, totalWidth, fontHeight);
        } else {
            baseX -= totalWidth;
            g.translate(baseX, baseY);
        }

        g.setColor(Theme.unreadIndicatorBackgroundColor);
        g.fillRect(
            fontHeight/2,
            0,
            stringWidth + fontHeight/4,
            (directG == null) ? fontHeight : (fontHeight + 1)  // +1 to hide triangle rendering error on s40
        );
        g.fillTriangle(
            fontHeight/2,
            0,
            fontHeight/2,
            fontHeight,
            0,
            fontHeight/2
        );
        g.setColor(Theme.unreadIndicatorTextColor);
        g.setFont(font);
        g.drawString(
            Locale.get(NEW_MARKER),
            fontHeight/2 + 1,
            1,
            Graphics.TOP | Graphics.LEFT
        );

        if (directG == null) {
            unreadIndicatorImage = Util.resizeImageBilinear(unreadIndicatorImage, totalWidth*2/3, fontHeight*2/3);
        } else {
            g.translate(-baseX, -baseY);
        }
        return fontHeight;
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
//#ifdef OVER_100KB
                    msg.contentFormatted.height +
//#else
                    messageFontHeight*msg.contentLines.length +
//#endif
                    messageFontHeight/4;
    
                // One line for message author
                if (msg.showAuthor) result += App.authorFont.getHeight();
    
                // Each embed's height + top margin
                if (msg.embeds != null) {
                    for (int i = 0; i < msg.embeds.length; i++) {
                        result += msg.embeds[i].getHeight(messageFontHeight) + messageFontHeight/4;
                    }
                }
    
                // Referenced message if message is a reply and option is enabled
                if (msg.recipient != null && Settings.showRefMessage) {
                    result += getRefAreaHeight(messageFontHeight, shouldUseDirectRefMessage()) + messageFontHeight/4;
                }
    
                return result;
            }

            case UNREAD_INDICATOR: {
                if (shouldUseDirectRefMessage()) return smallFont.getHeight() + 1 + messageFontHeight/6*2;
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

    public static boolean shouldUseDirectRefMessage() {
//#ifdef OVER_100KB
        final boolean result = (
//#ifdef NOKIA_THEME_BACKGROUND
            Settings.theme == Theme.SYSTEM ||
//#endif
            Settings.messageFontSize != 0
        );

        if (result && smallFont == null) {
            smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            smallBoldFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        }
        return result;
//#else
        return false;
//#endif
    }

    private int getRefAreaHeight(int messageFontHeight, boolean directRefMessage) {
        if (directRefMessage) {
            return smallFont.getHeight()*5/4;
        } else {
            return messageFontHeight;
        }
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
                    g.setColor(Theme.selectedMessageBackgroundColor);
                    g.fillRect(0, y, width, getHeight());
                }
                
                y += messageFontHeight/8;

                if (msg.showAuthor) {
                    int defaultAuthorColor = selected ? Theme.selectedMessageAuthorColor : Theme.messageAuthorColor;
                    int recipientColor = 0;
                    
                    if (msg.recipient != null) {
                        boolean hasColor = NameColorCache.has(msg.recipient, true);
                        
                        recipientColor = NameColorCache.get(msg.recipient.id);
                        if (recipientColor == 0) {
                            recipientColor = defaultAuthorColor;
                        }

                        // Draw referenced message if option is enabled
                        if (Settings.showRefMessage) {
                            y += messageFontHeight/4;

                            // Should referenced message be drawn directly or onto an image that is then downscaled?
                            // Drawing directly means we cannot use a smaller font size, but should be faster and supports system theme transparency, so use it in system theme mode and if a larger font is in use
                            // Drawing onto an image lets us use a smaller font than what J2ME normally allows
                            final boolean directRefMessage = shouldUseDirectRefMessage();

                            // Determine refmessage area's height based on the font size and whether we are direct drawing
                            int refAreaHeight = getRefAreaHeight(messageFontHeight, directRefMessage);

                            if (directRefMessage || shouldRedrawRefMessage(selected)) {
//#ifdef OVER_100KB
                                boolean useFormattedString =
//#ifdef EMOJI_SUPPORT
                                    FormattedString.emojiMode != FormattedString.EMOJI_MODE_OFF ||
//#endif
                                    FormattedString.useMarkdown;
//#endif

                                Graphics refG;
                                int refX, refY, formatStrWidth;
                                Font refFont, refFontBold;
                                int refFontHeight;

                                Image refImgFull = null;
                                int refImgFullWidth = 0;
                                int refImgFull2Width = 0;
                                int refImgBackgroundColor = 0;
                                
                                if (!directRefMessage) {
                                    refFont = App.messageFont;
                                    refFontBold = App.titleFont;
                                    refFontHeight = messageFontHeight;

                                    // if drawing to an image, set vars that can be used to determine if the image needs to be redrawn when pfp/name color get loaded
                                    refImgHasPfp =
                                        !useIcons || Settings.pfpSize == Settings.PFP_SIZE_PLACEHOLDER ||
                                        msg.recipient.getIconHash() == null ||
                                        IconCache.hasResized(msg.recipient, refFontHeight);

                                    refImgHasColor = hasColor;

                                    // Create an image where the ref message will be rendered.
                                    // This will then be downscaled, giving us a smaller font size than what J2ME normally allows.
                                    refImgFullWidth = (width - refDrawX)*4/3;
                                    // On Samsung (JBlend), the image has to be drawn in two parts due to a bug
//#ifdef SAMSUNG
                                    refImgFull2Width = refImgFullWidth - width;
                                    refImgFullWidth = width;
//#endif
                                    refImgFull = Image.createImage(refImgFullWidth, refFontHeight);
                                    refG = refImgFull.getGraphics();

                                    // Fill ref message image with the same background color that the rest of the message has
                                    refImgBackgroundColor = selected ?
                                        Theme.selectedMessageBackgroundColor : Theme.channelViewBackgroundColor;
                                    refG.setColor(refImgBackgroundColor);
                                    refG.fillRect(0, 0, refImgFullWidth, refFontHeight);

                                    refX = 0;
                                    refY = 0;
                                    formatStrWidth = refImgFullWidth;
                                } else {
                                    // draw refmessage contents directly onto the screen:
                                    // use screen as the graphics target, use small font regardless of settings, and set correct base screen coordinates
                                    refFont = smallFont;
                                    refFontBold = smallBoldFont;
                                    refFontHeight = smallFont.getHeight();
                                    refG = g;
                                    refX = refDrawX;
                                    refY = y;
                                    formatStrWidth = width;
                                }

                                if (useIcons) {
                                    Image icon = IconCache.getResized(msg.recipient, refFontHeight);

                                    if (icon != null) {
                                        refG.drawImage(icon, refX, refY, Graphics.TOP | Graphics.LEFT);
                                    } else {
                                        refG.setColor(msg.recipient.iconColor);

                                        if (Settings.pfpType == Settings.PFP_TYPE_CIRCLE || Settings.pfpType == Settings.PFP_TYPE_CIRCLE_HQ) {
                                            refG.fillArc(refX, refY, refFontHeight, refFontHeight, 0, 360);
                                        } else {
                                            refG.fillRect(refX, refY, refFontHeight, refFontHeight);
                                        }
                                    }

                                    refX += refFontHeight;
                                }
                                refX += refFontHeight/3;

                                refG.setFont(refFontBold);
                                refG.setColor(recipientColor);
                                refG.drawString(msg.recipient.name, refX, refY, Graphics.TOP | Graphics.LEFT);

                                refX += refFontBold.stringWidth(msg.recipient.name) + refFontHeight/3;
                                
                                int refImgTextColor = selected ?
                                    Theme.selectedRecipientMessageContentColor : Theme.recipientMessageContentColor;
                                refG.setFont(refFont);
                                refG.setColor(refImgTextColor);
//#ifdef OVER_100KB
                                if (useFormattedString) {
                                    if (refImgFormatStr == null) {
                                        refImgFormatStr = new FormattedString(msg.refContent, refFont, formatStrWidth, refX, true, false, false);
                                    }
                                    refImgFormatStr.draw(refG, refY);
                                } else
//#endif
                                {
                                    refG.drawString(msg.refContent, refX, refY, Graphics.TOP | Graphics.LEFT);
                                }

                                if (!directRefMessage) {
                                    int refImgResizeWidth = width - refDrawX;
//#ifdef SAMSUNG
                                    refImgResizeWidth = width*3/4;
//#endif
                                    refImg = Util.resizeImageBilinear(refImgFull, refImgResizeWidth, refFontHeight*3/4);

//#ifdef SAMSUNG
                                    Image refImgFull2 = Image.createImage(refImgFull2Width, refFontHeight);
                                    refG = refImgFull2.getGraphics();

                                    refG.setColor(refImgBackgroundColor);
                                    refG.fillRect(0, 0, refImgFull2Width, refFontHeight);

                                    refG.setFont(refFont);
                                    refG.setColor(refImgTextColor);
//#ifdef OVER_100KB
                                    if (useFormattedString) {
                                        refG.translate(-refImgFullWidth, 0);
                                        refImgFormatStr.draw(refG, 0);
                                    } else
//#endif
                                    {
                                        refG.drawString(msg.refContent, -refImgFullWidth + refX, 0, Graphics.TOP | Graphics.LEFT);
                                    }

                                    refImg2 = Util.resizeImageBilinear(refImgFull2, refImgFull2Width*3/4, refFontHeight*3/4);
//#endif

                                    refImgSelected = selected;
                                    refImgLastDrawn = System.currentTimeMillis();
                                }
                            }

                            if (!directRefMessage) {
                                // draw downscaled refmessage image on screen
                                g.drawImage(refImg, refDrawX, y, Graphics.TOP | Graphics.LEFT);
//#ifdef SAMSUNG
                                g.drawImage(refImg2, refDrawX + width*3/4, y, Graphics.TOP | Graphics.LEFT);
//#endif
                            }

                            // draw connecting line between refmessage and message
                            y += refAreaHeight*3/8;
                            g.setColor(Theme.recipientMessageConnectorColor);
                            g.drawLine(refDrawX/2, y, refDrawX/2, y + refAreaHeight/2);  // vertical line |
                            g.drawLine(refDrawX/2, y, refDrawX*7/8, y);  // horizontal line -
                            y += refAreaHeight*5/8;
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

                            g.setColor(0xFFFFFF);
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
                    if (authorColor == 0) {
                        authorColor = defaultAuthorColor;
                    }
                    
                    int authorX = x;

                    g.setColor(authorColor);
                    g.setFont(App.authorFont);
                    g.drawString(msg.author.name, authorX, y, Graphics.TOP | Graphics.LEFT);

                    authorX += App.authorFont.stringWidth(msg.author.name);

                    if (msg.recipient != null && !Settings.showRefMessage) {
                        g.setFont(App.timestampFont);
                        g.setColor(defaultAuthorColor);
                        g.drawString(" -> ", authorX, y, Graphics.TOP | Graphics.LEFT);
                        
                        authorX += App.timestampFont.stringWidth(" -> ");

                        g.setFont(App.authorFont);
                        g.setColor(recipientColor);
                        g.drawString(msg.recipient.name, authorX, y, Graphics.TOP | Graphics.LEFT);

                        authorX += App.authorFont.stringWidth(msg.recipient.name);
                    }

                    // Draw timestamp
                    g.setColor(selected ? Theme.selectedTimestampColor : Theme.timestampColor);
                    g.setFont(App.timestampFont);
                    g.drawString("  " + msg.timestamp, authorX, y, Graphics.TOP | Graphics.LEFT);
                    y += App.authorFont.getHeight();
                }

                // Draw message content
                // Use timestamp color for status messages to distinguish them
                if (msg.isStatus) {
                    g.setColor(selected ? Theme.selectedStatusMessageContentColor : Theme.statusMessageContentColor);
                } else {
                    g.setColor(selected ? Theme.selectedMessageContentColor : Theme.messageContentColor);
                }
                g.setFont(App.messageFont);
//#ifdef OVER_100KB
                msg.contentFormatted.draw(g, y);
                y += msg.contentFormatted.height;
//#else
                for (int i = 0; i < msg.contentLines.length; i++) {
                    g.drawString(msg.contentLines[i], x, y, Graphics.TOP | Graphics.LEFT);
                    y += messageFontHeight;
                }
//#endif

                // Draw embeds
                if (msg.embeds != null) {
                    for (int i = 0; i < msg.embeds.length; i++) {
                        Embed emb = msg.embeds[i];
                        y += messageFontHeight/4; // Top margin

                        g.setColor(selected ? Theme.selectedEmbedBackgroundColor : Theme.embedBackgroundColor);
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
                            g.setColor(selected ? Theme.selectedEmbedTitleColor : Theme.embedTitleColor);
                            g.setFont(App.titleFont);
//#ifdef OVER_100KB
                            emb.titleFormatted.draw(g, y);
                            y += emb.titleFormatted.height;
//#else
                            for (int l = 0; l < emb.titleLines.length; l++) {
                                g.drawString(emb.titleLines[l], x, y, Graphics.TOP|Graphics.LEFT);
                                y += messageFontHeight;
                            }
//#endif
                            // Spacing between title and desc
                            if (emb.description != null) y += messageFontHeight/4;
                        }

                        if (emb.description != null) {
                            g.setColor(selected ? Theme.selectedEmbedDescriptionColor : Theme.embedDescriptionColor);
                            g.setFont(App.messageFont);
//#ifdef OVER_100KB
                            emb.descFormatted.draw(g, y);
                            y += emb.descFormatted.height;
//#else
                            for (int l = 0; l < emb.descLines.length; l++) {
                                g.drawString(emb.descLines[l], x, y, Graphics.TOP|Graphics.LEFT);
                                y += messageFontHeight;
                            }
//#endif
                        }

                        x -= messageFontHeight/3;  // Undo left padding
                        y += messageFontHeight/3;  // Bottom padding
                    }
                }
                break;
            }

            case OLDER_BUTTON:
            case NEWER_BUTTON: {
                String caption = (type == OLDER_BUTTON) ?
                    Locale.get(VIEW_OLDER_MESSAGES_L) : Locale.get(VIEW_NEWER_MESSAGES_L);

                int textWidth = App.messageFont.stringWidth(caption);
                g.setColor(selected ? Theme.selectedButtonBackgroundColor : Theme.buttonBackgroundColor);

//#ifdef OVER_100KB
                int rectX = width/2 - textWidth/2 - messageFontHeight;
                int rectY = y + messageFontHeight/6;
                int rectWidth = textWidth + messageFontHeight*2;
                int rectHeight = messageFontHeight*4/3;
                int rounding = messageFontHeight/2;

                if (selected || Settings.theme != Theme.SYSTEM) {
                    g.setColor(selected ? Theme.selectedButtonBackgroundColor : Theme.buttonBackgroundColor);
                    g.fillRoundRect(rectX, rectY, rectWidth, rectHeight, rounding, rounding);
                } else {
                    g.setColor(Theme.buttonTextColor);
                    g.drawRoundRect(rectX, rectY, rectWidth, rectHeight, rounding, rounding);
                }
//#else
                g.setColor(selected ? Theme.selectedButtonBackgroundColor : Theme.buttonBackgroundColor);
                g.fillRoundRect(
                    width/2 - textWidth/2 - messageFontHeight,
                    y + messageFontHeight/6,
                    textWidth + messageFontHeight*2,
                    messageFontHeight*4/3,
                    messageFontHeight/2,
                    messageFontHeight/2
                );
//#endif

                g.setFont(App.messageFont);
                g.setColor(selected ? Theme.selectedButtonTextColor : Theme.buttonTextColor);
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
                    msg.attachments.length +
                    (msg.attachments.length > 1 ?
                        Locale.get(VIEW_ATTACHMENTS_SUFFIX) :
                        Locale.get(VIEW_ATTACHMENT_SUFFIX)
                    );

                int x = useIcons ? messageFontHeight*2 : 0;
                int textWidth = App.messageFont.stringWidth(caption);

//#ifdef OVER_100KB
                int rectX = x + messageFontHeight/2;
                int rectY = y + messageFontHeight/6;
                int rectWidth = textWidth + messageFontHeight;
                int rectHeight = messageFontHeight*4/3;
                int rounding = messageFontHeight/2;

                if (selected || Settings.theme != Theme.SYSTEM) {
                    g.setColor(selected ? Theme.selectedButtonBackgroundColor : Theme.buttonBackgroundColor);
                    g.fillRoundRect(rectX, rectY, rectWidth, rectHeight, rounding, rounding);
                } else {
                    g.setColor(Theme.buttonTextColor);
                    g.drawRoundRect(rectX, rectY, rectWidth, rectHeight, rounding, rounding);
                }
//#else
                g.setColor(selected ? Theme.selectedButtonBackgroundColor : Theme.buttonBackgroundColor);
                g.fillRoundRect(
                    x + messageFontHeight/2,
                    y + messageFontHeight/6,
                    textWidth + messageFontHeight,
                    messageFontHeight*4/3,
                    messageFontHeight/2,
                    messageFontHeight/2
                );
//#endif

                g.setFont(App.messageFont);
                g.setColor(selected ? Theme.selectedButtonTextColor : Theme.buttonTextColor);
                g.drawString(caption, x + messageFontHeight, y + messageFontHeight/3, Graphics.TOP | Graphics.LEFT);
                break;
            }

            case UNREAD_INDICATOR: {
//#ifdef NOKIA_THEME_BACKGROUND
                final boolean directDraw = (Settings.theme == Theme.SYSTEM);

                if (directDraw && smallFont == null) {
                    smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                    smallBoldFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
                }
//#else
                final boolean directDraw = false;
//#endif
                
                Font font = (directDraw ? smallFont : App.titleFont);
                int height = (font.getHeight() + 1)/2*2;

                int end = width - messageFontHeight/4;
                if (directDraw) y += messageFontHeight/6;

                g.setColor(Theme.unreadIndicatorBackgroundColor);
                g.drawLine(
                    messageFontHeight/4,
                    y + height/2,
                    end - 1,
                    y + height/2
                );

                if (directDraw) {
                    drawUnreadIndicatorImage(g, end, y);
                } else {
                    g.drawImage(
                        unreadIndicatorImage,
                        end - unreadIndicatorImage.getWidth(),
                        y + messageFontHeight/6,
                        Graphics.TOP | Graphics.LEFT
                    );
                }
                break;
            }
        }
    }
}
