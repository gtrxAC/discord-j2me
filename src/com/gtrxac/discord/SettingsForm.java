package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class SettingsForm extends Form implements CommandListener, ItemCommandListener {
    State s;
    private RecordStore loginRms;

    private ChoiceGroup themeGroup;
    private ChoiceGroup uiGroup;
    private ChoiceGroup authorFontGroup;
    private ChoiceGroup messageFontGroup;
    private TextField messageCountField;
    private ChoiceGroup formatGroup;
    private TextField attachSizeField;
    private ChoiceGroup iconGroup;
    private ChoiceGroup iconSizeGroup;
    private ChoiceGroup refMsgGroup;
    private ChoiceGroup hotkeyGroup;
    private StringItem keyMapperItem;
    private Command saveCommand;
    private Command cancelCommand;
    private Command openMapperCommand;

    private void createHeading(Image icon, String text) {
        append(new ImageItem(null, icon, 0, null));
        append(new StringItem(null, text));
    }

    public SettingsForm(State s) {
        super("Settings");
        setCommandListener(this); 
        this.s = s;

        String[] themeChoices = {"Dark", "Light", "Black"};
        Image[] themeImages = {s.ic.themeDark, s.ic.themeLight, s.ic.themeBlack};
        createHeading(s.ic.themesGroup, "Theme");
        themeGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, themeChoices, themeImages);
        themeGroup.setSelectedIndex(s.theme, true);
        append(themeGroup);

        String[] uiChoices = {"Use old UI", "Use 12-hour time", "Native file picker", "Gateway auto reconnect", "Show icons in menus", "Show name colors"};
        Image[] uiImages = {s.ic.oldUI, s.ic.use12h, s.ic.nativePicker, s.ic.autoReconnect, s.ic.menuIcons, s.ic.nameColors};
        createHeading(s.ic.uiGroup, "User interface");
        uiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, uiChoices, uiImages);
        uiGroup.setSelectedIndex(0, s.oldUI);
        uiGroup.setSelectedIndex(1, s.use12hTime);
        uiGroup.setSelectedIndex(2, s.nativeFilePicker);
        uiGroup.setSelectedIndex(3, s.autoReConnect);
        uiGroup.setSelectedIndex(4, s.showMenuIcons);
        uiGroup.setSelectedIndex(5, s.useNameColors);
        append(uiGroup);

        String[] fontChoices = {"Small", "Medium", "Large"};
        Image[] fontImages = {s.ic.fontSmall, s.ic.fontMedium, s.ic.fontLarge};
        createHeading(s.ic.fontSize, "Message author font");
        authorFontGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        authorFontGroup.setSelectedIndex(s.authorFontSize, true);
        append(authorFontGroup);

        createHeading(s.ic.fontSize, "Message content font");
        messageFontGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        messageFontGroup.setSelectedIndex(s.messageFontSize, true);
        append(messageFontGroup);

        createHeading(s.ic.msgCount, "Message load count");
        messageCountField = new TextField(null, new Integer(s.messageLoadCount).toString(), 3, TextField.NUMERIC);
        append(messageCountField);

        String[] formatChoices = {"PNG", "JPEG"};
        Image[] formatImages = {null, null};
        createHeading(s.ic.attachFormat, "Attachment format");
        formatGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, formatChoices, formatImages);
        formatGroup.setSelectedIndex(s.useJpeg ? 1 : 0, true);
        append(formatGroup);

        createHeading(s.ic.attachSize, "Max. attachment size");
        attachSizeField = new TextField(null, new Integer(s.attachmentSize).toString(), 5, TextField.NUMERIC);
        append(attachSizeField);

        String[] iconChoices = {"Off", "Square", "Circle", "Circle (HQ)"};
        Image[] iconImages = {s.ic.pfpNone, s.ic.pfpSquare, s.ic.pfpCircle, s.ic.pfpCircleHq};
        createHeading(s.ic.pfpType, "Profile pictures");
        iconGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, iconChoices, iconImages);
        iconGroup.setSelectedIndex(s.iconType, true);
        append(iconGroup);

        String[] iconSizeChoices = {"Placeholders only", "16 px", "32 px"};
        Image[] iconSizeImages = {s.ic.pfpPlaceholder, s.ic.pfp16, s.ic.pfp32};
        createHeading(s.ic.pfpSize, "Icon size");
        iconSizeGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, iconSizeChoices, iconSizeImages);
        iconSizeGroup.setSelectedIndex(s.iconSize, true);
        append(iconSizeGroup);

        String[] refMsgChoices = {"Only recipient", "Full message"};
        Image[] refMsgImages = {s.ic.repliesName, s.ic.repliesFull};
        createHeading(s.ic.repliesGroup, "Show replies as");
        refMsgGroup = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE, refMsgChoices, refMsgImages);
        refMsgGroup.setSelectedIndex(s.showRefMessage ? 1 : 0, true);
        append(refMsgGroup);

        String[] hotkeyChoices = {"Use default hotkeys"};
        Image[] hotkeyImages = {s.ic.keysDefault};
        createHeading(s.ic.keys, "Hotkeys");
        hotkeyGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, hotkeyChoices, hotkeyImages);
        hotkeyGroup.setSelectedIndex(0, s.defaultHotkeys);
        append(hotkeyGroup);

        openMapperCommand = new Command("Map keys", Command.ITEM, 2);
        keyMapperItem = new StringItem(null, "Remap hotkeys", Item.BUTTON);
        keyMapperItem.setDefaultCommand(openMapperCommand);
        keyMapperItem.setItemCommandListener(this);
        append(keyMapperItem);

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    private void setRecord(int index, byte[] value) throws Exception {
        if (loginRms.getNumRecords() >= index) {
            loginRms.setRecord(index, value, 0, value.length);
        } else {
            loginRms.addRecord(value, 0, value.length);
        }
    }

    private void setByteRecord(int index, int value) throws Exception {
        byte[] record = {new Integer(value).byteValue()};
        setRecord(index, record);
    }

    private void setBoolRecord(int index, boolean value) throws Exception {
        setByteRecord(index, value ? 1 : 0);
    }

    private void setIntRecord(int index, int value) throws Exception {
        byte[] record = new Integer(value).toString().getBytes();
        setRecord(index, record);
    }

    public void saveKeyMappings() {
        try {
            loginRms = RecordStore.openRecordStore("login", true);
            setIntRecord(22, s.sendHotkey);
            setIntRecord(23, s.replyHotkey);
            setIntRecord(24, s.copyHotkey);
            setIntRecord(25, s.refreshHotkey);
            setIntRecord(26, s.backHotkey);
            loginRms.closeRecordStore();
        }
        catch (Exception e) {
            s.error(e);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            try {
                s.theme = themeGroup.getSelectedIndex();
                s.authorFontSize = authorFontGroup.getSelectedIndex();
                s.messageFontSize = messageFontGroup.getSelectedIndex();
                s.useJpeg = formatGroup.getSelectedIndex() == 1;
                s.iconType = iconGroup.getSelectedIndex();
                s.iconSize = iconSizeGroup.getSelectedIndex();
                s.iconCache = new IconCache(s);
                s.showRefMessage = refMsgGroup.getSelectedIndex() == 1;

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
                s.oldUI = selected[0];
                s.use12hTime = selected[1];
                s.nativeFilePicker = selected[2];
                s.autoReConnect = selected[3];
                s.showMenuIcons = selected[4];
                s.useNameColors = selected[5];

                hotkeyGroup.getSelectedFlags(selected);
                s.defaultHotkeys = selected[0];

                loginRms = RecordStore.openRecordStore("login", true);
                setByteRecord(3, s.theme);
                setBoolRecord(4, s.oldUI);
                setByteRecord(6, s.authorFontSize);
                setByteRecord(7, s.messageFontSize);
                setBoolRecord(8, s.use12hTime);
                setByteRecord(9, s.messageLoadCount);
                setBoolRecord(12, s.useJpeg);
                setByteRecord(14, s.iconType);
                setIntRecord(15, s.attachmentSize);
                setByteRecord(16, s.iconSize);
                setBoolRecord(17, s.nativeFilePicker);
                setBoolRecord(18, s.autoReConnect);
                setBoolRecord(19, s.showMenuIcons);
                setBoolRecord(21, s.useNameColors);
                setBoolRecord(27, s.showRefMessage);
                setBoolRecord(28, s.defaultHotkeys);
                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        s.ic = null;
        s.ic = new Icons(s.iconSize == 2);

        s.loadFonts();
        s.disp.setCurrent(new MainMenu(s));
    }
    
    public void commandAction(Command c, Item i) {
        if (c == openMapperCommand) {
            s.disp.setCurrent(new KeyMapper(s));
        }
    }
}
