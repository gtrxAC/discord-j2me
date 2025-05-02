package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends List implements CommandListener {
    private Command backCommand;
    private Command refreshCommand;

    public ChannelSelector() {
        super("", List.IMPLICIT);
        setCommandListener(this);

        if (App.isDM) {
            setTitle(Util.screenWidth <= 128 ? "Direct msgs." : "Direct messages");
        } else {
            setTitle(App.selectedGuild.name);
        }

        for (int i = 0; i < App.channels.size(); i++) {
            DiscordObject ch = (DiscordObject) App.channels.elementAt(i);
            String label = (App.isDM || App.listTimestamps) ? ch.name : ("#" + ch.name);
            append(Util.trimItem(label), null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.SCREEN, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            if (App.isDM) App.disp.setCurrent(new MainMenu());
            else App.disp.setCurrent(App.guildSelector);
        }
        else if (c == refreshCommand) {
            App.openChannelSelector(true);
        }
        else {
            // list select command
            App.selectedChannel = (DiscordObject) App.channels.elementAt(getSelectedIndex());
            App.openChannelView(true);
        }
    }
}