package com.gtrxac.discord;

import java.io.*;
import cc.nnproject.json.*;
import java.util.*;

public class HeartbeatThread extends Thread {
    State s;
    private OutputStream os;

    int lastReceived;
    private long lastHeartbeat;
    private int interval;
    boolean stop;

    public HeartbeatThread(State s, OutputStream os, int interval) {
        this.s = s;
        this.os = os;
        this.interval = interval;
        this.lastReceived = -1;
    }

    public void run() {
        try {
            while (true) {
                if (stop) break;
                long now = new Date().getTime();
                
                if (interval > 0 && now > lastHeartbeat + interval) {
                    JSONObject hbMsg = new JSONObject();
                    hbMsg.put("op", 1);
                    if (lastReceived >= 0) {
                        hbMsg.put("d", lastReceived);
                    } else {
                        hbMsg.put("d", JSON.json_null);
                    }

                    os.write(hbMsg.build().getBytes());
                    os.write("\n".getBytes());
                    lastHeartbeat = now;
                    // System.out.println("Sent heartbeat");
                }

                Thread.sleep(interval);
            }
        }
        catch (Exception e) {
            s.error(e.toString());
        }
    }
}