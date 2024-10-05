package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Represents an entity that has a loadable icon/avatar image on Discord.
 */
public interface HasIcon {
    /**
     * Returns the Discord "snowflake" ID of this object.
     * Like the hash, this is required as part of the URL for fetching the icon.
     */
    public String getIconID();

    /**
     * Returns the icon/avatar hash of this object.
     */
    public String getIconHash();

    /**
     * Returns the icon type (the first part of the icon URL after the hostname).
     * For example: "/avatars/"
     * 
     * The icon type is used to form the icon URL, you can think of it as filling in the blank here:
     * https://cdn.discordapp.com_____123456789012345678/1234567890abcdef.png
     */
    public String getIconType();

    /**
     * Callback function that is called when the icon for this object has been loaded and cached.
     */
    public void iconLoaded(State s);
}
