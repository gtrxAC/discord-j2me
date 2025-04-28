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

        if (App.disp.isColor()) {
            String[] themeChoices = {"Monochrome", "Dark", "Light"};
            themeGroup = new ChoiceGroup("Theme", ChoiceGroup.EXCLUSIVE, themeChoices, null);
            themeGroup.setSelectedIndex(App.theme, true);
            append(themeGroup);
        }

        String[] uiChoices = {"12-hour time", "List timestamps"};
        uiGroup = new ChoiceGroup("User interface", ChoiceGroup.MULTIPLE, uiChoices, null);
        uiGroup.setSelectedIndex(0, App.use12hTime);
        uiGroup.setSelectedIndex(1, App.listTimestamps);
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

        saveCommand = new Command("Save", Command.BACK, 0);
        cancelCommand = new Command("Cancel", Command.BACK, 1);
        addCommand(saveCommand);
        addCommand(cancelCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCommand) {
            App.theme = App.disp.isColor() ? themeGroup.getSelectedIndex() : 0;
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

            App.use12hTime = uiGroup.isSelected(0);
            App.listTimestamps = uiGroup.isSelected(1);
            Settings.save();
            App.login();
        } else {
            App.disp.setCurrent(new MainMenu());
        }
    }
}
