package com.gtrxac.discord;

import java.io.*;
import cc.nnproject.json.*;
import java.util.*;

public class StopTypingThread extends Thread {
    State s;
    String userID;

    public StopTypingThread(State s, String userID) {
        this.s = s;
        this.userID = userID;
    }

    public void run() {
        Util.sleep(10000);

        for (int i = 0; i < s.typingUsers.size(); i++) {
            if (s.typingUserIDs.elementAt(i).equals(userID)) {
                s.typingUsers.removeElementAt(i);
                s.typingUserIDs.removeElementAt(i);
                s.channelView.repaint();
                return;
            }
        }
    }
}
