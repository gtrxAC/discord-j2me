package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ThreadSelector extends ListScreen implements CommandListener, Strings {
    State s;
    private Command refreshCommand;
    private Command markThreadReadCommand;
    private Command markAllReadCommand;
    // ifdef OVER_100KB
    private Command muteCommand;
    // endif

    public ThreadSelector(State s) throws Exception {
        super("#" + s.selectedChannelForThreads.name, true, true, false);
        setCommandListener(this);
        this.s = s;

        for (int i = 0; i < s.threads.size(); i++) {
            Channel ch = (Channel) s.threads.elementAt(i);
            append(ch.toString(), null, null, ch.getMenuIndicator());
        }

        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 2);
        markThreadReadCommand = Locale.createCommand(MARK_READ, Command.ITEM, 3);
        markAllReadCommand = Locale.createCommand(MARK_ALL_READ, Command.ITEM, 4);
        // ifdef OVER_100KB
        muteCommand = Locale.createCommand(MUTE, Command.ITEM, 5);
        // endif
        addCommand(refreshCommand);

        if (s.threads.size() > 0) {
            addCommand(markThreadReadCommand);
            addCommand(markAllReadCommand);
            // ifdef OVER_100KB
            addCommand(muteCommand);
            // endif
        }
    }

    /**
     * Updates the unread indicator for a thread shown in this selector.
     */
    public void update(String id) {
        for (int i = 0; i < s.threads.size(); i++) {
            Channel ch = (Channel) s.threads.elementAt(i);
            if (id != null && !ch.id.equals(id)) continue;

            set(i, ch.toString(), null, null, ch.getMenuIndicator());
        }
    }

    /**
     * Updates the unread indicators for all threads shown in this selector.
     */
    public void update() { update(null); }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            // Unload this channel's thread list if needed, and go back to channel list
            if (!s.highRamMode && !s.gatewayActive()) {
                s.threads = null;
                s.selectedChannelForThreads.threads = null;
            }
            s.disp.setCurrent(s.channelSelector);
        }
        else if (c == refreshCommand) {
            new HTTPThread(s, HTTPThread.FETCH_THREADS).start();
        }
        else if (c == markAllReadCommand) {
            UnreadManager.markRead(s.threads);
            update();
        }
        else {
            Channel ch = (Channel) s.threads.elementAt(getSelectedIndex());

            if (c == SELECT_COMMAND) {
                s.isDM = false;
                s.selectedChannel = ch;
                s.openChannelView(true);
            }
            // ifdef OVER_100KB
            else if (c == muteCommand) {
                FavoriteGuilds.toggleMute(s, ch.id);
                update(ch.id);
            }
            // endif
            else {
                // 'mark as read' command
                ch.markRead();
                update(ch.id);
            }
        }
    }
}