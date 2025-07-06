package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsScreen extends ListScreen implements CommandListener, Strings {
    private static SettingsScreen instance;

    private Command saveCommand;
    private Command textBoxOkCommand;
    private Command cancelCommand;

    // Settings menu items/structure.
    // First array dimension is settings submenu (e.g. Behavior, Appearance)
    // Second dimension is item position in that submenu
    // For icons and value labels, third dimension is that setting's current numeric value
    private String[] sectionNames;
    private Image[][][] icons;
    private String[][] labels;
    private String[][][] valueLabels;
    public int[][] values;

    private static final int[][] maxValues = {
        {
            // ifdef OVER_100KB
            4,
            // else
            2,
            // endif            
            2, 2, 1, 1, 1, 1,
            // ifdef OVER_100KB
            1,
            // endif
        },
        {
            1, 10000, 3, 2, 255, 1,
            // ifdef OVER_100KB    
            1,
            // ifdef EMOJI_SUPPORT
            2,
            // endif
            // endif
        },
        {
            100, 1, 1, 1, 1, 1, 2, 2,
            // ifdef OVER_100KB
            1, 1,
            // endif
        },
        {
            1, 1, 1, 1, 1, 1,
            // ifdef PIGLER_SUPPORT
            1,
            // endif
            // ifdef NOKIA_UI_SUPPORT
            1,
            // endif
        },
    };

    private boolean isInSubmenu;
    private int currentSection;

    SettingsScreen() {
        super(Locale.get(SETTINGS_FORM_TITLE), false, false, true);
        setCommandListener(this);

        BACK_COMMAND = Locale.createCommand(BACK, Command.BACK, 0);
        saveCommand = Locale.createCommand(SAVE, Command.BACK, 0);
        textBoxOkCommand = Locale.createCommand(OK, Command.OK, 0);
        cancelCommand = Locale.createCommand(CANCEL, Command.BACK, 1);

        sectionNames = new String[] {
            Locale.get(SETTINGS_SECTION_APPEARANCE),
            Locale.get(SETTINGS_SECTION_IMAGES),
            Locale.get(SETTINGS_SECTION_BEHAVIOR),
            Locale.get(SETTINGS_SECTION_NOTIFICATIONS),
            Locale.get(SETTINGS_SECTION_LANGUAGE),
            // ifdef OVER_100KB
            Locale.get(DATA_MANAGER_TITLE),
            // endif
        };

        labels = new String[][] {
            {
                // Appearance
                Locale.get(SETTINGS_SECTION_THEMES),
                Locale.get(SETTINGS_SECTION_AUTHOR_FONT),
                Locale.get(SETTINGS_SECTION_CONTENT_FONT),
                Locale.get(SETTINGS_SECTION_REPLIES),
                Locale.get(USE_12H_TIME),
                Locale.get(NAME_COLORS),
                Locale.get(FULLSCREEN_DEFAULT),
                // ifdef OVER_100KB
                Locale.get(TEXT_FORMATTING),
                // endif
            }, {
                // Images
                Locale.get(SETTINGS_SECTION_IMAGE_FORMAT),
                Locale.get(SETTINGS_SECTION_IMAGE_SIZE),
                Locale.get(SETTINGS_SECTION_PFP_SHAPE),
                Locale.get(SETTINGS_SECTION_PFP_RESOLUTION),
                Locale.get(SETTINGS_SECTION_MENU_ICONS),
                Locale.get(GUILD_ICONS),
                // ifdef OVER_100KB
                Locale.get(FILE_PREVIEW),
                // ifdef EMOJI_SUPPORT
                Locale.get(SHOW_EMOJI),
                // endif
                // endif
            }, {
                // Behavior
                Locale.get(SETTINGS_SECTION_MESSAGE_COUNT),
                Locale.get(HIGH_RAM_MODE),
                Locale.get(NATIVE_FILE_PICKER),
                Locale.get(AUTO_RECONNECT),
                Locale.get(DEFAULT_HOTKEYS),
                Locale.get(REMAP_HOTKEYS_L),
                Locale.get(SHOW_SCROLLBAR),
                Locale.get(AUTO_UPDATE),
                // ifdef OVER_100KB
                Locale.get(SEND_TYPING),
                Locale.get(FAST_SCROLLING),
                // endif
            }, {
                // Notifications
                Locale.get(NOTIFICATIONS_ALL),
                Locale.get(NOTIFICATIONS_MENTIONS),
                Locale.get(NOTIFICATIONS_DMS),
                Locale.get(NOTIFICATIONS_ALERT),
                Locale.get(NOTIFICATIONS_SOUND),
                Locale.get(NOTIFICATIONS_VIBRATE),
                // ifdef PIGLER_SUPPORT
                Locale.get(NOTIFICATIONS_PIGLER),
                // endif
                // The below two are for the same option, Nokia UI notifications. Only one of these defines is ever defined.
                // ifdef NOKIA_UI_ICON
                Locale.get(NOTIFICATIONS_NOKIA_UI),
                // endif
                // ifdef J2ME_LOADER
                Locale.get(NOTIFICATIONS_ANDROID),
                // endif
            },
        };

        icons = new Image[][][] {
            {
                // Appearance
                {
                    App.ic.themeDark, App.ic.themeLight, App.ic.themeBlack,
                    // ifdef OVER_100KB
                    App.ic.settings, App.ic.themeCustom
                    // endif
                },
                { App.ic.fontSmall, App.ic.fontMedium, App.ic.fontLarge },
                { App.ic.fontSmall, App.ic.fontMedium, App.ic.fontLarge },
                { App.ic.repliesName, App.ic.repliesFull },
                { App.ic.use12h },
                { App.ic.nameColorsOff, App.ic.nameColors },
                { App.ic.fullscreen },
                // ifdef OVER_100KB
                { App.ic.markdown }
                // endif
            }, {
                // Images
                { App.ic.attachFormat },
                { App.ic.attachSize },
                { App.ic.pfpNone, App.ic.pfpSquare, App.ic.pfpCircle, App.ic.pfpCircleHq },
                { App.ic.pfpPlaceholder, App.ic.pfp16, App.ic.pfp32 },
                { App.ic.iconSize },
                { App.ic.menuIcons },
                // ifdef OVER_100KB
                { App.ic.attachFormat },
                // ifdef EMOJI_SUPPORT
                { App.ic.emoji }
                // endif
                // endif
            }, {
                // Behavior
                { App.ic.msgCount },
                { App.ic.keepChLoaded },
                { App.ic.nativePicker },
                { App.ic.autoReconnect },
                { App.ic.keysDefault },
                { App.ic.keys },
                { App.ic.scrollBars },
                { App.ic.autoUpdate },
                // ifdef OVER_100KB
                { App.ic.typing },
                { App.ic.fastScroll },
                // endif
            }, {
                // Notifications
                { App.ic.msgCount },
                { App.ic.notifyPing },
                { App.ic.notifyDM },
                { App.ic.notifyAlert },
                { App.ic.notifySound },
                { App.ic.vibra },
                // ifdef PIGLER_SUPPORT
                { App.ic.pigler },
                // endif
                // The below two are for the same option, Nokia UI notifications. Only one of these defines is ever defined.
                // ifdef NOKIA_UI_ICON
                { App.ic.nokiaUI },
                // endif
                // ifdef J2ME_LOADER
                { App.ic.android }
                // endif
            }
        };
        String[] boolValues = { Locale.get(SETTING_VALUE_OFF), Locale.get(SETTING_VALUE_ON) };
        
        valueLabels = new String[][][] {
            {
                // Appearance
                {
                    Locale.get(THEME_DARK), Locale.get(THEME_LIGHT), Locale.get(THEME_BLACK),
                    // ifdef OVER_100KB
                    Locale.get(THEME_SYSTEM), Locale.get(THEME_CUSTOM),
                    // endif
                },
                { Locale.get(FONT_SMALL), Locale.get(FONT_MEDIUM), Locale.get(FONT_LARGE) },
                { Locale.get(FONT_SMALL), Locale.get(FONT_MEDIUM), Locale.get(FONT_LARGE) },
                { Locale.get(REPLIES_ONLY_RECIPIENT), Locale.get(REPLIES_FULL_MESSAGE) },
                boolValues,
                boolValues,
                boolValues,
                // ifdef OVER_100KB
                boolValues
                // endif
            }, {
                // Images
                { "PNG", "JPEG" },
                { "0" },
                { Locale.get(PFP_OFF), Locale.get(PFP_SQUARE), Locale.get(PFP_CIRCLE), Locale.get(PFP_CIRCLE_HQ) },
                { Locale.get(PFP_PLACEHOLDER), Locale.get(PFP_16PX), Locale.get(PFP_32PX) },
                { Locale.get(SETTING_VALUE_OFF) },
                boolValues,
                // ifdef OVER_100KB
                boolValues,
                // ifdef EMOJI_SUPPORT
                { Locale.get(SETTING_VALUE_OFF), Locale.get(SHOW_EMOJI_DEFAULT_ONLY), Locale.get(SHOW_EMOJI_ALL) }
                // endif
                // endif
            }, {
                // Behavior
                { "0" },
                boolValues,
                boolValues,
                boolValues,
                boolValues,
                null,
                { Locale.get(SETTING_VALUE_OFF), Locale.get(SCROLLBAR_WHEN_NEEDED), Locale.get(SCROLLBAR_PERMANENT) },
                { Locale.get(SETTING_VALUE_OFF), Locale.get(RELEASES_ONLY), Locale.get(AUTO_UPDATE_ALL_STR) },
                // ifdef OVER_100KB
                boolValues,
                boolValues,
                // endif
            }, {
                // Notifications
                boolValues,
                boolValues,
                boolValues,
                boolValues,
                boolValues,
                boolValues,
                // ifdef PIGLER_SUPPORT
                boolValues,
                // endif
                // ifdef NOKIA_UI_SUPPORT
                boolValues,
                // endif
            }
        };
        values = new int[][] {
            {
                // Appearance
                Settings.theme,
                Settings.authorFontSize, 
                Settings.messageFontSize,
                Settings.showRefMessage ? 1 : 0,
                Settings.use12hTime ? 1 : 0,
                Settings.useNameColors ? 1 : 0,
                Settings.fullscreenDefault ? 1 : 0,
                // ifdef OVER_100KB
                FormattedString.useMarkdown ? 1 : 0
                // endif
            }, {
                // Images
                Settings.useJpeg ? 1 : 0,
                Settings.attachmentSize,
                Settings.pfpType,
                Settings.pfpSize,
                Settings.menuIconSize,
                Settings.showMenuIcons ? 1 : 0,
                // ifdef OVER_100KB
                Settings.useFilePreview ? 1 : 0,
                // ifdef EMOJI_SUPPORT
                FormattedString.emojiMode
                // endif
                // endif
            }, {
                // Behavior
                Settings.messageLoadCount,
                Settings.highRamMode ? 1 : 0,
                Settings.nativeFilePicker ? 1 : 0,
                Settings.autoReConnect ? 1 : 0,
                Settings.defaultHotkeys ? 1 : 0,
                0,
                KineticScrollingCanvas.scrollBarMode,
                Settings.autoUpdate,
                // ifdef OVER_100KB
                Settings.sendTyping ? 1 : 0,
                KeyRepeatThread.enabled ? 1 : 0,
                // endif
            }, {
                // Notifications
                Settings.showNotifsAll ? 1 : 0, 
                Settings.showNotifsPings ? 1 : 0,
                Settings.showNotifsDMs ? 1 : 0,
                Settings.showNotifAlert ? 1 : 0,
                Settings.playNotifSound ? 1 : 0,
                Settings.playNotifVibra ? 1 : 0,
                // ifdef PIGLER_SUPPORT
                Settings.showNotifPigler ? 1 : 0,
                // endif
                // ifdef NOKIA_UI_SUPPORT
                Settings.showNotifNokiaUI ? 1 : 0,
                // endif
            }
        };
        showMainScreen();
    }

    // Gets settings value index that should be changed based on selected menu item index
    private int getItemIndex(int item) {
        // ifdef PIGLER_SUPPORT
        // Pigler API not supported on device - 6th item in notifs menu corresponds to 7th setting
        if (isInSubmenu && currentSection == 3 && item == 6 && !Util.supportsPigler) return 7;
        // endif
        // ifdef MIDP2_GENERIC
        // KEmu is always fullscreen - 6th item in appearance menu corresponds to 7th setting
        if (isInSubmenu && currentSection == 0 && item == 6 && Util.isKemulator) return 7;
        // endif
        return item;
    }

    private String getValueLabel(int section, int item) {
        int itemIndex = getItemIndex(item);
        String[] itemValueLabels = valueLabels[section][itemIndex];
        int value = values[section][itemIndex];

        if (itemValueLabels == null) {
            return null;
        } else {
            return (itemValueLabels.length > value) ?
                itemValueLabels[value] :
                Integer.toString(value);
        }
    }

    private Image getIcon(int section, int item) {
        int itemIndex = getItemIndex(item);
        Image[] itemIcons = icons[section][itemIndex];
        int value = values[section][itemIndex];
        return (itemIcons.length > value) ? itemIcons[value] : itemIcons[0];
    }

    public void updateMenuItem(int index) {
        int itemIndex = getItemIndex(index);
        set(
            index,
            labels[currentSection][itemIndex],
            getValueLabel(currentSection, index),
            getIcon(currentSection, index),
            ListScreen.INDICATOR_NONE
        );
    }

    private void cycleValue(int direction) {
        int selected = getSelectedIndex();
        int itemIndex = getItemIndex(selected);

        values[currentSection][itemIndex] += direction;
        
        int max = maxValues[currentSection][itemIndex];
        
        if (values[currentSection][itemIndex] < 0) {
            values[currentSection][itemIndex] = max;
        }
        else if (values[currentSection][itemIndex] > max) {
            values[currentSection][itemIndex] = 0;
        }
        updateMenuItem(selected);
    }

    private void showMainScreen() {
        setTitle(Locale.get(SETTINGS_FORM_TITLE));
        removeCommand(BACK_COMMAND);
        addCommand(saveCommand);
        addCommand(cancelCommand);

        isInSubmenu = false;

        deleteAll();
        append(sectionNames[0], App.ic.themesGroup);
        append(sectionNames[1], App.ic.attachFormat);
        append(sectionNames[2], App.ic.uiGroup);
        append(sectionNames[3], App.ic.notify);
        append(sectionNames[4], App.ic.language);
        // ifdef OVER_100KB
        append(sectionNames[5], App.ic.dataManager);
        // endif

        setSelectedIndex(currentSection, true);
    }

    private void showSectionScreen(int index) {
        setTitle(sectionNames[index]);
        addCommand(BACK_COMMAND);
        removeCommand(saveCommand);
        removeCommand(cancelCommand);

        isInSubmenu = true;
        currentSection = index;

        deleteAll();
        int nokiaUIOptionIndex = 6;
        // ifdef PIGLER_SUPPORT
        nokiaUIOptionIndex++;
        // endif
        for (int i = 0; i < labels[index].length; i++) {
            if (index == 3) {
                // Pigler API option is only shown on devices that support said API
                // ifdef PIGLER_SUPPORT
                if (i == 6 && !Util.supportsPigler) continue;
                // endif
                // Same for Nokia UI API
                // ifdef NOKIA_UI_SUPPORT
                if (i == nokiaUIOptionIndex && !Util.supportsNokiaUINotifs) {
                    continue;
                }
                // endif
            }
            // Fullscreen option hidden on KEmu
            // ifdef MIDP2_GENERIC
            if (index == 0 && i == 6 && Util.isKemulator) continue;
            // endif
            
            append(labels[index][i], getValueLabel(index, i), getIcon(index, i), ListScreen.INDICATOR_NONE);
        }
    }

    private void showTextBox(int index) {
        SettingsScreen.instance = this;

        int itemIndex = getItemIndex(index);
        int max = maxValues[currentSection][itemIndex];
        int maxLength = (max == 0) ? 10 : Integer.toString(max).length();

        TextBox tb = new TextBox(
            labels[currentSection][itemIndex],
            Integer.toString(values[currentSection][itemIndex]),
            maxLength,
            TextField.NUMERIC
        );
        tb.addCommand(textBoxOkCommand);
        tb.addCommand(cancelCommand);
        tb.setCommandListener(this);
        App.disp.setCurrent(tb);
    }

    public void customKeyEvent(int keycode) {
        if (!isInSubmenu) return;
        
        switch (getGameAction(keycode)) {
            case LEFT: {
                cycleValue(-1);
                break;
            }
            case RIGHT: {
                cycleValue(1);
                break;
            }
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            int selected = getSelectedIndex();
            int itemIndex = getItemIndex(selected);
            if (isInSubmenu) {
                // In submenu: select item
                if (currentSection == 2 && itemIndex == 5) {
                    // Special case for "remap hotkeys" option - open separate menu
                    App.disp.setCurrent(new KeyMapper());
                } else {
                    int max = maxValues[currentSection][itemIndex];
                    
                    if (max == 0 || max >= 5) {
                        // Max value is 0 (any value allowed) or >= 5, show text entry
                        showTextBox(selected);
                    } else {
                        // Max value is a small number: cycle between values
                        cycleValue(1);
                    }
                }
            } else {
                // In top-level settings menu: select submenu (settings section)
                // Language selection and data manager screens are separate menus, other screens are part of this menu
                if (selected == 4) {
                    App.disp.setCurrent(new LanguageSelector());
                }
                // ifdef OVER_100KB
                else if (selected == 5) {
                    App.disp.setCurrent(new DataManagerScreen());
                }
                // endif
                else {
                    showSectionScreen(selected);
                }
            }
        }
        else if (c == BACK_COMMAND) {
            showMainScreen();
        }
        else if (d == this) {
            // Save or cancel command in main settings screen: (if save, write changes to state and save them persistently), then return to main menu
            if (c == saveCommand) {
                // Check if icons need to be reloaded (if any icon-related settings have changed)
                boolean reloadMenuIcons =
                    Settings.menuIconSize != values[1][4] ||
                    Settings.showMenuIcons != (values[1][5] == 1);

                boolean reloadIcons =
                    reloadMenuIcons ||
                    Settings.pfpType != values[1][2] ||
                    Settings.pfpSize != values[1][3] ||
                    Settings.useJpeg != (values[1][0] == 1);

                boolean fontSizeChanged =
                    Settings.authorFontSize != values[0][1] ||
                    Settings.messageFontSize != values[0][2];

                Settings.theme = values[0][0];
                Settings.authorFontSize = values[0][1];
                Settings.messageFontSize = values[0][2];
                Settings.showRefMessage = values[0][3] == 1;
                Settings.use12hTime = values[0][4] == 1;
                Settings.useNameColors = values[0][5] == 1;
                Settings.fullscreenDefault = values[0][6] == 1;
                // ifdef OVER_100KB
                FormattedString.useMarkdown = values[0][7] == 1;
                // endif

                Settings.useJpeg = values[1][0] == 1;
                Settings.attachmentSize = values[1][1];
                Settings.pfpType = values[1][2];
                Settings.pfpSize = values[1][3];
                Settings.menuIconSize = values[1][4];
                Settings.showMenuIcons = values[1][5] == 1;
                // ifdef OVER_100KB
                Settings.useFilePreview = values[1][6] == 1;
                // ifdef EMOJI_SUPPORT
                FormattedString.emojiMode = values[1][7];
                App.gatewayToggleGuildEmoji();
                // endif
                // endif

                Settings.messageLoadCount = values[2][0];
                Settings.highRamMode = values[2][1] == 1;
                Settings.nativeFilePicker = values[2][2] == 1;
                Settings.autoReConnect = values[2][3] == 1;
                Settings.defaultHotkeys = values[2][4] == 1;
                KineticScrollingCanvas.scrollBarMode = values[2][6];
                Settings.autoUpdate = values[2][7];
                // ifdef OVER_100KB
                Settings.sendTyping = values[2][8] == 1;
                KeyRepeatThread.toggle(values[2][9] == 1);
                // endif

                Settings.showNotifsAll = values[3][0] == 1;
                Settings.showNotifsPings = values[3][1] == 1;
                Settings.showNotifsDMs = values[3][2] == 1;
                Settings.showNotifAlert = values[3][3] == 1;
                Settings.playNotifSound = values[3][4] == 1;
                Settings.playNotifVibra = values[3][5] == 1;
                int index = 6;
                // ifdef PIGLER_SUPPORT
                Settings.showNotifPigler = values[3][index] == 1;
                index++;
                // endif
                // ifdef NOKIA_UI_SUPPORT
                Settings.showNotifNokiaUI = values[3][index] == 1;
                // endif

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

                // ifdef PIGLER_SUPPORT
                if (App.gatewayActive()) App.gateway.checkInitPigler();
                // endif
                Settings.save();
                Theme.load();
                App.loadFonts();
            }
            App.disp.setCurrent(MainMenu.get(true));
        }
        else {
            // OK or cancel command in textbox screen: (if OK, write entered value into values array), then return to where we left off in the settings screen
            if (c == textBoxOkCommand) {
                int selected = getSelectedIndex();
                int itemIndex = getItemIndex(selected);
                int max = maxValues[currentSection][itemIndex];
                try {
                    int value = Integer.parseInt(((TextBox) d).getString());
                    // Menu icon size has a minimum of 0 (off), other options have a min of 1
                    int min = (currentSection == 1 && itemIndex == 4) ? 0 : 1;
                    if (value < min || value > max) throw new Exception();

                    // Special case for menu icon size:
                    // 1 and 2 are reserved values that older versions used for 16 and 32 px
                    if (currentSection == 1 && itemIndex == 4 && (value == 1 || value == 2)) {
                        value = 3;
                    }

                    values[currentSection][itemIndex] = value;
                    updateMenuItem(selected);
                }
                catch (Exception e) {
                    App.error(Locale.get(SETTINGS_ERROR_INVALID_NUMBER_PREFIX) + max + Locale.get(SETTINGS_ERROR_INVALID_NUMBER_SUFFIX));
                    return;
                }
            }
            App.disp.setCurrent(SettingsScreen.instance);
            SettingsScreen.instance = null;
        }
    }
}
