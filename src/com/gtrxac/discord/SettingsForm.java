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
    private Command saveCommand;
    private Command cancelCommand;

    public SettingsForm(State s) {
        super("Log in");
        setCommandListener(this); 
        this.s = s;

        String[] themeChoices = {"Dark", "Light", "Black"};
        Image[] themeImages = {null, null, null};
        themeGroup = new ChoiceGroup("Theme", ChoiceGroup.EXCLUSIVE, themeChoices, themeImages);
        themeGroup.setSelectedIndex(s.theme, true);

        String[] uiChoices = {"Use old UI"};
        Image[] uiImages = {null};
        uiGroup = new ChoiceGroup("User interface", ChoiceGroup.MULTIPLE, uiChoices, uiImages);
        uiGroup.setSelectedIndex(0, s.oldUI);

        String[] fontChoices = {"Small", "Medium", "Large"};
        Image[] fontImages = {null, null, null};
        authorFontGroup = new ChoiceGroup("Message author font", ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        authorFontGroup.setSelectedIndex(s.authorFontSize, true);
        messageFontGroup = new ChoiceGroup("Message content font", ChoiceGroup.EXCLUSIVE, fontChoices, fontImages);
        messageFontGroup.setSelectedIndex(s.messageFontSize, true);

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);

        append(themeGroup);
        append(uiGroup);
        append(authorFontGroup);
        append(messageFontGroup);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            try {
                s.theme = themeGroup.getSelectedIndex();
                s.authorFontSize = authorFontGroup.getSelectedIndex();
                s.messageFontSize = messageFontGroup.getSelectedIndex();

                boolean[] selected = {false};
                uiGroup.getSelectedFlags(selected);
                s.oldUI = selected[0];

                loginRms = RecordStore.openRecordStore("login", true);
                byte[] themeRecord = {new Integer(s.theme).byteValue()};
                byte[] uiRecord = {new Integer(s.oldUI ? 1 : 0).byteValue()};
                byte[] authorFontRecord = {new Integer(s.authorFontSize).byteValue()};
                byte[] messageFontRecord = {new Integer(s.messageFontSize).byteValue()};

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
