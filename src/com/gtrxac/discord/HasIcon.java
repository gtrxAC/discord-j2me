package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public interface HasIcon {
    public String getIconID();
    public String getIconHash();
    public String getIconType();
    public void iconLoaded(State s);
}
