package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class SettingsSectionScreen extends ListScreen implements CommandListener, Strings {
    static SettingsSectionScreen instance;
    private Object lastScreen;

    static Setting[][] settings;
    int section;
    
    private Command textBoxOkCommand;
    private Command textBoxCancelCommand;

    SettingsSectionScreen(int section) {
        super(SettingsScreen.sectionNames[section], true, false, true);
        setCommandListener(this);
        this.section = section;
        lastScreen = App.disp.getCurrent();

        for (int i = 0; i < settings[section].length; i++) {
            Setting set = settings[section][i];
            if (set == null || set.label == null) continue;  // this is a "dummy" setting which is hidden
            append(set.label, getValueLabel(set), getIcon(set), ListScreen.INDICATOR_NONE);
        }
    }

    private String getValueLabel(Setting set) {
        if (set.valueLabels == null) {
            return null;
        } else {
            return (set.valueLabels.length > set.value) ?
                set.valueLabels[set.value] :
                Integer.toString(set.value);
        }
    }

    private Image getIcon(Setting set) {
        return (set.icons.length > set.value) ? set.icons[set.value] : set.icons[0];
    }

    private void cycleValue(int direction) {
        int selectedIndex = getSelectedIndex();
        int itemIndex = getItemIndex(selectedIndex);
        Setting set = settings[section][itemIndex];

        set.value += direction;
        
        if (set.value < 0) {
            set.value = set.maxValue;
        }
        else if (set.value > set.maxValue) {
            set.value = 0;
        }
        updateMenuItem(selectedIndex);
    }

    public void customKeyEvent(int keycode) {
        switch (getGameAction(keycode)) {
            case LEFT: {
                cycleValue(-1);
                break;
            }
            case RIGHT: {
                cycleValue(1);
                break;
            }
        }
    }

    public void updateMenuItem(int selectedIndex) {
        int itemIndex = getItemIndex(selectedIndex);
        Setting set = settings[section][itemIndex];
        set(selectedIndex, set.label, getValueLabel(set), getIcon(set), ListScreen.INDICATOR_NONE);
    }

    // Gets settings[] index that should be changed based on selected menu item index
    private int getItemIndex(int selectedIndex) {
        int counter = 0;  // count how many non-null setting items we've looked through
        Setting[] sectSettings = settings[section];

        for (int i = 0; i < sectSettings.length; i++) {
            if (sectSettings[i] == null || sectSettings[i].label == null) continue;
            if (counter == selectedIndex) return i;
            counter++;
        }
        return -1;
    }

    private void showTextBox(int selectedIndex) {
        instance = this;
        int itemIndex = getItemIndex(selectedIndex);
        Setting set = settings[section][itemIndex];

        int maxLength = (set.maxValue == 0) ? 10 : Integer.toString(set.maxValue).length();

        TextBox tb = new TextBox(
            set.label,
            Integer.toString(set.value),
            maxLength,
            TextField.NUMERIC
        );

        textBoxOkCommand = Locale.createCommand(OK, Command.OK, 0);
        textBoxCancelCommand = Locale.createCommand(CANCEL, Command.BACK, 0);
        tb.addCommand(textBoxOkCommand);
        tb.addCommand(textBoxCancelCommand);
        tb.setCommandListener(this);
        App.disp.setCurrent(tb);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            int selectedIndex = getSelectedIndex();
            int itemIndex = getItemIndex(selectedIndex);

            // SPECIAL CASE for Behavior -> Remap hotkeys: open separate menu
            if (section == 2 && itemIndex == 5) {
                App.disp.setCurrent(new KeyMapper());
            }
            // SPECIAL CASE for Appearance -> Time format: open separate menu
            else if (section == 0 && itemIndex == 4) {
                App.disp.setCurrent(new TimeFormatForm());
            }
            else {
                int max = settings[section][itemIndex].maxValue;
                if (max == 0 || max >= 5) {
                    // Max value is 0 (any value allowed) or >= 5, show text entry
                    showTextBox(selectedIndex);
                } else {
                    // Max value is a small number: cycle between values
                    cycleValue(1);
                }
            }
        }
        else if (c == BACK_COMMAND) {
            App.disp.setCurrent(lastScreen);
        }
        else {
            // textbox command
            // OK or cancel command in textbox screen: (if OK, write entered value into values array), then return to where we left off in the settings screen
            if (c == textBoxOkCommand) {
                int selectedIndex = getSelectedIndex();
                int itemIndex = getItemIndex(selectedIndex);
                int max = settings[section][itemIndex].maxValue;

                try {
                    int value = Integer.parseInt(((TextBox) d).getString());
                    // SPECIAL CASE for Images -> Menu icon size
                    boolean isMenuIconSizeSetting = (section == 1 && itemIndex == 4);
                    // it has a minimum value of 0 (off), while other options have a min of 1
                    // and 1 and 2 are reserved values that older versions of the app used for 16 and 32 px, so don't allow inputting them

                    int min = isMenuIconSizeSetting ? 0 : 1;
                    if (value < min || value > max) throw new Exception();

                    if (isMenuIconSizeSetting && (value == 1 || value == 2)) {
                        value = 3;
                    }

                    settings[section][itemIndex].value = value;
                    updateMenuItem(selectedIndex);
                }
                catch (Exception e) {
                    App.error(Locale.get(SETTINGS_ERROR_INVALID_NUMBER_PREFIX) + max + Locale.get(SETTINGS_ERROR_INVALID_NUMBER_SUFFIX));
                    return;
                }
            }
            App.disp.setCurrent(instance);
            instance = null;
            textBoxOkCommand = null;
            textBoxCancelCommand = null;
        }
    }
}