package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends List implements CommandListener {
    State s;
    private Command backCommand;
    private Command refreshCommand;

    public ChannelSelector(State s) {
        super(s.selectedGuild.name, List.IMPLICIT);
        setCommandListener(this);
        this.s = s;

        try {
            Channel.fetchChannels(s);
            for (int i = 0; i < s.channels.size(); i++) {
                append("#" + ((Channel) s.channels.elementAt(i)).name, null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            append("Failed to get server list", null);
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
            s.channelSelector = new ChannelSelector(s);
            s.disp.setCurrent(new LoadingScreen());
            s.disp.setCurrent(s.channelSelector);
        }
        if (c == List.SELECT_COMMAND) {
            s.selectedChannel = (Channel) s.channels.elementAt(getSelectedIndex());
            s.disp.setCurrent(new LoadingScreen());
            s.disp.setCurrent(new ChannelView(s));
        }
    }
}