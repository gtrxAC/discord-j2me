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
            Guild g = (Guild) s.guilds.elementAt(i);
            append(g.toString(s), s.iconCache.get(g));
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    /**
     * Updates the icon and unread indicator for a server shown in this selector.
     */
    public void update(String id) {
        for (int i = 0; i < s.guilds.size(); i++) {
            Guild g = (Guild) s.guilds.elementAt(i);
            if (id != null && !g.id.equals(id)) continue;

            set(i, g.toString(s), s.iconCache.get(g));
        }
    }

    /**
     * Updates the icons and unread indicators for all servers shown in this selector.
     */
    public void update() { update(null); }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(new MainMenu(s));
        }
        if (c == refreshCommand) {
            s.openGuildSelector(true);
        }
        if (c == List.SELECT_COMMAND) {
            Guild newGuild = (Guild) s.guilds.elementAt(getSelectedIndex());
            s.selectedGuild = newGuild;
            s.openChannelSelector(false);
        }
    }
}
