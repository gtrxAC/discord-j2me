package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class LoadingScreen extends Form {
    public LoadingScreen() {
        super("Please wait");
        append("Downloading data");
    }
}
