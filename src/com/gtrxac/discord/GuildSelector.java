package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends List implements CommandListener {
    private State s;
    private Vector guilds;
    private Command backCommand;
    private Command refreshCommand;

    public GuildSelector(State s, Vector guilds) {
        super("Servers", List.IMPLICIT);

        setCommandListener(this);
        this.s = s;
        this.guilds = guilds;

        for (int i = 0; i < guilds.size(); i++) {
            DiscordObject g = (DiscordObject) guilds.elementAt(i);
            append(g.name, null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 3);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(new MainMenu(s));
        }
        else if (c == refreshCommand) {
            s.openGuildSelector(true);
        }
        else if (c == List.SELECT_COMMAND) {
            DiscordObject g = (DiscordObject) guilds.elementAt(getSelectedIndex());

            s.isDM = false;
            if (g == s.selectedGuild) {
                s.openChannelSelector(false);
            } else {
                s.selectedGuild = g;
                s.openChannelSelector(true);
            }
        }
    }
}
