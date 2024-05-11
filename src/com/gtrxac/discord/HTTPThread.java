package com.gtrxac.discord;

import java.util.Vector;

import cc.nnproject.json.*;

public class HTTPThread extends Thread {
    public static final int FETCH_GUILDS = 0;

    State s;
    int action;

    public HTTPThread(State s) {
        this.s = s;
    }

    public void run() {
        try {
            switch (action) {
                case FETCH_GUILDS: {
                    JSONArray guilds = JSON.getArray(s.http.get("/users/@me/guilds"));
                    s.guilds = new Vector();

                    for (int i = 0; i < guilds.size(); i++) {
                        s.guilds.addElement(new Guild(guilds.getObject(i)));
                    }
                    if (s.guildSelector != null) s.guildSelector.load();
                    break;
                }
            }
        }
        catch (Exception e) {
            s.error(e.toString());
        }
    }
}
