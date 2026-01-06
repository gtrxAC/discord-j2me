package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsForm extends Form implements CommandListener {
    private ChoiceGroup themeGroup;
    private ChoiceGroup uiGroup;
    private ChoiceGroup authorFontGroup;
    private ChoiceGroup messageFontGroup;
    private TextField messageCountField;

    public SettingsForm() {
        super("Settings");
        setCommandListener(this);

        String[] uiChoices = {"12-hour time", "List timestamps", "Mark as read"};
        uiGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, uiChoices, null);
        uiGroup.setSelectedIndex(0, App.use12hTime);
        uiGroup.setSelectedIndex(1, App.listTimestamps);
        uiGroup.setSelectedIndex(2, App.markAsRead);
        append(uiGroup);

        if (App.disp.isColor()) {
            String[] themeChoices = {"Monochrome", "Dark", "Light"};
            themeGroup = new ChoiceGroup("Theme", ChoiceGroup.EXCLUSIVE, themeChoices, null);
            themeGroup.setSelectedIndex(App.theme, true);
            append(themeGroup);
        }

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
        append(new StringItem("Support", "discord.gg/2GKuJjQagp\nt.me/dscforsymbian"));

        addCommand(new Command("Save", Command.BACK, 0));
        addCommand(new Command("Cancel", Command.BACK, 1));
    }

    public void commandAction(Command c, Displayable d) {
        if (c.getPriority() == 0) {
            // save command
            App.theme = App.disp.isColor() ? themeGroup.getSelectedIndex() : 0;
            App.authorFontSize = authorFontGroup.getSelectedIndex();
            App.messageFontSize = messageFontGroup.getSelectedIndex();

            int newCount = 0;
            try {
                newCount = Integer.parseInt(messageCountField.getString());
            }
            catch (Exception e) {}

            App.messageLoadCount = (newCount >= 1 && newCount <= 100) ? newCount : 20;

            App.use12hTime = uiGroup.isSelected(0);
            App.listTimestamps = uiGroup.isSelected(1);
            App.markAsRead = uiGroup.isSelected(2);
            Settings.save();
            App.login();
        } else {
            // cancel command
            App.disp.setCurrent(new MainMenu());
        }
    }
}
