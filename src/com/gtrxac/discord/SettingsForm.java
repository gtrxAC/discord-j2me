package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsForm extends Form implements CommandListener {
    private ChoiceGroup themeGroup;
    private ChoiceGroup uiGroup;
    private ChoiceGroup authorFontGroup;
    private ChoiceGroup messageFontGroup;
    private TextField messageCountField;
    private Command saveCommand;
    private Command cancelCommand;

    public SettingsForm() {
        super("Settings");
        setCommandListener(this);

        String[] themeChoices = {"Monochrome", "Dark", "Light"};
        themeGroup = new ChoiceGroup("Theme", ChoiceGroup.EXCLUSIVE, themeChoices, null);
        themeGroup.setSelectedIndex(App.theme, true);
        append(themeGroup);

        String[] uiChoices = {"12-hour time"};
        uiGroup = new ChoiceGroup("User interface", ChoiceGroup.MULTIPLE, uiChoices, null);
        uiGroup.setSelectedIndex(0, App.use12hTime);
        append(uiGroup);

        String[] fontChoices = {"Small", "Medium", "Large"};
        authorFontGroup = new ChoiceGroup("Author font", ChoiceGroup.EXCLUSIVE, fontChoices, null);
        authorFontGroup.setSelectedIndex(App.authorFontSize, true);
        append(authorFontGroup);

        messageFontGroup = new ChoiceGroup("Message font", ChoiceGroup.EXCLUSIVE, fontChoices, null);
        messageFontGroup.setSelectedIndex(App.messageFontSize, true);
        append(messageFontGroup);

        messageCountField = new TextField("Message count", new Integer(App.messageLoadCount).toString(), 3, TextField.NUMERIC);
        append(messageCountField);

        append(new StringItem("About", "Discord client for Java ME (Nokia 6310i version)\nDeveloped by gtrxAC\nJSON parser by Shinovon"));

        saveCommand = new Command("Save", Command.OK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            App.theme = themeGroup.getSelectedIndex();
            App.authorFontSize = authorFontGroup.getSelectedIndex();
            App.messageFontSize = messageFontGroup.getSelectedIndex();

            try {
                int newCount = Integer.parseInt(messageCountField.getString());
                if (newCount < 1 || newCount > 100) throw new Exception();
                App.messageLoadCount = newCount;
            }
            catch (Exception e) {
                App.messageLoadCount = 20;
            }

            boolean[] selected = {false, false};
            uiGroup.getSelectedFlags(selected);
            App.use12hTime = selected[0];

            Settings.save();
        }
        App.loadFonts();
        App.disp.setCurrent(new MainMenu());
    }
}
