package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsScreen extends ListScreen implements CommandListener, Strings {
    private static SettingsScreen instance;

    private State s;
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
    private int[][] values;

    private static final int[][] maxValues = {
        { 2, 2, 2, 1, 1, 1, 1,
        // ifdef OVER_100KB
        1
        // endif
        },
        { 1, 10000, 3, 2, 255, 1,
        // ifdef OVER_100KB    
        1, 2
        // endif
        },
        {100, 1, 1, 1, 1, 1, 2, 2,
        // ifdef OVER_100KB
        1
        // endif
        },
        { 1, 1, 1, 1, 1, 1,
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

    SettingsScreen(State s) {
        super(Locale.get(SETTINGS_FORM_TITLE), false, false, true);
        setCommandListener(this); 
        this.s = s;

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
                Locale.get(SHOW_EMOJI),
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
                { s.ic.themeDark, s.ic.themeLight, s.ic.themeBlack },
                { s.ic.fontSmall, s.ic.fontMedium, s.ic.fontLarge },
                { s.ic.fontSmall, s.ic.fontMedium, s.ic.fontLarge },
                { s.ic.repliesName, s.ic.repliesFull },
                { s.ic.use12h },
                { s.ic.nameColorsOff, s.ic.nameColors },
                { s.ic.fullscreen },
                // ifdef OVER_100KB
                { s.ic.markdown }
                // endif
            }, {
                // Images
                { s.ic.attachFormat },
                { s.ic.attachSize },
                { s.ic.pfpNone, s.ic.pfpSquare, s.ic.pfpCircle, s.ic.pfpCircleHq },
                { s.ic.pfpPlaceholder, s.ic.pfp16, s.ic.pfp32 },
                { s.ic.iconSize },
                { s.ic.menuIcons },
                // ifdef OVER_100KB
                { s.ic.attachFormat },
                { s.ic.emoji }
                // endif
            }, {
                // Behavior
                { s.ic.msgCount },
                { s.ic.keepChLoaded },
                { s.ic.nativePicker },
                { s.ic.autoReconnect },
                { s.ic.keysDefault },
                { s.ic.keys },
                { s.ic.scrollBars },
                { s.ic.autoUpdate },
                // ifdef OVER_100KB
                { null }
                // endif
            }, {
                // Notifications
                { s.ic.msgCount },
                { s.ic.notifyPing },
                { s.ic.notifyDM },
                { s.ic.notifyAlert },
                { s.ic.notifySound },
                { s.ic.vibra },
                // ifdef PIGLER_SUPPORT
                { s.ic.pigler },
                // endif
                // The below two are for the same option, Nokia UI notifications. Only one of these defines is ever defined.
                // ifdef NOKIA_UI_ICON
                { s.ic.nokiaUI },
                // endif
                // ifdef J2ME_LOADER
                { s.ic.android }
                // endif
            }
        };
        String[] boolValues = { Locale.get(SETTING_VALUE_OFF), Locale.get(SETTING_VALUE_ON) };
        
        valueLabels = new String[][][] {
            {
                // Appearance
                { Locale.get(THEME_DARK), Locale.get(THEME_LIGHT), Locale.get(THEME_BLACK) },
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
                { Locale.get(SETTING_VALUE_OFF), Locale.get(SHOW_EMOJI_DEFAULT_ONLY), Locale.get(SHOW_EMOJI_ALL) }
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
                s.theme,
                s.authorFontSize, 
                s.messageFontSize,
                s.showRefMessage ? 1 : 0,
                s.use12hTime ? 1 : 0,
                s.useNameColors ? 1 : 0,
                s.fullscreenDefault ? 1 : 0,
                // ifdef OVER_100KB
                FormattedString.useMarkdown ? 1 : 0
                // endif
            }, {
                // Images
                s.useJpeg ? 1 : 0,
                s.attachmentSize,
                s.pfpType,
                s.pfpSize,
                s.menuIconSize,
                s.showMenuIcons ? 1 : 0,
                // ifdef OVER_100KB
                s.useFilePreview ? 1 : 0,
                FormattedString.emojiMode
                // endif
            }, {
                // Behavior
                s.messageLoadCount,
                s.highRamMode ? 1 : 0,
                s.nativeFilePicker ? 1 : 0,
                s.autoReConnect ? 1 : 0,
                s.defaultHotkeys ? 1 : 0,
                0,
                KineticScrollingCanvas.scrollBarMode,
                s.autoUpdate,
                // ifdef OVER_100KB
                s.sendTyping ? 1 : 0,
                // endif
            }, {
                // Notifications
                s.showNotifsAll ? 1 : 0, 
                s.showNotifsPings ? 1 : 0,
                s.showNotifsDMs ? 1 : 0,
                s.showNotifAlert ? 1 : 0,
                s.playNotifSound ? 1 : 0,
                s.playNotifVibra ? 1 : 0,
                // ifdef PIGLER_SUPPORT
                s.showNotifPigler ? 1 : 0,
                // endif
                // ifdef NOKIA_UI_SUPPORT
                s.showNotifNokiaUI ? 1 : 0,
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

    private void updateMenuItem(int index) {
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
        append(sectionNames[0], s.ic.themesGroup);
        append(sectionNames[1], s.ic.attachFormat);
        append(sectionNames[2], s.ic.uiGroup);
        append(sectionNames[3], s.ic.notify);
        append(sectionNames[4], s.ic.language);

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
        s.disp.setCurrent(tb);
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
                    s.disp.setCurrent(new KeyMapper(s));
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
                // Language selection screen is a separate menu, other screens are part of this menu
                if (selected == 4) {
                    s.disp.setCurrent(new LanguageSelector(s));
                } else {
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
                    s.menuIconSize != values[1][4] ||
                    s.showMenuIcons != (values[1][5] == 1);

                boolean reloadIcons =
                    reloadMenuIcons ||
                    s.pfpType != values[1][2] ||
                    s.pfpSize != values[1][3] ||
                    s.useJpeg != (values[1][0] == 1);

                s.theme = values[0][0];
                s.authorFontSize = values[0][1];
                s.messageFontSize = values[0][2];
                s.showRefMessage = values[0][3] == 1;
                s.use12hTime = values[0][4] == 1;
                s.useNameColors = values[0][5] == 1;
                s.fullscreenDefault = values[0][6] == 1;
                // ifdef OVER_100KB
                FormattedString.useMarkdown = values[0][7] == 1;
                // endif

                s.useJpeg = values[1][0] == 1;
                s.attachmentSize = values[1][1];
                s.pfpType = values[1][2];
                s.pfpSize = values[1][3];
                s.menuIconSize = values[1][4];
                s.showMenuIcons = values[1][5] == 1;
                // ifdef OVER_100KB
                s.useFilePreview = values[1][6] == 1;
                FormattedString.emojiMode = values[1][7];
                s.gatewayToggleGuildEmoji();
                // endif

                s.messageLoadCount = values[2][0];
                s.highRamMode = values[2][1] == 1;
                s.nativeFilePicker = values[2][2] == 1;
                s.autoReConnect = values[2][3] == 1;
                s.defaultHotkeys = values[2][4] == 1;
                KineticScrollingCanvas.scrollBarMode = values[2][6];
                s.autoUpdate = values[2][7];
                // ifdef OVER_100KB
                s.sendTyping = values[2][8] == 1;
                // endif

                s.showNotifsAll = values[3][0] == 1;
                s.showNotifsPings = values[3][1] == 1;
                s.showNotifsDMs = values[3][2] == 1;
                s.showNotifAlert = values[3][3] == 1;
                s.playNotifSound = values[3][4] == 1;
                s.playNotifVibra = values[3][5] == 1;
                int index = 6;
                // ifdef PIGLER_SUPPORT
                s.showNotifPigler = values[3][index] == 1;
                index++;
                // endif
                // ifdef NOKIA_UI_SUPPORT
                s.showNotifNokiaUI = values[3][index] == 1;
                // endif

                if (reloadIcons) {
                    // Unload server and DM lists so the icons get refreshed
                    IconCache.init(s);
                    s.guilds = null;
                    s.dmChannels = null;
                    if (reloadMenuIcons) {
                        s.ic = null;
                        s.ic = new Icons(s);
                    }
                }

                // ifdef PIGLER_SUPPORT
                if (s.gatewayActive()) s.gateway.checkInitPigler();
                // endif
                LoginSettings.save(s);
                s.loadTheme();
                s.loadFonts();
            }
            s.disp.setCurrent(MainMenu.get(s));
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
                    s.error(Locale.get(SETTINGS_ERROR_INVALID_NUMBER_PREFIX) + max + Locale.get(SETTINGS_ERROR_INVALID_NUMBER_SUFFIX));
                    return;
                }
            }
            s.disp.setCurrent(SettingsScreen.instance);
            SettingsScreen.instance = null;
        }
    }
}