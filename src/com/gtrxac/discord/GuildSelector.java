package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends List implements CommandListener {
    private Vector guilds;
    private Command backCommand;
    private Command addFavCommand;
    private Command removeFavCommand;
    private Command refreshCommand;
    public boolean isFavGuilds;

    public GuildSelector(Vector guilds, boolean isFavGuilds) {
        super(isFavGuilds ? Settings.favLabel2 : "Servers", List.IMPLICIT);

        setCommandListener(this);
        this.guilds = guilds;
        this.isFavGuilds = isFavGuilds;

        for (int i = 0; i < guilds.size(); i++) {
            DiscordObject g = (DiscordObject) guilds.elementAt(i);
            append(Util.trimItem(g.name), null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        addCommand(backCommand);

        if (isFavGuilds) {
            removeFavCommand = new Command("Remove", Command.ITEM, 2);
            addCommand(removeFavCommand);
        } else {
            addFavCommand = new Command(Settings.favLabel, Command.ITEM, 2);
            refreshCommand = new Command("Refresh", Command.SCREEN, 3);
            addCommand(addFavCommand);
            addCommand(refreshCommand);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            App.disp.setCurrent(new MainMenu());
        }
        else if (c == refreshCommand) {
            App.openGuildSelector(true);
        }
        else if (c == removeFavCommand) {
            Settings.favRemove(getSelectedIndex());
            Settings.favOpenSelector();
        }
        else {
            DiscordObject g = (DiscordObject) guilds.elementAt(getSelectedIndex());

            if (c == List.SELECT_COMMAND) {
                App.isDM = false;
                if (g == App.selectedGuild) {
                    App.openChannelSelector(false);
                } else {
                    App.selectedGuild = g;
                    App.openChannelSelector(true);
                }
            } else {
                // add to favorites command
                Settings.favAdd(g);
            }
        }
    }
}
