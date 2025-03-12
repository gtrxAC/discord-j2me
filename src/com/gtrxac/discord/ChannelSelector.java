package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class ChannelSelector extends ListScreen implements CommandListener, Strings {
    private Command viewThreadsCommand;
    private Command refreshCommand;
    private Command markChannelReadCommand;
    private Command markGuildReadCommand;
    // ifdef OVER_100KB
    private Command muteCommand;
    // endif

    public ChannelSelector() throws Exception {
        super(App.selectedGuild.name, true, true, false);
        setCommandListener(this);

        UnreadManager.autoSave = false;
        for (int i = 0; i < App.channels.size(); i++) {
            Channel ch = (Channel) App.channels.elementAt(i);
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

        if (App.channels.size() > 0) {
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
        for (int i = 0; i < App.channels.size(); i++) {
            Channel ch = (Channel) App.channels.elementAt(i);
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
            if (!Settings.highRamMode && !App.gatewayActive()) {
                App.channels = null;
                App.selectedGuild.channels = null;
            }
            App.guildSelector.update(App.selectedGuild.id);
            App.disp.setCurrent(App.guildSelector);
        }
        else if (c == refreshCommand) {
            App.openChannelSelector(true, true);
        }
        else if (c == markGuildReadCommand) {
            App.selectedGuild.markRead();
            update();
            App.guildSelector.update(App.selectedGuild.id);
        }
        else {
            Channel ch = (Channel) App.channels.elementAt(getSelectedIndex());

            if (c == markChannelReadCommand) {
                ch.markRead();
                update(ch.id);
                App.guildSelector.update(App.selectedGuild.id);
            }
            // ifdef OVER_100KB
            else if (c == muteCommand) {
                FavoriteGuilds.toggleMute(ch.id);
                update(ch.id);
            }
            // endif
            else if (c == SELECT_COMMAND && !ch.isForum) {
                App.isDM = false;
                App.selectedChannel = ch;
                App.openChannelView(true);
            }
            // "View threads" command was used, or a channel was selected and it's a forum channel
            else {
                App.selectedChannelForThreads = ch;
                App.openThreadSelector(true, false);
            }
        }
    }
}