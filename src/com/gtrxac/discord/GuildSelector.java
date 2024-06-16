package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class GuildSelector extends List implements CommandListener {
    State s;

    private Command backCommand;
    private Command refreshCommand;

    public GuildSelector(State s) throws Exception {
        super("Servers", List.IMPLICIT);
        setCommandListener(this);
        this.s = s;

        for (int i = 0; i < s.guilds.size(); i++) {
            Guild g = (Guild) s.guilds.elementAt(i);
            Image icon = (s.iconCache != null) ? s.iconCache.get(g) : null;
            append(g.toString(s), icon);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(refreshCommand);
    }

    /**
     * Updates the icons and unread/ping indicators for server names shown in this selector.
     */
    public void update() {
        for (int i = 0; i < s.guilds.size(); i++) {
            Guild g = (Guild) s.guilds.elementAt(i);
            Image icon = (s.iconCache != null) ? s.iconCache.get(g) : null;
            set(i, g.toString(s), icon);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(new MainMenu(s));
        }
        if (c == refreshCommand) {
            s.openGuildSelector(true);
        }
        if (c == List.SELECT_COMMAND) {
            Guild newGuild = (Guild) s.guilds.elementAt(getSelectedIndex());

            // If gateway is active, subscribe to typing events for this server, if not already subscribed
            if (s.gateway != null && s.gateway.isAlive() && s.subscribedGuilds.indexOf(newGuild.id) == -1) {
                JSONObject subGuild = new JSONObject();
                subGuild.put("typing", true);

                JSONObject subList = new JSONObject();
                subList.put(newGuild.id, subGuild);

                JSONObject subData = new JSONObject();
                subData.put("subscriptions", subList);

                JSONObject subMsg = new JSONObject();
                subMsg.put("op", 37);
                subMsg.put("d", subData);

                try {
                    s.gateway.os.write((subMsg.build() + "\n").getBytes());
                    s.gateway.os.flush();
                }
                catch (Exception e) {}

                s.subscribedGuilds.addElement(newGuild.id);
            }

            s.selectedGuild = newGuild;
            s.openChannelSelector(false);
        }
    }
}
