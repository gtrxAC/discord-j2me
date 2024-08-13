package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class DMSelector extends List implements CommandListener {
    State s;
    Vector lastDMs;
    private Timer refreshTimer;
    private boolean isAutoRefreshEnabled = false;

    private Command backCommand;
    private Command searchCommand;
    private Command refreshCommand;
    private Command markReadCommand;
    private Command markAllReadCommand;
    private Command toggleAutoRefreshCommand;

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
            append(ch.toString(s), s.iconCache.get(ch));
        }

        backCommand = new Command("Back", Command.BACK, 0);
        searchCommand = new Command("Search", Command.ITEM, 1);
        refreshCommand = new Command("Refresh", Command.ITEM, 2);
        markReadCommand = new Command("Mark as read", Command.ITEM, 3);
        markAllReadCommand = new Command("Mark all as read", Command.ITEM, 4);
        toggleAutoRefreshCommand = new Command("Toggle Auto-Refresh", Command.ITEM, 5);

        addCommand(backCommand);
        addCommand(searchCommand);
        addCommand(refreshCommand);
        addCommand(toggleAutoRefreshCommand);

        if (s.dmChannels.size() > 0) {
            addCommand(markReadCommand);
            addCommand(markAllReadCommand);
        }
    }

    /**
     * Updates the icon and unread indicator for one DM channel in this selector.
     */
    public void update(String chId) {
        for (int i = 0; i < lastDMs.size(); i++) {
            DMChannel ch = (DMChannel) lastDMs.elementAt(i);
            if (chId != null && !ch.id.equals(chId)) continue;

            set(i, ch.toString(s), s.iconCache.get(ch));
        }
    }

    /**
     * Updates the icons and unread indicators for all DM channels in this selector.
     */
    public void update() { update(null); }

    /**
     * Starts the auto-refresh timer.
     */
    private void startAutoRefresh() {
        if (refreshTimer == null) {
            refreshTimer = new Timer();
            refreshTimer.schedule(new TimerTask() {
                public void run() {
                    s.openDMSelector(true);  // Refresh the selector
                }
            }, 0, 15000);  // 15 seconds
        }
    }

    /**
     * Stops the auto-refresh timer.
     */
    private void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            stopAutoRefresh();  // Stop auto-refresh when going back
            s.disp.setCurrent(new MainMenu(s));
        }
        if (c == searchCommand) {
            s.disp.setCurrent(new DMSearchForm(s));
        }
        if (c == refreshCommand) {
            s.openDMSelector(true);
        }
        if (c == markReadCommand) {
            DMChannel dmCh = (DMChannel) lastDMs.elementAt(getSelectedIndex());
            s.unreads.markRead(dmCh);
            update(dmCh.id);
        }
        if (c == markAllReadCommand) {
            s.unreads.markDMsRead();
            update();
        }
        if (c == toggleAutoRefreshCommand) {
            isAutoRefreshEnabled = !isAutoRefreshEnabled;
            if (isAutoRefreshEnabled) {
                startAutoRefresh();
            } else {
                stopAutoRefresh();
            }
        }
        if (c == List.SELECT_COMMAND) {
            s.isDM = true;
            s.selectedDmChannel = (DMChannel) lastDMs.elementAt(getSelectedIndex());
            s.openChannelView(true);
        }
    }
}
