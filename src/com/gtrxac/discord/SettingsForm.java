package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class SettingsForm extends Form implements CommandListener {
    State s;
    private RecordStore loginRms;

    private ChoiceGroup themeGroup;
    private ChoiceGroup uiGroup;
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

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);

        append(themeGroup);
        append(uiGroup);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            try {
                s.theme = themeGroup.getSelectedIndex();

                boolean[] selected = {false};
                uiGroup.getSelectedFlags(selected);
                s.oldUI = selected[0];

                loginRms = RecordStore.openRecordStore("login", true);
                byte[] themeRecord = {new Integer(s.theme).byteValue()};
                byte[] uiRecord = {new Integer(s.oldUI ? 1 : 0).byteValue()};

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

                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        s.openGuildSelector(false);
    }
}
