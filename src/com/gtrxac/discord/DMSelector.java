package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class DMSelector extends ListScreen implements CommandListener, Strings {
    State s;
    Vector lastDMs;

    private Command searchCommand;
    private Command refreshCommand;
    private Command markReadCommand;
    private Command markAllReadCommand;
    // ifdef OVER_100KB
    private Command muteCommand;
    // endif

    public DMSelector(State s) throws Exception {
        super(Locale.get(DM_SELECTOR_TITLE), true, true, false);
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

        UnreadManager.autoSave = false;
        for (int i = 0; i < lastDMs.size(); i++) {
            DMChannel ch = (DMChannel) lastDMs.elementAt(i);
            append(ch.name, null, IconCache.getResized(ch, s.menuIconSize), ch.getMenuIndicator());
        }
        UnreadManager.manualSave();

        searchCommand = Locale.createCommand(SEARCH, Command.SCREEN, 2);
        refreshCommand = Locale.createCommand(REFRESH, Command.SCREEN, 3);
        markReadCommand = Locale.createCommand(MARK_READ, Command.SCREEN, 4);
        markAllReadCommand = Locale.createCommand(MARK_ALL_READ, Command.SCREEN, 5);
        // ifdef OVER_100KB
        muteCommand = Locale.createCommand(MUTE, Command.ITEM, 6);
        // endif

        addCommand(searchCommand);
        addCommand(refreshCommand);

        if (s.dmChannels.size() > 0) {
            addCommand(markReadCommand);
            addCommand(markAllReadCommand);
            // ifdef OVER_100KB
            addCommand(muteCommand);
            // endif
        }
    }

    /**
     * Updates the icon and unread indicator for one DM channel in this selector.
     */
    public void update(String chId) {
        for (int i = 0; i < lastDMs.size(); i++) {
            DMChannel ch = (DMChannel) lastDMs.elementAt(i);
            if (chId != null && !ch.id.equals(chId)) continue;

            set(i, ch.name, null, IconCache.getResized(ch, s.menuIconSize), ch.getMenuIndicator());
        }
    }

    /**
     * Updates the icons and unread indicators for all DM channels in this selector.
     */
    public void update() { update(null); }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            // Unload DM list if needed, and go back to main menu
            if (!s.highRamMode) s.dmChannels = null;
            s.disp.setCurrent(MainMenu.get(null));
        }
        if (c == searchCommand) {
            s.disp.setCurrent(new DMSearchForm(s));
        }
        if (c == refreshCommand) {
            s.openDMSelector(true, true);
        }
        if (c == markAllReadCommand) {
            UnreadManager.markDMsRead();
            update();
        }
        else {
            DMChannel dmCh = (DMChannel) lastDMs.elementAt(getSelectedIndex());

            if (c == SELECT_COMMAND) {
                s.isDM = true;
                s.selectedDmChannel = dmCh;
                s.openChannelView(true);
            }
            // ifdef OVER_100KB
            else if (c == muteCommand) {
                FavoriteGuilds.toggleMute(s, dmCh.id);
                update(dmCh.id);
            }
            // endif
            else {
                // 'mark as read' command
                dmCh.markRead();
                update(dmCh.id);
            }
        }
    }
}