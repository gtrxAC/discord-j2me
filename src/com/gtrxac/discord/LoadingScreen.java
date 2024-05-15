package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoadingScreen extends Form {
    public LoadingScreen() {
        super(null);
        StringItem i = new StringItem(null, "Loading");
        i.setLayout(Item.LAYOUT_VEXPAND | Item.LAYOUT_VCENTER | Item.LAYOUT_CENTER);
        append(i);
    }
}
