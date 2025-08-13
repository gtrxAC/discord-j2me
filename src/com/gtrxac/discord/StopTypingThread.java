// ifdef OVER_100KB
package com.gtrxac.discord;

import java.io.*;
import cc.nnproject.json.*;
import java.util.*;

public class StopTypingThread extends Thread {
    String userID;

    public StopTypingThread(String userID) {
        this.userID = userID;
    }

    public void run() {
        Util.sleep(10000);

        for (int i = 0; i < App.typingUsers.size(); i++) {
            if (App.typingUserIDs.elementAt(i).equals(userID)) {
                App.typingUsers.removeElementAt(i);
                App.typingUserIDs.removeElementAt(i);
                App.channelView.repaint();
                return;
            }
        }
    }
}
// endif