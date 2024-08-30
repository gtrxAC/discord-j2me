package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends List implements CommandListener {
    State s;
    private Command backCommand;
    private Command refreshCommand;

    public ChannelSelector(State s) {
        super(s.isDM ? "Direct messages" : s.selectedGuild.name, List.IMPLICIT);
        setCommandListener(this);
        this.s = s;

        for (int i = 0; i < s.channels.size(); i++) {
            DiscordObject ch = (DiscordObject) s.channels.elementAt(i);
            append(s.isDM ? ch.name : ("#" + ch.name), null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            if (s.isDM) s.disp.setCurrent(new MainMenu(s));
            else s.openGuildSelector(false);
        }
        else if (c == refreshCommand) {
            s.openChannelSelector(true);
        }
        else if (c == List.SELECT_COMMAND) {
            s.selectedChannel = (DiscordObject) s.channels.elementAt(getSelectedIndex());
            s.openChannelView(true);
        }
    }
}