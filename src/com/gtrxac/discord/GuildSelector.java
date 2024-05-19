package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class GuildSelector extends List implements CommandListener {
    State s;

    private Command backCommand;
    private Command refreshCommand;

    public GuildSelector(State s) throws Exception {
        super("Servers", List.IMPLICIT);
        setCommandListener(this);
        this.s = s;

        for (int i = 0; i < s.guilds.size(); i++) {
            append(((Guild) s.guilds.elementAt(i)).name, null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(new MainMenu(s));
        }
        if (c == refreshCommand) {
            s.openGuildSelector(true);
        }
        if (c == List.SELECT_COMMAND) {
            Guild newGuild = (Guild) s.guilds.elementAt(getSelectedIndex());

            if (s.selectedGuild == null || newGuild.id != s.selectedGuild.id) {
                s.selectedGuild = newGuild;
                s.openChannelSelector(true);
            } else {
                s.openChannelSelector(false);
            }
        }
    }
}
