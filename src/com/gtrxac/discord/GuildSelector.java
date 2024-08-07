package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.*;

public class GuildSelector extends List implements CommandListener {
    State s;
    boolean isFavGuilds;

    private Vector guilds;
    private Command backCommand;
    private Command addFavCommand;
    private Command removeFavCommand;
    private Command refreshCommand;

    public GuildSelector(State s, Vector guilds, boolean isFavGuilds) throws Exception {
        super("Servers", List.IMPLICIT);
        if (isFavGuilds) setTitle("Favorites");

        setCommandListener(this);
        this.s = s;
        this.guilds = guilds;
        this.isFavGuilds = isFavGuilds;

        for (int i = 0; i < guilds.size(); i++) {
            Guild g = (Guild) guilds.elementAt(i);
            append(g.toString(s), s.iconCache.get(g));
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 3);
        addCommand(backCommand);
        addCommand(refreshCommand);

        if (isFavGuilds) {
            removeFavCommand = new Command("Remove", Command.ITEM, 1);
            addCommand(removeFavCommand);
        } else {
            addFavCommand = new Command("Favorite", "Add to favorites", Command.ITEM, 1);
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

            set(i, g.toString(s), s.iconCache.get(g));
        }
    }

    /**
     * Updates the icons and unread indicators for all servers shown in this selector.
     */
    public void update() { update(null); }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(new MainMenu(s));
        }
        else if (c == refreshCommand) {
            if (isFavGuilds) {
                FavoriteGuilds.openSelector(s, true);
            } else {
                s.openGuildSelector(true);
            }
        }
        else if (c == addFavCommand) {
            Guild g = (Guild) guilds.elementAt(getSelectedIndex());
            FavoriteGuilds.add(s, g);
        }
        else if (c == removeFavCommand) {
            FavoriteGuilds.remove(s, getSelectedIndex());
            FavoriteGuilds.openSelector(s, false);
        }
        else if (c == List.SELECT_COMMAND) {
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
            s.openChannelSelector(false);
        }
    }
}
