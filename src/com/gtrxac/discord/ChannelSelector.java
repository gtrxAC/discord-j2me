package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends List implements CommandListener {
    public ChannelSelector() {
        super("", List.IMPLICIT);
        setCommandListener(this);

        if (App.isDM) {
            setTitle(App.screenWidth <= 128 ? "Direct msgs." : "Direct messages");
        } else {
            setTitle(App.selectedGuild.name);
        }

        for (int i = 0; i < App.channels.size(); i++) {
            DiscordObject ch = (DiscordObject) App.channels.elementAt(i);
            String label = (App.isDM || App.listTimestamps) ? ch.name : ("#" + ch.name);
            append(App.trimItem(label), null);
        }

        addCommand(new Command("Back", Command.BACK, 1));
        addCommand(new Command("Refresh", Command.SCREEN, 2));
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case 0: {  // list select command
                App.selectedChannel = (DiscordObject) App.channels.elementAt(getSelectedIndex());
                App.openChannelView(true);
                break;
            }

            case 1: {  // back
                if (App.isDM) App.disp.setCurrent(new MainMenu());
                else App.disp.setCurrent(App.guildSelector);
                break;
            }

            case 2: {  // refresh
                App.openChannelSelector(true);
                break;
            }
        }
    }
}