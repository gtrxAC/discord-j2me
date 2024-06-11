package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoadingScreen extends Form {
    public StringItem text;

    public LoadingScreen() {
        super(null);
        text = new StringItem(null, "Loading");
        text.setLayout(Item.LAYOUT_VEXPAND | Item.LAYOUT_VCENTER | Item.LAYOUT_CENTER);
        append(text);
    }
}
