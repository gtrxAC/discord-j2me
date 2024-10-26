package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends ListScreen implements CommandListener, Strings {
    State s;
    boolean isFavGuilds;

    private Vector guilds;
    private Command addFavCommand;
    private Command removeFavCommand;
    private Command refreshCommand;

    public GuildSelector(State s, Vector guilds, boolean isFavGuilds) throws Exception {
        super(Locale.get(GUILD_SELECTOR_TITLE), true, true, false);
        
        if (isFavGuilds) setTitle(Locale.get(FAVORITE_SELECTOR_TITLE));

        setCommandListener(this);
        this.s = s;
        this.guilds = guilds;
        this.isFavGuilds = isFavGuilds;

        for (int i = 0; i < guilds.size(); i++) {
            Guild g = (Guild) guilds.elementAt(i);
            append(g.name, null, s.iconCache.getResized(g, s.menuIconSize), s.unreads.hasUnreads(g));
        }

        refreshCommand = Locale.createCommand(REFRESH, Command.ITEM, 3);
        addCommand(refreshCommand);

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

            set(i, g.name, null, s.iconCache.getResized(g, s.menuIconSize), s.unreads.hasUnreads(g));
        }
    }

    /**
     * Updates the icons and unread indicators for all servers shown in this selector.
     */
    public void update() { update(null); }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            // Unload server list if needed, and go back to main menu
            if (!s.highRamMode) s.guilds = null;
            s.disp.setCurrent(MainMenu.get(null));
        }
        else if (c == refreshCommand) {
            if (isFavGuilds) {
                FavoriteGuilds.openSelector(s, true, true);
            } else {
                s.openGuildSelector(true, true);
            }
        }
        else if (c == addFavCommand) {
            Guild g = (Guild) guilds.elementAt(getSelectedIndex());
            FavoriteGuilds.add(s, g);
        }
        else if (c == removeFavCommand) {
            FavoriteGuilds.remove(s, getSelectedIndex());
            FavoriteGuilds.openSelector(s, false, false);
        }
        else if (c == SELECT_COMMAND) {
            Guild newGuild = (Guild) guilds.elementAt(getSelectedIndex());

            // If gateway is active, subscribe to typing events for this server, if not already subscribed
            if (s.gatewayActive() && s.subscribedGuilds.indexOf(newGuild.id) == -1) {
                JSONObject subGuild = new JSONObject();
                subGuild.put("typing", true);

                JSONObject subList = new JSONObject();
                subList.put(newGuild.id, subGuild);

                JSONObject subData = new JSONObject();
                subData.put("subscriptions", subList);

                JSONObject subMsg = new JSONObject();
                subMsg.put("op", 37);
                subMsg.put("d", subData);

                s.gateway.send(subMsg);
                s.subscribedGuilds.addElement(newGuild.id);
            }

            s.selectedGuild = newGuild;
            s.openChannelSelector(false, false);
        }
    }
}
