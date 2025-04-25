package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends List implements CommandListener {
    private Vector guilds;
    private Command backCommand;
    private Command refreshCommand;

    public GuildSelector(Vector guilds) {
        super("Servers", List.IMPLICIT);

        setCommandListener(this);
        this.guilds = guilds;

        for (int i = 0; i < guilds.size(); i++) {
            DiscordObject g = (DiscordObject) guilds.elementAt(i);
            append(Util.trimItem(g.name), null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 3);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            App.disp.setCurrent(new MainMenu());
        }
        else if (c == refreshCommand) {
            App.openGuildSelector(true);
        }
        else if (c == List.SELECT_COMMAND) {
            DiscordObject g = (DiscordObject) guilds.elementAt(getSelectedIndex());

            App.isDM = false;
            if (g == App.selectedGuild) {
                App.openChannelSelector(false);
            } else {
                App.selectedGuild = g;
                App.openChannelSelector(true);
            }
        }
    }
}
