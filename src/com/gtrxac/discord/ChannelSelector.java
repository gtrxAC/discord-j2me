package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends List implements CommandListener {
    private Command backCommand;
    private Command refreshCommand;

    public ChannelSelector() {
        super(App.isDM ? "Direct messages" : App.selectedGuild.name, List.IMPLICIT);
        setCommandListener(this);

        for (int i = 0; i < App.channels.size(); i++) {
            DiscordObject ch = (DiscordObject) App.channels.elementAt(i);
            append(App.isDM ? ch.name : ("#" + ch.name), null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            if (App.isDM) App.disp.setCurrent(new MainMenu());
            else App.openGuildSelector(false);
        }
        else if (c == refreshCommand) {
            App.openChannelSelector(true);
        }
        else if (c == List.SELECT_COMMAND) {
            App.selectedChannel = (DiscordObject) App.channels.elementAt(getSelectedIndex());
            App.openChannelView(true);
        }
    }
}