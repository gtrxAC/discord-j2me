package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoadingScreen extends Form implements CommandListener {
    private State s;
    public StringItem text;
    public Command backCommand;
    public Displayable prevScreen;

    public LoadingScreen(State s) {
        super(null);
        this.s = s;
        prevScreen = s.disp.getCurrent();

        text = new StringItem(null, "Loading");
        text.setLayout(Item.LAYOUT_VEXPAND | Item.LAYOUT_VCENTER | Item.LAYOUT_CENTER);
        append(text);

        backCommand = new Command("Cancel", Command.BACK, 1);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        s.disp.setCurrent(prevScreen);
    }
}
