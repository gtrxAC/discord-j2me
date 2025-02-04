package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends ListScreen implements CommandListener, Strings {
    State s;
    private Command viewThreadsCommand;
    private Command refreshCommand;
    private Command markChannelReadCommand;
    private Command markGuildReadCommand;
    // ifdef OVER_100KB
    private Command muteCommand;
    // endif

    public ChannelSelector(State s) throws Exception {
        super(s.selectedGuild.name, true, true, false);
        setCommandListener(this);
        this.s = s;

        UnreadManager.autoSave = false;
        for (int i = 0; i < s.channels.size(); i++) {
            Channel ch = (Channel) s.channels.elementAt(i);
            append(ch.toString(), null, null, ch.getMenuIndicator());
        }
        UnreadManager.manualSave();

        viewThreadsCommand = Locale.createCommand(VIEW_THREADS, Command.ITEM, 1);
        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 2);
        markChannelReadCommand = Locale.createCommand(MARK_READ, Command.ITEM, 3);
        markGuildReadCommand = Locale.createCommand(MARK_ALL_READ, Command.ITEM, 4);
        // ifdef OVER_100KB
        muteCommand = Locale.createCommand(MUTE, Command.ITEM, 5);
        // endif
        addCommand(refreshCommand);

        if (s.channels.size() > 0) {
            addCommand(viewThreadsCommand);
            addCommand(markChannelReadCommand);
            addCommand(markGuildReadCommand);
            // ifdef OVER_100KB
            addCommand(muteCommand);
            // endif
        }
    }

    /**
     * Updates the unread indicator for a channel shown in this selector.
     */
    public void update(String id) {
        for (int i = 0; i < s.channels.size(); i++) {
            Channel ch = (Channel) s.channels.elementAt(i);
            if (id != null && !ch.id.equals(id)) continue;

            set(i, ch.toString(), null, null, ch.getMenuIndicator());
        }
    }

    /**
     * Updates the unread indicators for all channels shown in this selector.
     */
    public void update() { update(null); }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            // Unload this server's channel list if needed, and go back to server list
            if (!s.highRamMode && !s.gatewayActive()) {
                s.channels = null;
                s.selectedGuild.channels = null;
            }
            s.guildSelector.update(s.selectedGuild.id);
            // s.openGuildSelector(false, false);
            s.disp.setCurrent(s.guildSelector);
        }
        else if (c == refreshCommand) {
            s.openChannelSelector(true, true);
        }
        else if (c == markGuildReadCommand) {
            s.selectedGuild.markRead();
            update();
            s.guildSelector.update(s.selectedGuild.id);
        }
        else {
            Channel ch = (Channel) s.channels.elementAt(getSelectedIndex());

            if (c == markChannelReadCommand) {
                ch.markRead();
                update(ch.id);
                s.guildSelector.update(s.selectedGuild.id);
            }
            // ifdef OVER_100KB
            if (c == muteCommand) {
                FavoriteGuilds.toggleMute(s, ch.id);
                update(ch.id);
            }
            // endif
            else if (c == SELECT_COMMAND && !ch.isForum) {
                s.isDM = false;
                s.selectedChannel = ch;
                s.openChannelView(true);
            }
            // "View threads" command was used, or a channel was selected and it's a forum channel
            else {
                s.selectedChannelForThreads = ch;
                s.openThreadSelector(true, false);
            }
        }
    }
}