package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoadingScreen extends Form {
    public LoadingScreen() {
        super(null);
        StringItem i = new StringItem(null, "Loading");
        append(i);
    }
}
