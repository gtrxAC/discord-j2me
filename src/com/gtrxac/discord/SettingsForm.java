package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsForm extends Form implements CommandListener {
    private State s;

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

        String[] themeChoices = {"Monochrome", "Dark", "Light"};
        themeGroup = new ChoiceGroup("Theme", ChoiceGroup.EXCLUSIVE, themeChoices, null);
        themeGroup.setSelectedIndex(s.theme, true);
        append(themeGroup);

        String[] uiChoices = {"12-hour time"};
        uiGroup = new ChoiceGroup("User interface", ChoiceGroup.MULTIPLE, uiChoices, null);
        uiGroup.setSelectedIndex(0, s.use12hTime);
        append(uiGroup);

        String[] fontChoices = {"Small", "Medium", "Large"};
        authorFontGroup = new ChoiceGroup("Message author font", ChoiceGroup.EXCLUSIVE, fontChoices, null);
        authorFontGroup.setSelectedIndex(s.authorFontSize, true);
        append(authorFontGroup);

        messageFontGroup = new ChoiceGroup("Message content font", ChoiceGroup.EXCLUSIVE, fontChoices, null);
        messageFontGroup.setSelectedIndex(s.messageFontSize, true);
        append(messageFontGroup);

        messageCountField = new TextField("Message load count", new Integer(s.messageLoadCount).toString(), 3, TextField.NUMERIC);
        append(messageCountField);

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
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
            s.use12hTime = selected[0];

            LoginSettings.save(s);
        }
        s.loadFonts();
        s.disp.setCurrent(new MainMenu(s));
    }
}
