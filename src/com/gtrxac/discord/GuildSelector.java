package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends List implements CommandListener {
    private Vector guilds;
    private Command backCommand;
    private Command refreshCommand;
    private Command addFavCommand;
    private Command removeFavCommand;
    public boolean isFavGuilds;

    public GuildSelector(Vector guilds, boolean isFavGuilds) {
        super(isFavGuilds ? FavoriteGuilds.label2 : "Servers", List.IMPLICIT);

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
            refreshCommand = new Command("Refresh", Command.ITEM, 3);
            addFavCommand = new Command(FavoriteGuilds.label, Command.ITEM, 3);
            addCommand(refreshCommand);
            addCommand(addFavCommand);
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
            FavoriteGuilds.remove(getSelectedIndex());
            FavoriteGuilds.openSelector();
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
                FavoriteGuilds.add(g);
            }
        }
    }
}
