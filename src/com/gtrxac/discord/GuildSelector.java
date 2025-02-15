package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends ListScreen implements CommandListener, Strings {
    boolean isFavGuilds;

    private Vector guilds;
    private Command addFavCommand;
    private Command removeFavCommand;
    private Command refreshCommand;
    // ifdef OVER_100KB
    private Command muteCommand;
    // endif

    public GuildSelector(Vector guilds, boolean isFavGuilds) throws Exception {
        super(Locale.get(GUILD_SELECTOR_TITLE), true, true, false);
        
        if (isFavGuilds) setTitle(Locale.get(FAVORITE_SELECTOR_TITLE));

        setCommandListener(this);
        this.guilds = guilds;
        this.isFavGuilds = isFavGuilds;

        UnreadManager.autoSave = false;
        for (int i = 0; i < guilds.size(); i++) {
            Guild g = (Guild) guilds.elementAt(i);
            append(g.name, null, IconCache.getResized(g, Settings.menuIconSize), g.getMenuIndicator());
        }
        UnreadManager.manualSave();

        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 3);
        addCommand(refreshCommand);

        // ifdef OVER_100KB
        muteCommand = Locale.createCommand(MUTE, Command.ITEM, 6);
        addCommand(muteCommand);
        // endif

        if (isFavGuilds) {
            removeFavCommand = Locale.createCommand(REMOVE, Command.ITEM, 2);
            addCommand(removeFavCommand);
        } else {
            addFavCommand = Locale.createCommand(ADD_FAVORITE, Command.ITEM, 2);
            addCommand(addFavCommand);
        }
    }

    /**
     * Updates the icon and unread indicator for a server shown in this selector.
     */
    public void update(String id) {
        for (int i = 0; i < guilds.size(); i++) {
            Guild g = (Guild) guilds.elementAt(i);
            if (id != null && !g.id.equals(id)) continue;

            set(i, g.name, null, IconCache.getResized(g, Settings.menuIconSize), g.getMenuIndicator());
        }
    }

    /**
     * Updates the icons and unread indicators for all servers shown in this selector.
     */
    public void update() { update(null); }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            // Unload server list if needed, and go back to main menu
            if (!Settings.highRamMode) App.guilds = null;
            App.disp.setCurrent(MainMenu.get(false));
        }
        else if (c == refreshCommand) {
            if (isFavGuilds) {
                FavoriteGuilds.openSelector(true, true);
            } else {
                App.openGuildSelector(true, true);
            }
        }
        else if (c == removeFavCommand) {
            FavoriteGuilds.remove(getSelectedIndex());
            FavoriteGuilds.openSelector(false, false);
        }
        else {
            Guild g = (Guild) guilds.elementAt(getSelectedIndex());

            if (c == SELECT_COMMAND) {
                // If gateway is active, subscribe to typing events for this server, if not already subscribed
                if (App.gatewayActive() && App.subscribedGuilds.indexOf(g.id) == -1) {
                    JSONObject subGuild = new JSONObject();
                    subGuild.put("typing", true);
    
                    JSONObject subList = new JSONObject();
                    subList.put(g.id, subGuild);
    
                    JSONObject subData = new JSONObject();
                    subData.put("subscriptions", subList);
    
                    JSONObject subMsg = new JSONObject();
                    subMsg.put("op", 37);
                    subMsg.put("d", subData);
    
                    App.gateway.send(subMsg);
                    App.subscribedGuilds.addElement(g.id);
                }
    
                App.selectedGuild = g;
                App.openChannelSelector(false, false);
            }
            // ifdef OVER_100KB
            else if (c == muteCommand) {
                FavoriteGuilds.toggleMute(g.id);
                update(g.id);
            }
            // endif
            else {
                // 'add to favorites' command
                FavoriteGuilds.add(g);
            }
        }
    }
}
