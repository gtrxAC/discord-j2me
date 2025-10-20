package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class TimeFormatForm extends Form implements Strings, CommandListener, ItemStateListener {
    private StringItem previewItem;
    private Gauge hourOffsetGauge;
    private Gauge minOffsetGauge;
    private ChoiceGroup optionsGroup;
    private Command saveCommand;
    private Command cancelCommand;
    private Displayable lastScreen;
    private int oldOffset;
    private boolean oldUse12hTime;

    public TimeFormatForm() {
        super("Time format");
        setCommandListener(this);
        setItemStateListener(this);
        lastScreen = App.disp.getCurrent();

        previewItem = new StringItem("Preview", "");
        previewItem.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE));
        previewItem.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_CENTER);

        hourOffsetGauge = new Gauge("Offset (hours)", true, 14, 0);
        minOffsetGauge = new Gauge("Offset (minutes)", true, 60, 0);

        int absOffsetMins = Math.abs(Settings.timeOffset)/(60*1000);
        hourOffsetGauge.setValue(absOffsetMins/60);
        minOffsetGauge.setValue(absOffsetMins%60);
        
        String[] choices = {
            "Negative offset",
            Locale.get(USE_12H_TIME)
        };
        optionsGroup = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, choices, null);
        optionsGroup.setSelectedIndex(0, Settings.timeOffset < 0);
        optionsGroup.setSelectedIndex(1, Settings.use12hTime);
        
        append(new Spacer(getWidth(), getHeight()/20));
        append(previewItem);
        append(new Spacer(getWidth(), getHeight()/20));
        append(hourOffsetGauge);
        append(minOffsetGauge);
        append(new Spacer(getWidth(), getHeight()/20));
        append(optionsGroup);

        saveCommand = Locale.createCommand(SAVE, Command.BACK, 0);
        cancelCommand = Locale.createCommand(CANCEL, Command.BACK, 1);
        addCommand(saveCommand);
        addCommand(cancelCommand);

        oldOffset = Settings.timeOffset;
        oldUse12hTime = Settings.use12hTime;
        itemStateChanged(null);  // refresh preview to show its initial content
    }

    public void itemStateChanged(Item i) {
        int offsetSign = optionsGroup.isSelected(0) ? -1 : 1;
        int offsetMinutes = hourOffsetGauge.getValue()*60 + minOffsetGauge.getValue();

        Settings.timeOffset = offsetSign*offsetMinutes*60*1000;
        Settings.use12hTime = optionsGroup.isSelected(1);

        previewItem.setText(Message.formatTimestamp(System.currentTimeMillis()));
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cancelCommand) {
            Settings.timeOffset = oldOffset;
            Settings.use12hTime = oldUse12hTime;
        }
        App.disp.setCurrent(lastScreen);
    }
}