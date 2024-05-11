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

        String[] uiChoices = {"Use old UI", "Use 12-hour time"};
        Image[] uiImages = {null, null};
        uiGroup = new ChoiceGroup("User interface", ChoiceGroup.MULTIPLE, uiChoices, uiImages);
        uiGroup.setSelectedIndex(0, s.oldUI);
        uiGroup.setSelectedIndex(1, s.use12hTime);

        String[] fontChoices = {"Small", "Medium", "Large"};
        Image[] fontImages = {null, null, null};
        authorFontGroup = new ChoiceGroup("Message author font", ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        authorFontGroup.setSelectedIndex(s.authorFontSize, true);
        messageFontGroup = new ChoiceGroup("Message content font", ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        messageFontGroup.setSelectedIndex(s.messageFontSize, true);

        messageCountField = new TextField("Message load count", new Integer(s.messageLoadCount).toString(), 3, TextField.NUMERIC);

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);

        append(themeGroup);
        append(uiGroup);
        append(authorFontGroup);
        append(messageFontGroup);
        append(messageCountField);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            try {
                s.theme = themeGroup.getSelectedIndex();
                s.authorFontSize = authorFontGroup.getSelectedIndex();
                s.messageFontSize = messageFontGroup.getSelectedIndex();

                try {
                    int newCount = Integer.parseInt(messageCountField.getString());
                    if (newCount < 1 || newCount > 100) throw new Exception();
                    s.messageLoadCount = newCount;
                }
                catch (Exception e) {
                    s.messageLoadCount = 20;
                }

                boolean[] selected = {false, false};
                uiGroup.getSelectedFlags(selected);
                s.oldUI = selected[0];
                s.use12hTime = selected[1];

                loginRms = RecordStore.openRecordStore("login", true);
                byte[] themeRecord = {new Integer(s.theme).byteValue()};
                byte[] uiRecord = {new Integer(s.oldUI ? 1 : 0).byteValue()};
                byte[] use12hRecord = {new Integer(s.use12hTime ? 1 : 0).byteValue()};
                byte[] authorFontRecord = {new Integer(s.authorFontSize).byteValue()};
                byte[] messageFontRecord = {new Integer(s.messageFontSize).byteValue()};
                byte[] messageCountRecord = {new Integer(s.messageLoadCount).byteValue()};

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

                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        s.loadFonts();
        s.openGuildSelector(false);
    }
}
