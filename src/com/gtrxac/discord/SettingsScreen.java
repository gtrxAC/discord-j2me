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
        { 2, 2, 2, 1, 1, 1, 1 },
        { 1, 10000, 3, 2, 255, 1 },
        { 100, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1 },
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
            }, {
                // Images
                Locale.get(SETTINGS_SECTION_IMAGE_FORMAT),
                Locale.get(SETTINGS_SECTION_IMAGE_SIZE),
                Locale.get(SETTINGS_SECTION_PFP_SHAPE),
                Locale.get(SETTINGS_SECTION_PFP_RESOLUTION),
                Locale.get(SETTINGS_SECTION_MENU_ICONS),
                Locale.get(GUILD_ICONS),
            }, {
                // Behavior
                Locale.get(SETTINGS_SECTION_MESSAGE_COUNT),
                Locale.get(HIGH_RAM_MODE),
                Locale.get(NATIVE_FILE_PICKER),
                Locale.get(AUTO_RECONNECT),
                Locale.get(DEFAULT_HOTKEYS),
                Locale.get(REMAP_HOTKEYS_L),
            }, {
                // Notifications
                Locale.get(NOTIFICATIONS_ALL),
                Locale.get(NOTIFICATIONS_MENTIONS),
                Locale.get(NOTIFICATIONS_DMS),
                Locale.get(NOTIFICATIONS_ALERT),
                Locale.get(NOTIFICATIONS_SOUND),
                Locale.get(NOTIFICATIONS_PIGLER),
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
                { s.ic.nameColors },
                { s.ic.fullscreen },
            }, {
                // Images
                { s.ic.attachFormat },
                { s.ic.attachSize },
                { s.ic.pfpNone, s.ic.pfpSquare, s.ic.pfpCircle, s.ic.pfpCircleHq },
                { s.ic.pfpPlaceholder, s.ic.pfp16, s.ic.pfp32 },
                { s.ic.iconSize },
                { s.ic.menuIcons },
            }, {
                // Behavior
                { s.ic.msgCount },
                { s.ic.keepChLoaded },
                { s.ic.nativePicker },
                { s.ic.autoReconnect },
                { s.ic.keysDefault },
                { s.ic.keys },
            }, {
                // Notifications
                { s.ic.msgCount },
                { s.ic.notifyPing },
                { s.ic.notifyDM },
                { s.ic.notifyAlert },
                { s.ic.notifySound },
                { null },
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
            }, {
                // Images
                { "PNG", "JPEG" },
                { "0" },
                { Locale.get(PFP_OFF), Locale.get(PFP_SQUARE), Locale.get(PFP_CIRCLE), Locale.get(PFP_CIRCLE_HQ) },
                { Locale.get(PFP_PLACEHOLDER), Locale.get(PFP_16PX), Locale.get(PFP_32PX) },
                { "0" },
                boolValues,
            }, {
                // Behavior
                { "0" },
                boolValues,
                boolValues,
                boolValues,
                boolValues,
                null,
            }, {
                // Notifications
                boolValues,
                boolValues,
                boolValues,
                boolValues,
                boolValues,
                boolValues,
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
            }, {
                // Images
                s.useJpeg ? 1 : 0,
                s.attachmentSize,
                s.pfpType,
                s.pfpSize,
                s.menuIconSize,
                s.showMenuIcons ? 1 : 0,
            }, {
                // Behavior
                s.messageLoadCount,
                s.highRamMode ? 1 : 0,
                s.nativeFilePicker ? 1 : 0,
                s.autoReConnect ? 1 : 0,
                s.defaultHotkeys ? 1 : 0,
                0
            }, {
                // Notifications
                s.showNotifsAll ? 1 : 0, 
                s.showNotifsPings ? 1 : 0,
                s.showNotifsDMs ? 1 : 0,
                s.showNotifAlert ? 1 : 0,
                s.playNotifSound ? 1 : 0,
                s.showNotifPigler ? 1 : 0,
            }
        };
        showMainScreen();
    }

    private String getValueLabel(int section, int item) {
        String[] itemValueLabels = valueLabels[section][item];
        int value = values[section][item];

        if (itemValueLabels == null) {
            return null;
        } else {
            return (itemValueLabels.length > value) ?
                itemValueLabels[value] :
                Integer.toString(value);
        }
    }

    private Image getIcon(int section, int item) {
        Image[] itemIcons = icons[section][item];
        int value = values[section][item];
        return (itemIcons.length > value) ? itemIcons[value] : itemIcons[0];
    }

    private void updateMenuItem(int index) {
        set(
            index,
            labels[currentSection][index],
            getValueLabel(currentSection, index),
            getIcon(currentSection, index),
            false
        );
    }

    private void cycleValue(int direction) {
        int selected = getSelectedIndex();
        values[currentSection][selected] += direction;
        
        int max = maxValues[currentSection][selected];
        
        if (values[currentSection][selected] < 0) {
            values[currentSection][selected] = max;
        }
        else if (values[currentSection][selected] > max) {
            values[currentSection][selected] = 0;
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
        for (int i = 0; i < labels[index].length; i++) {
            append(labels[index][i], getValueLabel(index, i), getIcon(index, i), false);
        }
    }

    private void showTextBox(int index) {
        SettingsScreen.instance = this;

        int max = maxValues[currentSection][index];
        int maxLength = (max == 0) ? 10 : Integer.toString(max).length();

        TextBox tb = new TextBox(
            labels[currentSection][index],
            Integer.toString(values[currentSection][index]),
            maxLength,
            TextField.NUMERIC
        );
        tb.addCommand(textBoxOkCommand);
        tb.addCommand(cancelCommand);
        tb.setCommandListener(this);
        s.disp.setCurrent(tb);
    }

    public void customKeyEvent(int keycode) {
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
            if (isInSubmenu) {
                // In submenu: select item
                if (currentSection == 2 && selected == 5) {
                    // Special case for "remap hotkeys" option - open separate menu
                    s.disp.setCurrent(new KeyMapper(s));
                } else {
                    int max = maxValues[currentSection][selected];
                    
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
                s.theme = values[0][0];
                s.authorFontSize = values[0][1];
                s.messageFontSize = values[0][2];
                s.showRefMessage = values[0][3] == 1;
                s.use12hTime = values[0][4] == 1;
                s.useNameColors = values[0][5] == 1;
                s.fullscreenDefault = values[0][6] == 1;

                s.useJpeg = values[1][0] == 1;
                s.attachmentSize = values[1][1];
                s.pfpType = values[1][2];
                s.pfpSize = values[1][3];
                s.menuIconSize = values[1][4];
                s.showMenuIcons = values[1][5] == 1;

                s.messageLoadCount = values[2][0];
                s.highRamMode = values[2][1] == 1;
                s.nativeFilePicker = values[2][2] == 1;
                s.autoReConnect = values[2][3] == 1;
                s.defaultHotkeys = values[2][4] == 1;

                s.showNotifsAll = values[3][0] == 1;
                s.showNotifsPings = values[3][1] == 1;
                s.showNotifsDMs = values[3][2] == 1;
                s.showNotifAlert = values[3][3] == 1;
                s.playNotifSound = values[3][4] == 1;
                s.showNotifPigler = values[3][5] == 1;

                s.iconCache = new IconCache(s);
                if (s.gatewayActive()) s.gateway.checkInitPigler();
                LoginSettings.save(s);
                s.ic = null;
                s.ic = new Icons(s);
                s.loadTheme();
                s.loadFonts();
            }
            s.disp.setCurrent(MainMenu.get(s));
        }
        else {
            // OK or cancel command in textbox screen: (if OK, write entered value into values array), then return to where we left off in the settings screen
            if (c == textBoxOkCommand) {
                int selected = getSelectedIndex();
                int max = maxValues[currentSection][selected];
                try {
                    int value = Integer.parseInt(((TextBox) d).getString());
                    if (value < 1 || value > max) throw new Exception();

                    // Special case for menu icon size:
                    // 1 and 2 are reserved values that older versions used for 16 and 32 px
                    if (currentSection == 1 && selected == 4 && (value == 1 || value == 2)) {
                        value = 3;
                    }

                    values[currentSection][selected] = value;
                    updateMenuItem(selected);
                }
                catch (Exception e) {
                    s.error(Locale.get(SETTINGS_ERROR_INVALID_NUMBER) + max);
                    return;
                }
            }
            s.disp.setCurrent(SettingsScreen.instance);
            SettingsScreen.instance = null;
        }
    }
}