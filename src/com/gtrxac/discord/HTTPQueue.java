package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;

public class HTTPQueue implements Runnable {
    // Time before a connection will be closed for timeout.
    private static final int TIMEOUT_MS = 60000;

    // Max amount of possible HTTP connections
    // Nokia S40 has a limit of 8 HTTP connections, if they run out, you get "No response entries available"
    // Most Motorola P2K phones have max 4 sockets, gateway is one socket, so 3 HTTP connections
//#ifdef S40V2
    private static final int MAX_SLOTS = 8;
//#else
    private static final int MAX_SLOTS = 3;
//#endif

    private long timestamp;
    InputStream is;
    OutputStream os;
    HttpConnection hc;

    private static volatile Vector queueItems = new Vector(MAX_SLOTS);

    static {
        new Thread(new HTTPQueue()).start();
    }

    private HTTPQueue() {
        timestamp = System.currentTimeMillis();
    }

    public static synchronized HTTPQueue newQueueItem() {
        // wait for available slot
        while (queueItems.size() >= MAX_SLOTS) {
            // System.out.println("waiting, slots filled " + queueItems.size());
            Util.sleep(100);
        }

        HTTPQueue result = new HTTPQueue();
        queueItems.addElement(result);
        return result;
    }

    public void run() {
        while (true) {
            long expireTime = System.currentTimeMillis() + TIMEOUT_MS;
            int closedCount = 0;

            for (int i = 0; i < queueItems.size(); ) {
                HTTPQueue item = (HTTPQueue) queueItems.elementAt(i);

                if (item.timestamp >= expireTime) {
                    item.close();
                    queueItems.removeElementAt(i);
                    closedCount++;
                }
                else i++;
            }

            // System.out.println("Checked connections, closed " + closedCount + ", currently " + queueItems.size());
            Util.sleep(5000);
        }
    }

    private void close() {
        // System.out.println(this.toString() + " closed");
        try { is.close(); } catch (Exception e) {}
        try { os.close(); } catch (Exception e) {}
        try { hc.close(); } catch (Exception e) {}
    }

    public void finished() {
        close();
        queueItems.removeElement(this);
    }
}