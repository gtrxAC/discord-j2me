package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends ListScreen implements CommandListener, Strings {
    State s;
    private Command refreshCommand;
    private Command markChannelReadCommand;
    private Command markGuildReadCommand;

    public ChannelSelector(State s) throws Exception {
        super(s.selectedGuild.name, true, true, false);
        setCommandListener(this);
        this.s = s;

        for (int i = 0; i < s.channels.size(); i++) {
            Channel ch = (Channel) s.channels.elementAt(i);
            append(ch.toString(s), null, null, s.unreads.hasUnreads(ch));
        }

        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 2);
        markChannelReadCommand = Locale.createCommand(MARK_READ, Command.ITEM, 3);
        markGuildReadCommand = Locale.createCommand(MARK_ALL_READ, Command.ITEM, 4);
        addCommand(refreshCommand);

        if (s.channels.size() > 0) {
            addCommand(markChannelReadCommand);
            addCommand(markGuildReadCommand);
        }
    }

    /**
     * Updates the unread indicator for a channel shown in this selector.
     */
    public void update(String id) {
        for (int i = 0; i < s.channels.size(); i++) {
            Channel ch = (Channel) s.channels.elementAt(i);
            if (id != null && !ch.id.equals(id)) continue;

            set(i, ch.toString(s), null, null, s.unreads.hasUnreads(ch));
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
        else if (c == markChannelReadCommand) {
            Channel ch = (Channel) s.channels.elementAt(getSelectedIndex());
            s.unreads.markRead(ch);
            update(ch.id);
            s.guildSelector.update(s.selectedGuild.id);
        }
        else if (c == markGuildReadCommand) {
            s.unreads.markRead(s.selectedGuild);
            update();
            s.guildSelector.update(s.selectedGuild.id);
        }
        else if (c == SELECT_COMMAND) {
            s.isDM = false;
            s.selectedChannel = (Channel) s.channels.elementAt(getSelectedIndex());
            s.openChannelView(true);
        }
    }
}