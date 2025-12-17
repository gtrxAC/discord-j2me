package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsScreen extends ListScreen implements CommandListener, Strings {
    // Rough(?) guide on how to add a new option for changing a setting:
    // - make sure you have created the setting itself (Settings.java)
    // - see if there is a suitable icon for that setting in Icons.java or icons.png
    //   - reusing icons is fine but don't overdo it or make it too obvious (ideally not e.g. next to each other on the same screen)
    //   - if no suitable icon, create a new icon or ask for a new icon to be created, then put an entry for it in Icons.java
    // - add a new entry to the array initialization in loadSettings: "settings = new Setting[][]"
    //   - you may have to create new arrays for valuelabels or icons used by the new setting, those are listed right above that initialization
    // - if it makes sense from an UI perspective, generally best to add new settings to the END of a section
    //   - if you're adding IN BETWEEN existing items of a section, find all comments in SettingsSectionScreen that are "// SPECIAL CASE" and make sure the indexes used in those special cases are still correct for the settings in question
    //   - also make sure the correct index is used in KeyMapper (search "SettingsSectionScreen.settings" there)
    // - commandAction method: add code to save that setting's value into the Settings field

    private Command saveCommand;
    private Command cancelCommand;

    public static String[] sectionNames;

    SettingsScreen() {
        super(Locale.get(SETTINGS_FORM_TITLE), false, false, false);
        setCommandListener(this);

        sectionNames = new String[] {
            Locale.get(SETTINGS_SECTION_APPEARANCE),
            Locale.get(SETTINGS_SECTION_IMAGES),
            Locale.get(SETTINGS_SECTION_BEHAVIOR),
            Locale.get(SETTINGS_SECTION_NOTIFICATIONS),
            Locale.get(SETTINGS_SECTION_LANGUAGE),
            Locale.get(DATA_MANAGER_TITLE),
        };

        append(sectionNames[0], App.ic.themesGroup);
        append(sectionNames[1], App.ic.attachFormat);
        append(sectionNames[2], App.ic.uiGroup);
        append(sectionNames[3], App.ic.notify);
        append(sectionNames[4], App.ic.language);
        append(sectionNames[5], App.ic.dataManager);

        saveCommand = Locale.createCommand(SAVE, Command.BACK, 0);
        cancelCommand = Locale.createCommand(CANCEL, Command.BACK, 1);
        addCommand(saveCommand);
        addCommand(cancelCommand);

        loadSettings();
    }

    private static final String[] zeroValueLabels = { "0" };

    private void loadSettings() {
        if (SettingsSectionScreen.settings != null) return;

        int[] themeValueLabels = { THEME_DARK, THEME_LIGHT, THEME_BLACK, THEME_SYSTEM, THEME_CUSTOM };
        int[] fontValueLabels = { FONT_SMALL, FONT_MEDIUM, FONT_LARGE };
        int[] replyValueLabels = { REPLIES_ONLY_RECIPIENT, REPLIES_FULL_MESSAGE };
        int[] messageBarValueLabels = { SETTING_VALUE_OFF, SETTING_VALUE_AUTO, SETTING_VALUE_ON };
        String[] imageFormatValueLabels = { "PNG", "JPEG" };
        int[] pfpShapeValueLabels = { PFP_OFF, PFP_SQUARE, PFP_CIRCLE, PFP_CIRCLE_HQ };
        int[] pfpSizeValueLabels = { PFP_PLACEHOLDER, PFP_16PX, PFP_32PX };
        int[] menuIconValueLabels = { SETTING_VALUE_OFF };
        int[] emojiValueLabels = { SETTING_VALUE_OFF, SHOW_EMOJI_DEFAULT_ONLY, SHOW_EMOJI_ALL };
        int[] scrollBarValueLabels = { SETTING_VALUE_OFF, SCROLLBAR_WHEN_NEEDED, SCROLLBAR_PERMANENT };
        int[] autoUpdateValueLabels = { SETTING_VALUE_OFF, RELEASES_ONLY, AUTO_UPDATE_ALL_STR };

        Image[] themeIcons = { App.ic.themeDark, App.ic.themeLight, App.ic.themeBlack, App.ic.settings, App.ic.themeCustom };
        Image[] fontIcons = { App.ic.fontSmall, App.ic.fontMedium, App.ic.fontLarge };
        Image[] replyIcons = { App.ic.repliesName, App.ic.repliesFull };
        Image[] nameColorIcons = { App.ic.nameColorsOff, App.ic.nameColors };
        Image[] pfpShapeIcons = { App.ic.pfpNone, App.ic.pfpSquare, App.ic.pfpCircle, App.ic.pfpCircleHq };
        Image[] pfpSizeIcons = { App.ic.pfpPlaceholder, App.ic.pfp16, App.ic.pfp32 };

        // Some settings are hidden or shown only on platforms with certain system properties, checked both compile-time (for different builds of the app) and run-time

        // Settings that are hidden on certain platforms (only on certain builds and if a runtime check passes):

        Setting fullscreenSetting =
//#ifdef MIDP2_GENERIC
            Util.isKemulator ? new Setting(Settings.fullscreenDefault ? 1 : 0) :
//#endif
            new Setting(FULLSCREEN_DEFAULT, 1, Settings.fullscreenDefault ? 1 : 0, App.ic.fullscreen);

        Setting fastScrollSetting =
//#ifdef MIDP2_GENERIC
            Util.isFullTouch ? new Setting(KeyRepeatThread.enabled ? 1 : 0) :
//#endif
            new Setting(FAST_SCROLLING, 1, KeyRepeatThread.enabled ? 1 : 0, null, App.ic.fastScroll);

        // Settings that only exist on certain builds and are only shown if a runtime check passes:

        Setting piglerSetting =
//#ifdef PIGLER_SUPPORT
            Util.supportsPigler ?
                new Setting(NOTIFICATIONS_PIGLER, 1, Settings.showNotifPigler ? 1 : 0, App.ic.pigler) :
                new Setting(Settings.showNotifPigler ? 1 : 0);
//#else
            null;
//#endif

        Setting nokiaUISetting =
//#ifdef NOKIA_UI_ICON
            Util.supportsNokiaUINotifs ?
                new Setting(NOTIFICATIONS_NOKIA_UI, 1, Settings.showNotifNokiaUI ? 1 : 0, App.ic.nokiaUI) :
                new Setting(Settings.showNotifNokiaUI ? 1 : 0);
//#else
//#ifdef J2ME_LOADER
            // Different label and icon on J2ME Loader
            Util.supportsNokiaUINotifs ?
                new Setting(NOTIFICATIONS_ANDROID, 1, Settings.showNotifNokiaUI ? 1 : 0, App.ic.android) :
                new Setting(Settings.showNotifNokiaUI ? 1 : 0);
//#else
            null;
//#endif
//#endif

        SettingsSectionScreen.settings = new Setting[][] {
            {
                // Appearance section
                new Setting(SETTINGS_SECTION_THEMES, 4, Settings.theme, themeValueLabels, themeIcons),
                new Setting(SETTINGS_SECTION_AUTHOR_FONT, 2, Settings.authorFontSize, fontValueLabels, fontIcons),
                new Setting(SETTINGS_SECTION_CONTENT_FONT, 2, Settings.messageFontSize, fontValueLabels, fontIcons),
                new Setting(SETTINGS_SECTION_REPLIES, 1, Settings.showRefMessage ? 1 : 0, replyValueLabels, replyIcons),
                new Setting(TIME_FORMAT, 1, 0, App.ic.use12h),
                new Setting(NAME_COLORS, 1, Settings.useNameColors ? 1 : 0, nameColorIcons),
                fullscreenSetting,
                new Setting(TEXT_FORMATTING, 1, FormattedString.useMarkdown ? 1 : 0, App.ic.markdown),
//#ifdef TOUCH_SUPPORT
                new Setting(SHOW_MESSAGE_BAR, 2, Settings.messageBarMode, messageBarValueLabels, App.ic.msgBar),
//#else
                null,
//#endif
            }, {
                // Images section
                new Setting(SETTINGS_SECTION_IMAGE_FORMAT, 1, Settings.useJpeg ? 1 : 0, imageFormatValueLabels, App.ic.attachFormat),
                new Setting(SETTINGS_SECTION_IMAGE_SIZE, 10000, Settings.attachmentSize, zeroValueLabels, App.ic.attachSize),
                new Setting(SETTINGS_SECTION_PFP_SHAPE, 3, Settings.pfpType, pfpShapeValueLabels, pfpShapeIcons),
                new Setting(SETTINGS_SECTION_PFP_RESOLUTION, 2, Settings.pfpSize, pfpSizeValueLabels, pfpSizeIcons),
                new Setting(SETTINGS_SECTION_MENU_ICONS, 255, Settings.menuIconSize, menuIconValueLabels, App.ic.iconSize),
                new Setting(GUILD_ICONS, 1, Settings.showMenuIcons ? 1 : 0, App.ic.menuIcons),
                new Setting(FILE_PREVIEW, 1, Settings.useFilePreview ? 1 : 0, App.ic.attachFormat),
//#ifdef EMOJI_SUPPORT
                new Setting(SHOW_EMOJI, 2, FormattedString.emojiMode, emojiValueLabels, App.ic.emoji),
//#else
                null,
//#endif
            }, {
                // Behaviour section
                new Setting(SETTINGS_SECTION_MESSAGE_COUNT, 100, Settings.messageLoadCount, zeroValueLabels, App.ic.msgCount),
                new Setting(HIGH_RAM_MODE, 1, Settings.highRamMode ? 1 : 0, App.ic.keepChLoaded),
                new Setting(NATIVE_FILE_PICKER, 1, Settings.nativeFilePicker ? 1 : 0, App.ic.nativePicker),
                new Setting(AUTO_RECONNECT, 1, Settings.autoReConnect ? 1 : 0, App.ic.autoReconnect),
                new Setting(DEFAULT_HOTKEYS, 1, Settings.defaultHotkeys ? 1 : 0, App.ic.keysDefault),
                new Setting(REMAP_HOTKEYS_L, 1, 0, App.ic.keys),
                new Setting(SHOW_SCROLLBAR, 2, KineticScrollingCanvas.scrollBarMode, scrollBarValueLabels, App.ic.scrollBars),
                new Setting(AUTO_UPDATE, 2, Settings.autoUpdate, autoUpdateValueLabels, App.ic.autoUpdate),
                fastScrollSetting,
                new Setting(SEND_TYPING, 1, Settings.sendTyping ? 1 : 0, App.ic.typing),
            }, {
                // Notifications section
                new Setting(NOTIFICATIONS_ALL, 1, Settings.showNotifsAll ? 1 : 0, App.ic.msgCount),
                new Setting(NOTIFICATIONS_MENTIONS, 1, Settings.showNotifsPings ? 1 : 0, App.ic.notifyPing),
                new Setting(NOTIFICATIONS_DMS, 1, Settings.showNotifsDMs ? 1 : 0, App.ic.notifyDM),
                new Setting(NOTIFICATIONS_ALERT, 1, Settings.showNotifAlert ? 1 : 0, App.ic.notifyAlert),
                new Setting(NOTIFICATIONS_SOUND, 1, Settings.playNotifSound ? 1 : 0, App.ic.notifySound),
                new Setting(NOTIFICATIONS_VIBRATE, 1, Settings.playNotifVibra ? 1 : 0, App.ic.vibra),
                piglerSetting,
                nokiaUISetting,
            }
        };
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            int selected = getSelectedIndex();

            // select submenu (settings section)
            // Some screens are separate menus, other screens are part of this menu
            switch (selected) {
                case 4: {
                    App.disp.setCurrent(new LanguageSelector());
                    break;
                }
                case 5: {
                    App.disp.setCurrent(new DataManagerScreen());
                    break;
                }
                default: {
                    App.disp.setCurrent(new SettingsSectionScreen(selected));
                    break;
                }
            }
        }
        else {
            // Save or cancel command: (if save, write changes to state and save them persistently), then return to main menu
            if (c == saveCommand) {
                Setting[][] settings = SettingsSectionScreen.settings;

                // Check if icons need to be reloaded (if any icon-related settings have changed)
                boolean reloadMenuIcons =
                    Settings.menuIconSize != settings[1][4].value ||
                    Settings.showMenuIcons != (settings[1][5].value == 1);

                boolean reloadIcons =
                    reloadMenuIcons ||
                    Settings.pfpType != settings[1][2].value ||
                    Settings.pfpSize != settings[1][3].value ||
                    Settings.useJpeg != (settings[1][0].value == 1);

                boolean fontSizeChanged =
                    Settings.authorFontSize != settings[0][1].value ||
                    Settings.messageFontSize != settings[0][2].value;

                Settings.theme = settings[0][0].value;
                Settings.authorFontSize = settings[0][1].value;
                Settings.messageFontSize = settings[0][2].value;
                Settings.showRefMessage = settings[0][3].value == 1;
                Settings.useNameColors = settings[0][5].value == 1;
//#ifdef 
                Settings.fullscreenDefault = settings[0][6].value == 1;
                FormattedString.useMarkdown = settings[0][7].value == 1;
//#ifdef TOUCH_SUPPORT
                Settings.messageBarMode = settings[0][8].value;
//#endif

                Settings.useJpeg = settings[1][0].value == 1;
                Settings.attachmentSize = settings[1][1].value;
                Settings.pfpType = settings[1][2].value;
                Settings.pfpSize = settings[1][3].value;
                Settings.menuIconSize = settings[1][4].value;
                Settings.showMenuIcons = settings[1][5].value == 1;
                Settings.useFilePreview = settings[1][6].value == 1;
//#ifdef EMOJI_SUPPORT
                FormattedString.emojiMode = settings[1][7].value;
                App.gatewayToggleGuildEmoji();
//#endif

                Settings.messageLoadCount = settings[2][0].value;
                Settings.highRamMode = settings[2][1].value == 1;
                Settings.nativeFilePicker = settings[2][2].value == 1;
                Settings.autoReConnect = settings[2][3].value == 1;
                Settings.defaultHotkeys = settings[2][4].value == 1;
                KineticScrollingCanvas.scrollBarMode = settings[2][6].value;
                Settings.autoUpdate = settings[2][7].value;
                KeyRepeatThread.toggle(settings[2][8].value == 1);
                Settings.sendTyping = settings[2][9].value == 1;

                Settings.showNotifsAll = settings[3][0].value == 1;
                Settings.showNotifsPings = settings[3][1].value == 1;
                Settings.showNotifsDMs = settings[3][2].value == 1;
                Settings.showNotifAlert = settings[3][3].value == 1;
                Settings.playNotifSound = settings[3][4].value == 1;
                Settings.playNotifVibra = settings[3][5].value == 1;
//#ifdef PIGLER_SUPPORT
                Settings.showNotifPigler = settings[3][6].value == 1;
//#endif
//#ifdef NOKIA_UI_SUPPORT
                Settings.showNotifNokiaUI = settings[3][7].value == 1;
//#endif

                // Unload server and DM lists if needed, so the icons and font-based layout metrics get refreshed
                if (reloadIcons || fontSizeChanged) {
                    App.guilds = null;
                    App.dmChannels = null;

                    if (reloadIcons) {
                        IconCache.init();
                        
                        if (reloadMenuIcons) {
                            App.ic = null;
                            App.ic = new Icons();
                        }
                    }
                }

//#ifdef PIGLER_SUPPORT
                if (App.gatewayActive()) App.gateway.checkInitPigler();
//#endif
                Settings.save();
                Theme.load();
                App.loadFonts();
            }
            SettingsSectionScreen.settings = null;
            App.disp.setCurrent(MainMenu.get(true));
        }
    }
}
