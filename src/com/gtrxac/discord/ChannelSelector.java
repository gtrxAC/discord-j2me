package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends List implements CommandListener {
    State s;
    private Command backCommand;
    private Command refreshCommand;

    public ChannelSelector(State s) throws Exception {
        super(s.selectedGuild.name, List.IMPLICIT);
        setCommandListener(this);
        this.s = s;

        Channel.fetchChannels(s);
        for (int i = 0; i < s.channels.size(); i++) {
            append("#" + ((Channel) s.channels.elementAt(i)).name, null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(s.guildSelector);
        }
        if (c == refreshCommand) {
            s.openChannelSelector(true);
        }
        if (c == List.SELECT_COMMAND) {
            s.isDM = false;
            s.selectedChannel = (Channel) s.channels.elementAt(getSelectedIndex());
            s.openChannelView(true);
        }
    }
}