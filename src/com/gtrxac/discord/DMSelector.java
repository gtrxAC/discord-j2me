package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class DMSelector extends List implements CommandListener {
    State s;
    Vector lastDMs;

    private Command backCommand;
    private Command searchCommand;
    private Command refreshCommand;

    public DMSelector(State s) throws Exception {
        super("Direct Message", List.IMPLICIT);
        setCommandListener(this);
        this.s = s;

        // Get the 20 latest DMs (add into another vector sorted by highest to lowest last message ID)
        int count = 20;
        if (s.dmChannels.size() < 20) count = s.dmChannels.size();
        lastDMs = new Vector(count);
        
        for (int i = 0; i < count; i++) {
            long highestID = 0;
            int highestIndex = 0;

            for (int u = 0; u < s.dmChannels.size(); u++) {
                DMChannel ch = (DMChannel) s.dmChannels.elementAt(u);

                if (ch.lastMessageID <= highestID) continue;

                // Don't repeat ones that we've already added
                boolean alreadyHave = false;
                for (int l = 0; l < lastDMs.size(); l++) {
                    if (((DMChannel) lastDMs.elementAt(l)).lastMessageID == ch.lastMessageID) {
                        alreadyHave = true;
                        break;
                    }
                }
                if (alreadyHave) continue;

                highestID = ch.lastMessageID;
                highestIndex = u;
            }
            lastDMs.addElement(s.dmChannels.elementAt(highestIndex));
        }

        for (int i = 0; i < lastDMs.size(); i++) {
            DMChannel ch = (DMChannel) lastDMs.elementAt(i);
            append(ch.name, s.iconCache.get(ch));
        }

        backCommand = new Command("Back", Command.BACK, 0);
        searchCommand = new Command("Search", Command.ITEM, 1);
        refreshCommand = new Command("Refresh", Command.ITEM, 2);
        addCommand(backCommand);
        addCommand(searchCommand);
        addCommand(refreshCommand);
    }

    /**
     * Updates the icons and unread/ping indicators for DM channel names shown in this selector.
     */
    public void update() {
        for (int i = 0; i < lastDMs.size(); i++) {
            DMChannel ch = (DMChannel) lastDMs.elementAt(i);
            set(i, ch.name, s.iconCache.get(ch));
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(new MainMenu(s));
        }
        if (c == searchCommand) {
            s.disp.setCurrent(new DMSearchForm(s));
        }
        if (c == refreshCommand) {
            s.openDMSelector(true);
        }
        if (c == List.SELECT_COMMAND) {
            s.isDM = true;
            s.selectedDmChannel = (DMChannel) lastDMs.elementAt(getSelectedIndex());
            s.openChannelView(true);
        }
    }
}