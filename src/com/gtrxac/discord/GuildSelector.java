package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class GuildSelector extends List implements CommandListener {
    State s;
    private Command backCommand;
    private Command refreshCommand;
    private Command dmCommand;

    public GuildSelector(State s) {
        super("Failed to log in", List.IMPLICIT);
        try {
            JSONObject response = JSON.getObject(s.http.get("/users/@me"));
            setTitle("Servers (@" + response.getString("username") + ")");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        setCommandListener(this);
        this.s = s;

        try {
            Guild.fetchGuilds(s);
            for (int i = 0; i < s.guilds.size(); i++) {
                append(((Guild) s.guilds.elementAt(i)).name, null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            setFitPolicy(Choice.TEXT_WRAP_ON);
            append(e.toString(), null);
        }

        backCommand = new Command("Back", Command.BACK, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        dmCommand = new Command("Direct messages", Command.ITEM, 2);
        addCommand(backCommand);
        addCommand(refreshCommand);
        addCommand(dmCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(new LoginForm(s));
        }
        if (c == refreshCommand) {
            s.disp.setCurrent(new LoadingScreen());
            s.guildSelector = new GuildSelector(s);
            s.disp.setCurrent(s.guildSelector);
        }
        if (c == List.SELECT_COMMAND) {
            Guild newGuild = (Guild) s.guilds.elementAt(getSelectedIndex());

            if (s.selectedGuild == null || newGuild.id != s.selectedGuild.id) {
                s.selectedGuild = newGuild;
                s.disp.setCurrent(new LoadingScreen());
                s.channelSelector = new ChannelSelector(s);
            }
            s.disp.setCurrent(s.channelSelector);
        }
        if (c == dmCommand) {
            s.disp.setCurrent(new DMSelector(s));
        }
    }
}
