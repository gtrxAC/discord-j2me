package com.gtrxac.discord;

import java.io.*;
import cc.nnproject.json.*;
import java.util.*;

public class HeartbeatThread extends Thread implements Strings {
    private GatewayThread gateway;

    int lastReceived;
    private long lastHeartbeat;
    private int interval;
    volatile boolean stop;

    public HeartbeatThread(GatewayThread gateway, int interval) {
        this.gateway = gateway;
        this.interval = interval - 3000;  // Discord already more or less accounts for network latency but this is 2G we're talking about
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

                    gateway.send(hbMsg);
                    lastHeartbeat = now;
                }
                Util.sleep(interval);
            }
        }
        catch (Exception e) {
            gateway.stopMessage = Locale.get(HEARTBEAT_THREAD_ERROR) + e.toString();
            gateway.stop = true;
        }
    }
}