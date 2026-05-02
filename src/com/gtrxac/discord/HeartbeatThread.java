//#ifdef OVER_100KB
package com.gtrxac.discord;

import java.io.*;
import cc.nnproject.json.*;
import java.util.*;

//#ifdef MODERNCONNECTOR
import tech.alicesworld.ModernConnector.*;
//#endif

public class HeartbeatThread extends Thread implements Strings {
    int lastReceived;
    private int interval;
    volatile boolean stop;

//#ifdef MODERNCONNECTOR
    boolean forProxylessQrLogin;
    WebSocketClient ws;
//#endif

    public HeartbeatThread(int interval) {
        this.interval = interval - 3000;  // Discord already more or less accounts for network latency but this is 2G we're talking about
        this.lastReceived = -1;
    }

    public void run() {
        try {
            while (true) {
                if (stop) break;
                
//#ifdef MODERNCONNECTOR
                if (forProxylessQrLogin) {
                    ws.sendMessage("{\"op\":\"heartbeat\"}");
                } else
//#endif
                {
                    JSONObject hbMsg = new JSONObject();
                    hbMsg.put("op", 1);
                    if (lastReceived >= 0) {
                        hbMsg.put("d", lastReceived);
                    } else {
                        hbMsg.put("d", JSON.json_null);
                    }
                    App.gateway.send(hbMsg);
                }

                Util.sleep(interval);
            }
        }
        catch (Exception e) {
//#ifdef MODERNCONNECTOR
            if (!forProxylessQrLogin)
//#endif
            {
                App.gateway.stopMessage = Locale.get(HEARTBEAT_THREAD_ERROR) + e.toString();
                App.gateway.stop = true;
            }
        }
    }
}
//#endif