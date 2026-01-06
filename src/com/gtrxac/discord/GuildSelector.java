package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends List implements CommandListener {
    private Vector guilds;
    public boolean isFavGuilds;

    public GuildSelector(Vector guilds, boolean isFavGuilds) {
        super(isFavGuilds ? Settings.favLabel2 : "Servers", List.IMPLICIT);

        setCommandListener(this);
        this.guilds = guilds;
        this.isFavGuilds = isFavGuilds;

        for (int i = 0; i < guilds.size(); i++) {
            DiscordObject g = (DiscordObject) guilds.elementAt(i);
            append(App.trimItem(g.name), null);
        }

        addCommand(new Command("Back", Command.BACK, 1));

        if (isFavGuilds) {
            addCommand(new Command("Remove", Command.ITEM, 2));
        } else {
            addCommand(new Command(Settings.favLabel, Command.ITEM, 3));
            addCommand(new Command("Refresh", Command.SCREEN, 4));
        }
    }

    public void commandAction(Command c, Displayable d) {
        DiscordObject g = (DiscordObject) guilds.elementAt(getSelectedIndex());

        switch (c.getPriority()) {
            case 0: {  // list select command
                App.isDM = false;
                if (g == App.selectedGuild) {
                    App.openChannelSelector(false);
                } else {
                    App.selectedGuild = g;
                    App.openChannelSelector(true);
                }
                break;
            }

            case 1: {  // back
                App.disp.setCurrent(new MainMenu());
                break;
            }

            case 2: {  // remove favorite
                Settings.favRemove(getSelectedIndex());
                Settings.favOpenSelector();
                break;
            }

            case 3: {  // add favorite
                Settings.favAdd(g);
                break;
            }

            case 4: {  // refresh
                App.openGuildSelector(true);
                break;
            }
        }
    }
}
