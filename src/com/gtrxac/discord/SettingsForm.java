package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class SettingsForm extends Form implements CommandListener {
    State s;
    private RecordStore loginRms;

    private ChoiceGroup choiceGroup;
    private Command saveCommand;
    private Command cancelCommand;

    public SettingsForm(State s) {
        super("Log in");
        setCommandListener(this); 
        this.s = s;

        String[] choices = {"Light theme"};
        Image[] images = {null};
        choiceGroup = new ChoiceGroup("Settings", ChoiceGroup.MULTIPLE, choices, images);
        choiceGroup.setSelectedIndex(0, s.lightTheme);

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);

        append(choiceGroup);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            try {
                boolean[] selected = {false};
                choiceGroup.getSelectedFlags(selected);
                s.lightTheme = selected[0];

                loginRms = RecordStore.openRecordStore("login", true);
                byte[] newRecord = {new Integer(s.lightTheme ? 1 : 0).byteValue()};

                if (loginRms.getNumRecords() >= 3) {
                    loginRms.setRecord(3, newRecord, 0, 1);
                } else {
                    loginRms.addRecord(newRecord, 0, 1);
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
