// ifdef SAMSUNG_100KB
package com.gtrxac.discord;

import java.io.*;
import cc.nnproject.json.*;
import java.util.*;

public class Threads100kb extends Thread implements Strings {
    public static final int HEARTBEAT_THREAD = 0;
    public static final int STOP_TYPING_THREAD = 1;

    public int threadType;

    // HeartbeatThread
    int lastReceived;
    private int interval;
    volatile boolean stop;

    public Threads100kb(int interval) {
        this.interval = interval - 3000;  // Discord already more or less accounts for network latency but this is 2G we're talking about
        this.lastReceived = -1;
        threadType = HEARTBEAT_THREAD;
    }

    // StopTypingThread
    String userID;

    public Threads100kb(String userID) {
        this.userID = userID;
        threadType = STOP_TYPING_THREAD;
    }

    public void run() {
        switch (threadType) {
            case HEARTBEAT_THREAD: {
                try {
                    while (true) {
                        if (stop) break;
                        
                        JSONObject hbMsg = new JSONObject();
                        hbMsg.put("op", 1);
                        if (lastReceived >= 0) {
                            hbMsg.put("d", lastReceived);
                        } else {
                            hbMsg.put("d", JSON.json_null);
                        }
                        App.gateway.send(hbMsg);

                        Util.sleep(interval);
                    }
                }
                catch (Exception e) {
                    App.gateway.stopMessage = Locale.get(HEARTBEAT_THREAD_ERROR) + e.toString();
                    App.gateway.stop = true;
                }
                break;
            }

            case STOP_TYPING_THREAD: {
                Util.sleep(10000);

                for (int i = 0; i < App.typingUsers.size(); i++) {
                    if (App.typingUserIDs.elementAt(i).equals(userID)) {
                        App.typingUsers.removeElementAt(i);
                        App.typingUserIDs.removeElementAt(i);
                        App.channelView.repaint();
                        return;
                    }
                }
                break;
            }
        }
    }
}
// endif