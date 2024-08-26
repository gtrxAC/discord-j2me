package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsForm extends Form implements CommandListener, ItemCommandListener, Strings {
    State s;

    private ChoiceGroup themeGroup;
    private ChoiceGroup uiGroup;
    private ChoiceGroup authorFontGroup;
    private ChoiceGroup messageFontGroup;
    private TextField messageCountField;
    private ChoiceGroup formatGroup;
    private TextField attachSizeField;
    private ChoiceGroup iconGroup;
    private ChoiceGroup pfpSizeGroup;
    private TextField menuIconField;
    private ChoiceGroup refMsgGroup;
    private ChoiceGroup hotkeyGroup;
    private StringItem keyMapperItem;
    private StringItem languageItem;
    private Command saveCommand;
    private Command cancelCommand;
    private Command openMapperCommand;
    private Command setLanguageCommand;

    private void createHeading(Image icon, int stringId) {
        ImageItem img = new ImageItem(null, icon, 0, null);
        img.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
        append(img);
        StringItem str = new StringItem(null, Locale.get(stringId));
        str.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        append(str);
    }

    public SettingsForm(State s) {
        super(Locale.get(SETTINGS_FORM_TITLE));
        setCommandListener(this); 
        this.s = s;

        String[] themeChoices = {
            Locale.get(THEME_DARK),
            Locale.get(THEME_LIGHT),
            Locale.get(THEME_BLACK)
        };
        Image[] themeImages = {
            s.ic.themeDark,
            s.ic.themeLight,
            s.ic.themeBlack
        };
        createHeading(s.ic.themesGroup, SETTINGS_SECTION_THEMES);
        themeGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, themeChoices, themeImages);
        themeGroup.setSelectedIndex(s.theme, true);
        append(themeGroup);

        String[] uiChoices = {
            Locale.get(USE_12H_TIME),
            Locale.get(NATIVE_FILE_PICKER),
            Locale.get(AUTO_RECONNECT),
            Locale.get(GUILD_ICONS),
            Locale.get(NAME_COLORS),
        };
        Image[] uiImages = {
            s.ic.use12h,
            s.ic.nativePicker,
            s.ic.autoReconnect,
            s.ic.menuIcons,
            s.ic.nameColors
        };
        createHeading(s.ic.uiGroup, SETTINGS_SECTION_UI);
        uiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, uiChoices, uiImages);
        uiGroup.setSelectedIndex(0, s.use12hTime);
        uiGroup.setSelectedIndex(1, s.nativeFilePicker);
        uiGroup.setSelectedIndex(2, s.autoReConnect);
        uiGroup.setSelectedIndex(3, s.showMenuIcons);
        uiGroup.setSelectedIndex(4, s.useNameColors);
        append(uiGroup);

        String[] fontChoices = {
            Locale.get(FONT_SMALL),
            Locale.get(FONT_MEDIUM),
            Locale.get(FONT_LARGE),
        };
        Image[] fontImages = {
            s.ic.fontSmall,
            s.ic.fontMedium,
            s.ic.fontLarge
        };
        createHeading(s.ic.fontSize, SETTINGS_SECTION_AUTHOR_FONT);
        authorFontGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        authorFontGroup.setSelectedIndex(s.authorFontSize, true);
        append(authorFontGroup);

        createHeading(s.ic.fontSize, SETTINGS_SECTION_CONTENT_FONT);
        messageFontGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        messageFontGroup.setSelectedIndex(s.messageFontSize, true);
        append(messageFontGroup);

        createHeading(s.ic.msgCount, SETTINGS_SECTION_MESSAGE_COUNT);
        messageCountField = new TextField(null, new Integer(s.messageLoadCount).toString(), 3, TextField.NUMERIC);
        append(messageCountField);

        String[] formatChoices = {"PNG", "JPEG"};
        Image[] formatImages = {null, null};
        createHeading(s.ic.attachFormat, SETTINGS_SECTION_IMAGE_FORMAT);
        formatGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, formatChoices, formatImages);
        formatGroup.setSelectedIndex(s.useJpeg ? 1 : 0, true);
        append(formatGroup);

        createHeading(s.ic.attachSize, SETTINGS_SECTION_IMAGE_SIZE);
        attachSizeField = new TextField(null, new Integer(s.attachmentSize).toString(), 5, TextField.NUMERIC);
        append(attachSizeField);

        String[] iconChoices = {
            Locale.get(PFP_OFF),
            Locale.get(PFP_SQUARE),
            Locale.get(PFP_CIRCLE),
            Locale.get(PFP_CIRCLE_HQ),
        };
        Image[] iconImages = {
            s.ic.pfpNone,
            s.ic.pfpSquare, 
            s.ic.pfpCircle,
            s.ic.pfpCircleHq
        };
        createHeading(s.ic.pfpType, SETTINGS_SECTION_PFP_SHAPE);
        iconGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, iconChoices, iconImages);
        iconGroup.setSelectedIndex(s.pfpType, true);
        append(iconGroup);

        String[] pfpSizeChoices = {
            Locale.get(PFP_PLACEHOLDER),
            Locale.get(PFP_16PX),
            Locale.get(PFP_32PX),
        };
        Image[] pfpSizeImages = {
            s.ic.pfpPlaceholder,
            s.ic.pfp16,
            s.ic.pfp32
        };
        createHeading(s.ic.pfpSize, SETTINGS_SECTION_PFP_RESOLUTION);
        pfpSizeGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, pfpSizeChoices, pfpSizeImages);
        pfpSizeGroup.setSelectedIndex(s.pfpSize, true);
        append(pfpSizeGroup);

        createHeading(s.ic.iconSize, SETTINGS_SECTION_MENU_ICONS);
        menuIconField = new TextField(null, new Integer(s.menuIconSize).toString(), 3, TextField.NUMERIC);
        append(menuIconField);

        String[] refMsgChoices = {
            Locale.get(REPLIES_ONLY_RECIPIENT),
            Locale.get(REPLIES_FULL_MESSAGE)
        };
        Image[] refMsgImages = {
            s.ic.repliesName,
            s.ic.repliesFull
        };
        createHeading(s.ic.repliesGroup, SETTINGS_SECTION_REPLIES);
        refMsgGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, refMsgChoices, refMsgImages);
        refMsgGroup.setSelectedIndex(s.showRefMessage ? 1 : 0, true);
        append(refMsgGroup);

        String[] hotkeyChoices = {Locale.get(DEFAULT_HOTKEYS)};
        Image[] hotkeyImages = {s.ic.keysDefault};
        createHeading(s.ic.keys, SETTINGS_SECTION_HOTKEYS);
        hotkeyGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, hotkeyChoices, hotkeyImages);
        hotkeyGroup.setSelectedIndex(0, s.defaultHotkeys);
        append(hotkeyGroup);

        openMapperCommand = Locale.createCommand(REMAP_HOTKEYS, Command.ITEM, 2);
        keyMapperItem = new StringItem(null, Locale.get(REMAP_HOTKEYS_L), Item.BUTTON);
        keyMapperItem.setDefaultCommand(openMapperCommand);
        keyMapperItem.setItemCommandListener(this);
        append(keyMapperItem);

        createHeading(s.ic.language, SETTINGS_SECTION_LANGUAGE);
        setLanguageCommand = Locale.createCommand(SET_LANGUAGE, Command.ITEM, 2);
        languageItem = new StringItem(null, Locale.get(SET_LANGUAGE_L), Item.BUTTON);
        languageItem.setDefaultCommand(setLanguageCommand);
        languageItem.setItemCommandListener(this);
        append(languageItem);

        saveCommand = Locale.createCommand(SAVE, Command.OK, 0);
        cancelCommand = Locale.createCommand(CANCEL, Command.BACK, 1);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            try {
                s.theme = themeGroup.getSelectedIndex();
                s.authorFontSize = authorFontGroup.getSelectedIndex();
                s.messageFontSize = messageFontGroup.getSelectedIndex();
                s.useJpeg = formatGroup.getSelectedIndex() == 1;
                s.pfpType = iconGroup.getSelectedIndex();
                s.pfpSize = pfpSizeGroup.getSelectedIndex();
                s.iconCache = new IconCache(s);
                s.showRefMessage = refMsgGroup.getSelectedIndex() == 1;

                try {
                    int newSize = Integer.parseInt(menuIconField.getString());

                    // Icon size is stored in save data as a byte (0-255) to retain compatibility
                    if (newSize > 255) throw new Exception();

                    // 1 and 2 are reserved values that older versions used for 16 and 32 px
                    if (newSize == 1 || newSize == 2) s.menuIconSize = 3;
                    else s.menuIconSize = newSize;
                }
                catch (Exception e) {
                    s.menuIconSize = 16;
                }

                try {
                    int newCount = Integer.parseInt(messageCountField.getString());
                    if (newCount < 1 || newCount > 100) throw new Exception();
                    s.messageLoadCount = newCount;
                }
                catch (Exception e) {
                    s.messageLoadCount = 20;
                }

                try {
                    int newSize = Integer.parseInt(attachSizeField.getString());
                    if (newSize < 1) throw new Exception();
                    s.attachmentSize = newSize;
                }
                catch (Exception e) {
                    s.attachmentSize = 1000;
                }

                boolean[] selected = {false, false, false, false, false, false};
                uiGroup.getSelectedFlags(selected);
                s.use12hTime = selected[0];
                s.nativeFilePicker = selected[1];
                s.autoReConnect = selected[2];
                s.showMenuIcons = selected[3];
                s.useNameColors = selected[4];

                hotkeyGroup.getSelectedFlags(selected);
                s.defaultHotkeys = selected[0];

                LoginSettings.save(s);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        s.ic = null;
        s.ic = new Icons(s);

        s.loadFonts();
        s.disp.setCurrent(new MainMenu(s));
    }
    
    public void commandAction(Command c, Item i) {
        if (c == openMapperCommand) {
            s.disp.setCurrent(new KeyMapper(s));
        }
        else if (c == setLanguageCommand) {
            s.disp.setCurrent(new LanguageSelector(s));
        }
    }
}
