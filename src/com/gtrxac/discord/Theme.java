package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;
// import com.nokia.mid.theme.*;

public class Theme {
    public static final int DARK = 0;
    public static final int LIGHT = 1;
    public static final int BLACK = 2;
//#ifdef OVER_100KB
    public static final int SYSTEM = 3;
    public static final int CUSTOM = 4;
//#endif

    public static int channelViewBackgroundColor;
    public static int channelViewEmptyTextColor;
    public static int timestampColor;
    public static int selectedTimestampColor;
    public static int linkColor;  // not used in system theme (links are same color as other text)
    public static int messageAuthorColor;
    public static int messageContentColor;
    public static int recipientMessageContentColor;
    public static int statusMessageContentColor;
    public static int selectedMessageBackgroundColor;
    public static int selectedMessageAuthorColor;
    public static int selectedMessageContentColor;
    public static int selectedRecipientMessageContentColor;
    public static int selectedStatusMessageContentColor;

    public static int embedBackgroundColor;
    public static int embedTitleColor;
    public static int embedDescriptionColor;
    public static int selectedEmbedBackgroundColor;
    public static int selectedEmbedTitleColor;
    public static int selectedEmbedDescriptionColor;

    public static int buttonBackgroundColor;
    public static int buttonTextColor;
    public static int selectedButtonBackgroundColor;
    public static int selectedButtonTextColor;

    public static int bannerBackgroundColor;
    public static int bannerTextColor;
    public static int outdatedBannerBackgroundColor;
    public static int outdatedBannerTextColor;
    public static int typingBannerBackgroundColor;
    public static int typingBannerTextColor;
    public static int unreadIndicatorBackgroundColor;
    public static int unreadIndicatorTextColor;
    public static int recipientMessageConnectorColor;

    public static int listBackgroundColor;
    public static int listTextColor;
    public static int listMutedTextColor;
    public static int listDescriptionTextColor;
    public static int listSelectedBackgroundColor;
    public static int listSelectedTextColor;
    public static int listSelectedMutedTextColor;
    public static int listSelectedDescriptionTextColor;
    public static int listNoItemsTextColor;
    public static int listIndicatorColor;
    
    public static int dialogBackgroundColor;
    public static int dialogTextColor;
    public static int emojiPickerBackgroundColor;
    public static int loadingScreenBackgroundColor;
    public static int loadingScreenTextColor;
    public static int keyMapperBackgroundColor;
    public static int keyMapperTextColor;
    public static int imagePreviewBackgroundColor;
    public static int imagePreviewTextColor;

    // these four are not used in system theme
    public static int subtextColor;
    public static int monospaceTextBackgroundColor;
    public static int forwardedTextColor;
    public static int editedTextColor;

    public static int scrollbarColor;
    public static int scrollbarHandleColor;

//#ifdef TOUCH_SUPPORT
    public static int messageBarBackgroundColor;
    public static int messageBarColor;
    public static int messageBarDraftColor;
//#endif

    //                                     Dark      Light     Black
    private static final int[] channelViewBackgroundColors = {0x313338, 0xFFFFFF, 0x000000};
    private static final int[] channelViewEmptyTextColors =  {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] timestampColors =  {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] selectedTimestampColors =  {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] messageAuthorColors =     {0xFFFFFF, 0x000000, 0xFFFFFF};
    private static final int[] messageContentColors =    {0xFFFFFF, 0x111111, 0xEEEEEE};
    private static final int[] recipientMessageContentColors = {0xDDDDDD, 0x333333, 0xCCCCCC};
    private static final int[] statusMessageContentColors =  {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] selectedMessageBackgroundColors =  {0x232428, 0xDDDDDD, 0x303030};
    private static final int[] selectedMessageAuthorColors =     {0xFFFFFF, 0x000000, 0xFFFFFF};
    private static final int[] selectedMessageContentColors =    {0xFFFFFF, 0x111111, 0xEEEEEE};
    private static final int[] selectedRecipientMessageContentColors = {0xDDDDDD, 0x333333, 0xCCCCCC};
    private static final int[] selectedStatusMessageContentColors =  {0xAAAAAA, 0x666666, 0x999999};

    private static final int[] embedBackgroundColors = {0x2b2d31, 0xEEEEEE, 0x202020};
    private static final int[] embedDescriptionColors =    {0xFFFFFF, 0x111111, 0xEEEEEE};
    private static final int[] selectedEmbedBackgroundColors =     {0x1e1f22, 0xCCCCCC, 0x404040};
    private static final int[] selectedEmbedDescriptionColors =     {0xFFFFFF, 0x111111, 0xEEEEEE};

    private static final int[] buttonBackgroundColors = {0x2b2d31, 0xEEEEEE, 0x202020};
    private static final int[] buttonTextColors =     {0xFFFFFF, 0x000000, 0xFFFFFF};
    private static final int[] selectedButtonBackgroundColors =     {0x1e1f22, 0xCCCCCC, 0x404040};
    private static final int[] selectedButtonTextColors =     {0xFFFFFF, 0x000000, 0xFFFFFF};

//#ifndef S40V2
    private static final int[] typingBannerBackgroundColors =     {0x1e1f22, 0xCCCCCC, 0x404040};
    private static final int[] typingBannerTextColors =     {0xFFFFFF, 0x000000, 0xFFFFFF};
//#endif

	private static final int[] listBackgroundColors      = {0x2b2d31, 0xffffff, 0x000000};
    private static final int[] listTextColors    = {0xdddddd, 0x222222, 0xdddddd};
	private static final int[] listMutedTextColors      = {0x888888, 0x888888, 0x888888};
    private static final int[] listDescriptionTextColors =  {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] listSelectedBackgroundColors      = {0x404249, 0xbbbbbb, 0x333333};
    private static final int[] listSelectedTextColors = {0xffffff, 0x000000, 0xffffff};
	private static final int[] listSelectedMutedTextColors      = {0x888888, 0x888888, 0x888888};
    private static final int[] listSelectedDescriptionTextColors =  {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] listNoItemsTextColors =  {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] listIndicatorColors = {0xffffff, 0x000000, 0xffffff};

    private static final int[] dialogBackgroundColors = {0x313338, 0xFFFFFF, 0x000000};
    private static final int[] dialogTextColors = {0xdddddd, 0x222222, 0xdddddd};
    private static final int[] emojiPickerBackgroundColors = {0x313338, 0xFFFFFF, 0x000000};
    private static final int[] loadingScreenBackgroundColors = {0x313338, 0xFFFFFF, 0x000000};
    private static final int[] loadingScreenTextColors = {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] keyMapperBackgroundColors = {0x313338, 0xFFFFFF, 0x000000};
    private static final int[] keyMapperTextColors = {0xFFFFFF, 0x111111, 0xEEEEEE};
    private static final int[] imagePreviewBackgroundColors = {0x313338, 0xFFFFFF, 0x000000};
    private static final int[] imagePreviewTextColors = {0xFFFFFF, 0x111111, 0xEEEEEE};

    private static final int[] subtextColors = {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] monospaceTextBackgroundColors = {0x1e1f22, 0xCCCCCC, 0x404040};
    private static final int[] forwardedTextColors = {0xAAAAAA, 0x666666, 0x999999};
    private static final int[] editedTextColors = {0xAAAAAA, 0x666666, 0x999999};

    private static final int[] scrollbarColors = {0xDDDDDD, 0xDDDDDD, 0xDDDDDD};
    private static final int[] scrollbarHandleColors = {0x888888, 0x888888, 0x888888};

//#ifdef TOUCH_SUPPORT
    private static final int[] messageBarBackgroundColors = {0x4d5055, 0xFFFFFF, 0x181818};
    private static final int[] messageBarColors = {0xDDDDDD, 0x444444, 0xAAAAAA};
    private static final int[] messageBarDraftColors = {0xFFFFFF, 0x111111, 0xEEEEEE};
//#endif
    
	public static void loadPresetTheme() {
        channelViewBackgroundColor = channelViewBackgroundColors[Settings.theme];
        channelViewEmptyTextColor = channelViewEmptyTextColors[Settings.theme];
        timestampColor = timestampColors[Settings.theme];
        selectedTimestampColor = selectedTimestampColors[Settings.theme];
        linkColor = 0x00AAFC;
        messageAuthorColor = messageAuthorColors[Settings.theme];
        messageContentColor = messageContentColors[Settings.theme];
        recipientMessageContentColor = recipientMessageContentColors[Settings.theme];
        statusMessageContentColor = statusMessageContentColors[Settings.theme];
        selectedMessageBackgroundColor = selectedMessageBackgroundColors[Settings.theme];
        selectedMessageAuthorColor = selectedMessageAuthorColors[Settings.theme];
        selectedMessageContentColor = selectedMessageContentColors[Settings.theme];
        selectedRecipientMessageContentColor = selectedRecipientMessageContentColors[Settings.theme];
        selectedStatusMessageContentColor = selectedStatusMessageContentColors[Settings.theme];

        embedBackgroundColor = embedBackgroundColors[Settings.theme];
        embedTitleColor = 0x00AAFC;
        embedDescriptionColor = embedDescriptionColors[Settings.theme];
        selectedEmbedBackgroundColor = selectedEmbedBackgroundColors[Settings.theme];
        selectedEmbedTitleColor = 0x00AAFC;
        selectedEmbedDescriptionColor = selectedEmbedDescriptionColors[Settings.theme];

        buttonBackgroundColor = buttonBackgroundColors[Settings.theme];
        buttonTextColor = buttonTextColors[Settings.theme];
        selectedButtonBackgroundColor = selectedButtonBackgroundColors[Settings.theme];
        selectedButtonTextColor = selectedButtonTextColors[Settings.theme];

//#ifdef S40V2
        // White background on banners on S40v2 so it blends into the system's title bar
        typingBannerBackgroundColor = 0xFFFFFF;
        typingBannerTextColor = 0x000000;
//#else
        typingBannerBackgroundColor = typingBannerBackgroundColors[Settings.theme];
        typingBannerTextColor = typingBannerTextColors[Settings.theme];
//#endif
        unreadIndicatorBackgroundColor = 0xF23F43;
        unreadIndicatorTextColor = 0xFFFFFF;
        recipientMessageConnectorColor = 0x666666;

        listBackgroundColor = listBackgroundColors[Settings.theme];
        listTextColor = listTextColors[Settings.theme];
        listMutedTextColor = listMutedTextColors[Settings.theme];
        listDescriptionTextColor = listDescriptionTextColors[Settings.theme];
        listSelectedBackgroundColor = listSelectedBackgroundColors[Settings.theme];
        listSelectedTextColor = listSelectedTextColors[Settings.theme];
        listSelectedMutedTextColor = listSelectedMutedTextColors[Settings.theme];
        listSelectedDescriptionTextColor = listSelectedDescriptionTextColors[Settings.theme];
        listNoItemsTextColor = listNoItemsTextColors[Settings.theme];
        listIndicatorColor = listIndicatorColors[Settings.theme];

        dialogBackgroundColor = dialogBackgroundColors[Settings.theme];
        dialogTextColor = dialogTextColors[Settings.theme];
        emojiPickerBackgroundColor = emojiPickerBackgroundColors[Settings.theme];
        loadingScreenBackgroundColor = loadingScreenBackgroundColors[Settings.theme];
        loadingScreenTextColor = loadingScreenTextColors[Settings.theme];
        keyMapperBackgroundColor = keyMapperBackgroundColors[Settings.theme];
        keyMapperTextColor = keyMapperTextColors[Settings.theme];
        imagePreviewBackgroundColor = imagePreviewBackgroundColors[Settings.theme];
        imagePreviewTextColor = imagePreviewTextColors[Settings.theme];

        subtextColor = subtextColors[Settings.theme];
        monospaceTextBackgroundColor = monospaceTextBackgroundColors[Settings.theme];
        forwardedTextColor = forwardedTextColors[Settings.theme];
        editedTextColor = editedTextColors[Settings.theme];

        scrollbarColor = scrollbarColors[Settings.theme];
        scrollbarHandleColor = scrollbarHandleColors[Settings.theme];

//#ifdef TOUCH_SUPPORT
        messageBarBackgroundColor = messageBarBackgroundColors[Settings.theme];
        messageBarColor = messageBarColors[Settings.theme];
        messageBarDraftColor = messageBarDraftColors[Settings.theme];
//#endif
	}

//#ifdef OVER_100KB
	public static void loadSystemTheme() {
        // int bg = ThemeManager.getCurrentTheme().getColor(Theme.COLOR_BACKGROUND);
        int bg = App.disp.getColor(Display.COLOR_BACKGROUND);
        int fg = App.disp.getColor(Display.COLOR_FOREGROUND);
        int hbg = App.disp.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND);
        int hfg = App.disp.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND);

        // fallback color scheme for devices where Display.getColor does not return valid colors (e.g. all black)
        if (fg == bg || hfg == hbg) {
            fg = 0x000000;
            bg = 0xFFFFFF;
            hfg = 0xFFFFFF;
            hbg = 0x2211CC;
        }

        // if (Util.contrast(hbg, hfg) < 255) {
        //     hbg = fg;
        //     hfg = bg;
        // }

        int secondaryText = Util.blend(fg, bg, 7);

        channelViewBackgroundColor = bg;
        timestampColor = secondaryText;
        selectedTimestampColor = Util.blend(hfg, hbg, 7);
        messageAuthorColor = fg;
        messageContentColor = fg;
        recipientMessageContentColor = secondaryText;
        statusMessageContentColor = secondaryText;
        selectedMessageBackgroundColor = hbg;
        selectedMessageAuthorColor = hfg;
        selectedMessageContentColor = hfg;
        selectedRecipientMessageContentColor = hfg;
        selectedStatusMessageContentColor = hfg;

        embedBackgroundColor = bg;
        embedTitleColor = fg;
        embedDescriptionColor = fg;
        selectedEmbedBackgroundColor = Util.blend(bg, hbg, 7);
        selectedEmbedTitleColor = fg;
        selectedEmbedDescriptionColor = fg;

        buttonBackgroundColor = bg;
        buttonTextColor = fg;
        selectedButtonBackgroundColor = hbg;
        selectedButtonTextColor = hfg;

        typingBannerBackgroundColor = bg;
        typingBannerTextColor = fg;
        unreadIndicatorBackgroundColor = 0xF23F43;
        unreadIndicatorTextColor = 0xFFFFFF;
        recipientMessageConnectorColor = recipientMessageContentColor;

        // if (Util.contrast(unreadIndicatorBackgroundColor, bg) < 300) {
        //     unreadIndicatorBackgroundColor = fg;
        //     unreadIndicatorTextColor = bg;
        // }

        listBackgroundColor = bg;
        listTextColor = fg;
        listMutedTextColor = Util.blend(fg, bg, 6);
        listDescriptionTextColor = secondaryText;
        listSelectedBackgroundColor = hbg;
        listSelectedTextColor = hfg;
        listSelectedMutedTextColor = Util.blend(hfg, hbg, 6);
        listSelectedDescriptionTextColor = Util.blend(hfg, hbg, 8);
        listNoItemsTextColor = fg;
        listIndicatorColor = fg;
    
        dialogBackgroundColor = bg;
        dialogTextColor = fg;
        emojiPickerBackgroundColor = bg;
        loadingScreenBackgroundColor = bg;
        loadingScreenTextColor = secondaryText;
        keyMapperBackgroundColor = bg;
        keyMapperTextColor = fg;
        imagePreviewBackgroundColor = bg;
        imagePreviewTextColor = fg;

        // check for bad contrast in dialogs (especially on s40 where bg seems to always be white)
        // use highlighted colors if it noticeably improves contrast
        if (Util.contrast(bg, fg) + 100 < Util.contrast(hbg, hfg)) {
            dialogBackgroundColor = hbg;
            dialogTextColor = hfg;
        }

        scrollbarColor = bg;
        scrollbarHandleColor = hbg;

//#ifdef TOUCH_SUPPORT
        messageBarBackgroundColor = bg;
        messageBarColor = fg;
        messageBarDraftColor = fg;
//#endif
    }

    public static void loadJsonRmsTheme() {
        String jsonStr;
        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("theme", false);
            jsonStr = Util.bytesToString(rms.getRecord(1));
        }
        catch (Exception e) {
            jsonStr = "{}";
        }

        Util.closeRecordStore(rms);

        loadJsonTheme(JSON.getObject(jsonStr));
    }
    
    public static void loadJsonTheme(JSONObject data) {
        // load dark theme as a base
        Settings.theme = Theme.DARK;
        loadPresetTheme();
        Settings.theme = Theme.CUSTOM;

        channelViewBackgroundColor = jsonGetHex(data, "channelViewBackground", channelViewBackgroundColor);
        channelViewEmptyTextColor = jsonGetHex(data, "channelViewEmptyText", channelViewEmptyTextColor);
        timestampColor = jsonGetHex(data, "timestamp", timestampColor);
        selectedTimestampColor = jsonGetHex(data, "selectedTimestamp", selectedTimestampColor);
        linkColor = jsonGetHex(data, "link", linkColor);
        messageAuthorColor = jsonGetHex(data, "messageAuthor", messageAuthorColor);
        messageContentColor = jsonGetHex(data, "messageContent", messageContentColor);
        recipientMessageContentColor = jsonGetHex(data, "recipientMessageContent", recipientMessageContentColor);
        statusMessageContentColor = jsonGetHex(data, "statusMessageContent", statusMessageContentColor);
        selectedMessageBackgroundColor = jsonGetHex(data, "selectedMessageBackground", selectedMessageBackgroundColor);
        selectedMessageAuthorColor = jsonGetHex(data, "selectedMessageAuthor", selectedMessageAuthorColor);
        selectedMessageContentColor = jsonGetHex(data, "selectedMessageContent", selectedMessageContentColor);
        selectedRecipientMessageContentColor = jsonGetHex(data, "selectedRecipientMessageContent", selectedRecipientMessageContentColor);
        selectedStatusMessageContentColor = jsonGetHex(data, "selectedStatusMessageContent", selectedStatusMessageContentColor);
        embedBackgroundColor = jsonGetHex(data, "embedBackground", embedBackgroundColor);
        embedTitleColor = jsonGetHex(data, "embedTitle", embedTitleColor);
        embedDescriptionColor = jsonGetHex(data, "embedDescription", embedDescriptionColor);
        selectedEmbedBackgroundColor = jsonGetHex(data, "selectedEmbedBackground", selectedEmbedBackgroundColor);
        selectedEmbedTitleColor = jsonGetHex(data, "selectedEmbedTitle", selectedEmbedTitleColor);
        selectedEmbedDescriptionColor = jsonGetHex(data, "selectedEmbedDescription", selectedEmbedDescriptionColor);
        buttonBackgroundColor = jsonGetHex(data, "buttonBackground", buttonBackgroundColor);
        buttonTextColor = jsonGetHex(data, "buttonText", buttonTextColor);
        selectedButtonBackgroundColor = jsonGetHex(data, "selectedButtonBackground", selectedButtonBackgroundColor);
        selectedButtonTextColor = jsonGetHex(data, "selectedButtonText", selectedButtonTextColor);
        bannerBackgroundColor = jsonGetHex(data, "bannerBackground", bannerBackgroundColor);
        bannerTextColor = jsonGetHex(data, "bannerText", bannerTextColor);
        outdatedBannerBackgroundColor = jsonGetHex(data, "outdatedBannerBackground", outdatedBannerBackgroundColor);
        outdatedBannerTextColor = jsonGetHex(data, "outdatedBannerText", outdatedBannerTextColor);
        typingBannerBackgroundColor = jsonGetHex(data, "typingBannerBackground", typingBannerBackgroundColor);
        typingBannerTextColor = jsonGetHex(data, "typingBannerText", typingBannerTextColor);
        unreadIndicatorBackgroundColor = jsonGetHex(data, "unreadIndicatorBackground", unreadIndicatorBackgroundColor);
        unreadIndicatorTextColor = jsonGetHex(data, "unreadIndicatorText", unreadIndicatorTextColor);
        recipientMessageConnectorColor = jsonGetHex(data, "recipientMessageConnector", recipientMessageConnectorColor);
        listBackgroundColor = jsonGetHex(data, "listBackground", listBackgroundColor);
        listTextColor = jsonGetHex(data, "listText", listTextColor);
        listMutedTextColor = jsonGetHex(data, "listMutedText", listMutedTextColor);
        listDescriptionTextColor = jsonGetHex(data, "listDescriptionText", listDescriptionTextColor);
        listSelectedBackgroundColor = jsonGetHex(data, "listSelectedBackground", listSelectedBackgroundColor);
        listSelectedTextColor = jsonGetHex(data, "listSelectedText", listSelectedTextColor);
        listSelectedMutedTextColor = jsonGetHex(data, "listSelectedMutedText", listSelectedMutedTextColor);
        listSelectedDescriptionTextColor = jsonGetHex(data, "listSelectedDescriptionText", listSelectedDescriptionTextColor);
        listNoItemsTextColor = jsonGetHex(data, "listNoItemsText", listNoItemsTextColor);
        listIndicatorColor = jsonGetHex(data, "listIndicator", listIndicatorColor);
        dialogBackgroundColor = jsonGetHex(data, "dialogBackground", dialogBackgroundColor);
        dialogTextColor = jsonGetHex(data, "dialogText", dialogTextColor);
        emojiPickerBackgroundColor = jsonGetHex(data, "emojiPickerBackground", emojiPickerBackgroundColor);
        loadingScreenBackgroundColor = jsonGetHex(data, "loadingScreenBackground", loadingScreenBackgroundColor);
        loadingScreenTextColor = jsonGetHex(data, "loadingScreenText", loadingScreenTextColor);
        keyMapperBackgroundColor = jsonGetHex(data, "keyMapperBackground", keyMapperBackgroundColor);
        keyMapperTextColor = jsonGetHex(data, "keyMapperText", keyMapperTextColor);
        imagePreviewBackgroundColor = jsonGetHex(data, "imagePreviewBackground", imagePreviewBackgroundColor);
        imagePreviewTextColor = jsonGetHex(data, "imagePreviewText", imagePreviewTextColor);
        subtextColor = jsonGetHex(data, "subtext", subtextColor);
        monospaceTextBackgroundColor = jsonGetHex(data, "monospaceTextBackground", monospaceTextBackgroundColor);
        forwardedTextColor = jsonGetHex(data, "forwardedText", forwardedTextColor);
        editedTextColor = jsonGetHex(data, "editedText", editedTextColor);
        scrollbarColor = jsonGetHex(data, "scrollbar", scrollbarColor);
        scrollbarHandleColor = jsonGetHex(data, "scrollbarHandle", scrollbarHandleColor);
//#ifdef TOUCH_SUPPORT
        messageBarBackgroundColor = jsonGetHex(data, "messageBarBackground", messageBarBackgroundColor);
        messageBarColor = jsonGetHex(data, "messageBarText", messageBarColor);
        messageBarDraftColor = jsonGetHex(data, "messageBarDraft", messageBarDraftColor);
//#endif
    }

    private static int jsonGetHex(JSONObject data, String key, int def) {
        try {
            return Integer.parseInt(data.getString(key), 16);
        }
        catch (JSONException e) {
            // key not found - use default color (color from dark theme)
            return def;
        }
        catch (NumberFormatException e) {
            // invalid color - use red to highlight it
            return 0xFF0000;
        }
    }
//#endif

	public static void load() {
//#ifdef S40V2
        // White background on banners on S40v2 so it blends into the system's title bar
        bannerBackgroundColor = 0xFFFFFF;
        bannerTextColor = 0x000000;
//#else
        bannerBackgroundColor = 0x5865F2;
        bannerTextColor = 0xFFFFFF;
//#endif

        outdatedBannerBackgroundColor = 0xAA1122;
        outdatedBannerTextColor = 0xFFFFFF;

//#ifdef OVER_100KB
        if (Settings.theme == Theme.SYSTEM) {
            loadSystemTheme();
        }
        else if (Settings.theme == Theme.CUSTOM) {
            loadJsonRmsTheme();
        }
        else
//#endif
        loadPresetTheme();
    }
}