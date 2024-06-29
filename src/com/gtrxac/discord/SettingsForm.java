package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class SettingsForm extends Form implements CommandListener {
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
    private Command saveCommand;
    private Command cancelCommand;

    public SettingsForm(State s) {
        super("Settings");
        setCommandListener(this); 
        this.s = s;

        String[] themeChoices = {"Dark", "Light", "Black"};
        Image[] themeImages = {null, null, null};
        themeGroup = new ChoiceGroup("Theme", ChoiceGroup.EXCLUSIVE, themeChoices, themeImages);
        themeGroup.setSelectedIndex(s.theme, true);

        String[] uiChoices = {"Use old UI", "Use 12-hour time", "Native file picker", "Gateway auto reconnect"};
        Image[] uiImages = {null, null, null, null};
        uiGroup = new ChoiceGroup("User interface", ChoiceGroup.MULTIPLE, uiChoices, uiImages);
        uiGroup.setSelectedIndex(0, s.oldUI);
        uiGroup.setSelectedIndex(1, s.use12hTime);
        uiGroup.setSelectedIndex(2, s.nativeFilePicker);
        uiGroup.setSelectedIndex(3, s.autoReConnect);

        String[] fontChoices = {"Small", "Medium", "Large"};
        Image[] fontImages = {null, null, null};
        authorFontGroup = new ChoiceGroup("Message author font", ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        authorFontGroup.setSelectedIndex(s.authorFontSize, true);
        messageFontGroup = new ChoiceGroup("Message content font", ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        messageFontGroup.setSelectedIndex(s.messageFontSize, true);

        messageCountField = new TextField("Message load count", new Integer(s.messageLoadCount).toString(), 3, TextField.NUMERIC);

        String[] formatChoices = {"PNG", "JPEG"};
        Image[] formatImages = {null, null};
        formatGroup = new ChoiceGroup("Attachment format", ChoiceGroup.EXCLUSIVE, formatChoices, formatImages);
        formatGroup.setSelectedIndex(s.useJpeg ? 1 : 0, true);

        attachSizeField = new TextField("Max. attachment size", new Integer(s.attachmentSize).toString(), 5, TextField.NUMERIC);

        String[] iconChoices = {"Off", "Square", "Circle", "Circle (HQ)"};
        Image[] iconImages = {null, null, null, null};
        iconGroup = new ChoiceGroup("Icons and avatars", ChoiceGroup.EXCLUSIVE, iconChoices, iconImages);
        iconGroup.setSelectedIndex(s.iconType, true);

        String[] iconSizeChoices = {"Placeholders only", "16 px", "32 px"};
        Image[] iconSizeImages = {null, null, null};
        iconSizeGroup = new ChoiceGroup("Icon size", ChoiceGroup.EXCLUSIVE, iconSizeChoices, iconSizeImages);
        iconSizeGroup.setSelectedIndex(s.iconSize, true);

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);

        append(themeGroup);
        append(uiGroup);
        append(authorFontGroup);
        append(messageFontGroup);
        append(messageCountField);
        append(formatGroup);
        append(attachSizeField);
        append(iconGroup);
        append(iconSizeGroup);
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
                s.iconType = iconGroup.getSelectedIndex();
                s.iconSize = iconSizeGroup.getSelectedIndex();
                s.iconCache = new IconCache(s);

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

                boolean[] selected = {false, false, false, false};
                uiGroup.getSelectedFlags(selected);
                s.oldUI = selected[0];
                s.use12hTime = selected[1];
                s.nativeFilePicker = selected[2];
                s.autoReConnect = selected[3];

                loginRms = RecordStore.openRecordStore("login", true);
                byte[] themeRecord = {new Integer(s.theme).byteValue()};
                byte[] uiRecord = {new Integer(s.oldUI ? 1 : 0).byteValue()};
                byte[] use12hRecord = {new Integer(s.use12hTime ? 1 : 0).byteValue()};
                byte[] authorFontRecord = {new Integer(s.authorFontSize).byteValue()};
                byte[] messageFontRecord = {new Integer(s.messageFontSize).byteValue()};
                byte[] messageCountRecord = {new Integer(s.messageLoadCount).byteValue()};
                byte[] jpegRecord = {new Integer(s.useJpeg ? 1 : 0).byteValue()};
                byte[] iconTypeRecord = {new Integer(s.iconType).byteValue()};
                byte[] iconSizeRecord = {new Integer(s.iconSize).byteValue()};
                byte[] attachSizeRecord = new Integer(s.attachmentSize).toString().getBytes();
                byte[] nativePickerRecord = {new Integer(s.nativeFilePicker ? 1 : 0).byteValue()};
                byte[] autoReConnectRecord = {new Integer(s.autoReConnect ? 1 : 0).byteValue()};

                if (loginRms.getNumRecords() >= 3) {
                    loginRms.setRecord(3, themeRecord, 0, 1);
                } else {
                    loginRms.addRecord(themeRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 4) {
                    loginRms.setRecord(4, uiRecord, 0, 1);
                } else {
                    loginRms.addRecord(uiRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 7) {
                    loginRms.setRecord(6, authorFontRecord, 0, 1);
                    loginRms.setRecord(7, messageFontRecord, 0, 1);
                } else {
                    loginRms.addRecord(authorFontRecord, 0, 1);
                    loginRms.addRecord(messageFontRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 8) {
                    loginRms.setRecord(8, use12hRecord, 0, 1);
                } else {
                    loginRms.addRecord(use12hRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 9) {
                    loginRms.setRecord(9, messageCountRecord, 0, 1);
                } else {
                    loginRms.addRecord(messageCountRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 12) {
                    loginRms.setRecord(12, jpegRecord, 0, 1);
                } else {
                    loginRms.addRecord(jpegRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 14) {
                    loginRms.setRecord(14, iconTypeRecord, 0, 1);
                } else {
                    loginRms.addRecord(iconTypeRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 15) {
                    loginRms.setRecord(15, attachSizeRecord, 0, attachSizeRecord.length);
                } else {
                    loginRms.addRecord(attachSizeRecord, 0, attachSizeRecord.length);
                }

                if (loginRms.getNumRecords() >= 16) {
                    loginRms.setRecord(16, iconSizeRecord, 0, 1);
                } else {
                    loginRms.addRecord(iconSizeRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 17) {
                    loginRms.setRecord(17, nativePickerRecord, 0, 1);
                } else {
                    loginRms.addRecord(nativePickerRecord, 0, 1);
                }

                if (loginRms.getNumRecords() >= 18) {
                    loginRms.setRecord(18, autoReConnectRecord, 0, 1);
                } else {
                    loginRms.addRecord(autoReConnectRecord, 0, 1);
                }

                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        s.loadFonts();
        s.disp.setCurrent(new MainMenu(s));
    }
}
