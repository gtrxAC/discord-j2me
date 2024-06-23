package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;

import cc.nnproject.json.*;

public class GatewayThread extends Thread {
    State s;
    String gateway;
    String token;
    boolean stop;

    HeartbeatThread hbThread;

    private SocketConnection sc;
    InputStream is;
    OutputStream os;

    public GatewayThread(State s, String gateway, String token) {
        this.s = s;
        this.gateway = s.getPlatformSpecificUrl(gateway);
        this.token = token;

        s.subscribedGuilds = new Vector();
    }

    private void disconnect() {
        if (hbThread != null) hbThread.stop = true;
        try {
            if (is != null) is.close();
            if (os != null) os.close();
            if (sc != null) sc.close();
        }
        catch (Exception ee) {}
    }

    public void run() {
        try {
            sc = (SocketConnection) Connector.open(gateway);
            sc.setSocketOption(SocketConnection.KEEPALIVE, 1);

            is = sc.openInputStream();
            os = sc.openOutputStream();
            
            StringBuffer sb = new StringBuffer();
            String msgStr;

            while (true) {
                // Get message
                while (true) {
                    if (stop) {
                        disconnect();
                        return;
                    }

                    int ch = is.read();
                    if (ch == '\n' || ch == -1) {
                        if (sb.length() > 0) {
                            // This message has been fully received, start processing it
                            msgStr = new String(sb.toString().getBytes(), "UTF-8");
                            sb = new StringBuffer();
                            break;
                        }
                    } else {
                        sb.append((char) ch);
                    }
                }

                // Process message
                JSONObject message = JSON.getObject(msgStr);
                String op = message.getString("t", "");

                // Save message sequence number (used for heartbeats)
                int seq = message.getInt("s", -1);
                if (hbThread != null && seq > hbThread.lastReceived) {
                    hbThread.lastReceived = seq;
                }

                if (op != null) {
                    if (op.equals("GATEWAY_HELLO")) {
                        // Connect to gateway
                        JSONArray events = new JSONArray();
                        events.add("MESSAGE_CREATE");
                        events.add("TYPING_START");

                        JSONObject connData = new JSONObject();
                        connData.put("supported_events", events);
                        connData.put("url", "wss://gateway.discord.gg/?v=9&encoding=json");

                        JSONObject connMsg = new JSONObject();
                        connMsg.put("op", -1);
                        connMsg.put("t", "GATEWAY_CONNECT");
                        connMsg.put("d", connData);

                        os.write((connMsg.build() + "\n").getBytes());
                        os.flush();
                    }
                    else if (op.equals("GATEWAY_DISCONNECT")) {
                        disconnect();
                        String reason = message.getObject("d").getString("message");
                        s.disp.setCurrent(new GatewayAlert(s, reason), s.disp.getCurrent());
                        return;
                    }
                    else if (op.equals("MESSAGE_CREATE")) {
                        JSONObject msgData = message.getObject("d");
                        String msgId = msgData.getString("id");
                        String chId = msgData.getString("channel_id");

                        // Mark this channel as unread
                        // TODO: pings
                        Channel ch = Channel.getByID(s, chId);
                        if (ch != null) {
                            ch.lastMessageID = Long.parseLong(msgId);
                            s.updateUnreadIndicators();
                        }

                        // Check that a channel has been opened
                        if (s.channelView == null && s.oldChannelView == null) continue;
                        
                        // Check that the opened channel is the one where the message was sent
                        if (s.isDM) {
                            if (!chId.equals(s.selectedDmChannel.id)) continue;
                        } else {
                            if (!chId.equals(s.selectedChannel.id)) continue;
                        }

                        // If we're on the newest page, make the new message visible
                        int page = s.oldUI ? s.oldChannelView.page : s.channelView.page;
                        if (page == 0) {
                            // Add the new message to the message list
                            s.messages.insertElementAt(new Message(s, msgData), 0);

                            // Remove the oldest message in the message list so it doesn't break pagination
                            // Except for channels that have less messages than the full page capacity
                            if (s.messages.size() > s.messageLoadCount) {
                                s.messages.removeElementAt(s.messages.size() - 1);
                            }
                        }

                        // Remove this user's typing indicator
                        if (s.isDM) {
                            if (s.typingUsers.size() >= 1) {
                                s.typingUsers.removeElementAt(0);
                                s.typingUserIDs.removeElementAt(0);
                            }
                        } else {
                            String authorID = msgData.getObject("author").getString("id");
                            
                            for (int i = 0; i < s.typingUsers.size(); i++) {
                                if (s.typingUserIDs.elementAt(i).equals(authorID)) {
                                    s.typingUsers.removeElementAt(i);
                                    s.typingUserIDs.removeElementAt(i);
                                }
                            }
                        }

                        // Redraw the message list
                        if (s.oldUI) {
                            if (page == 0) {
                                s.oldChannelView.update();
                            } else {
                                s.oldChannelView.setTitle("Refresh for new msgs");
                            }
                        } else {
                            if (page == 0) {
                                int oldScroll = s.channelView.scroll;
                                boolean atBottom = oldScroll == s.channelView.maxScroll;
                                s.channelView.update(false);

                                if (atBottom) s.channelView.scroll = s.channelView.maxScroll;
                                else s.channelView.scroll = oldScroll;
                            } else {
                                // If user is not on the newest page of messages, ask them to refresh
                                // There is no easy way to do it any other way without breaking pagination
                                s.channelView.outdated = true;
                            }
                            s.channelView.repaint();
                        }
                    }
                    else if (op.equals("TYPING_START")) {
                        if (s.channelView == null && s.oldChannelView == null) continue;

                        JSONObject msgData = message.getObject("d");
                        String channel = msgData.getString("channel_id");

                        // Check that the opened channel (if there is any) is the one where the typing event happened
                        if (s.isDM) {
                            if (!channel.equals(s.selectedDmChannel.id)) continue;
                        } else {
                            if (!channel.equals(s.selectedChannel.id)) continue;
                        }

                        if (s.isDM) {
                            // Typing events not supported in group DMs (typing event contains guild member info if it happened in a server, but not user info; in a group DM, there's no easy way to know who started typing)
                            if (s.selectedDmChannel.isGroup) continue;

                            // If we are in a one person DM, then we know the typing user is the other participant
                            // If we already have a typing indicator, don't create a dupe
                            if (s.typingUsers.size() >= 1) continue;

                            s.typingUsers.addElement(s.selectedDmChannel.name);
                            s.typingUserIDs.addElement("0");

                            // Remove the name from the typing list after 10 seconds
                            StopTypingThread stopThread = new StopTypingThread(s, "0");
                            stopThread.start();
                        } else {
                            try {
                                // Get this user's name and add it to the typing users list
                                JSONObject userObj = msgData.getObject("member").getObject("user");
                                
                                String author = userObj.getString("global_name", "(no name)");
                                if (author == null || author == "(no name)") {
                                    author = userObj.getString("username", "(no name)");
                                }

                                // If this user is already in the list, don't add them again
                                String id = userObj.getString("id");
                                if (s.typingUserIDs.indexOf(id) != -1) continue;

                                s.typingUsers.addElement(author);
                                s.typingUserIDs.addElement(id);

                                StopTypingThread stopThread = new StopTypingThread(s, id);
                                stopThread.start();
                            }
                            catch (Exception e) {}
                        }

                        // Redraw the message list
                        if (s.oldUI) {
                            s.oldChannelView.update();
                        } else {
                            s.channelView.repaint();
                        }
                    }
                }
                else if (message.getInt("op", 0) == 10) {
                    int heartbeatInterval = message.getObject("d").getInt("heartbeat_interval");
                    hbThread = new HeartbeatThread(s, os, heartbeatInterval);
                    hbThread.start();

                    // Identify
                    JSONObject idProps = new JSONObject();
                    idProps.put("os", "Linux");
                    idProps.put("browser", "Firefox");
                    idProps.put("device", "");
            
                    JSONObject idData = new JSONObject();
                    idData.put("token", token.trim());
                    idData.put("capabilities", 30717);
                    idData.put("properties", idProps);
            
                    JSONObject idMsg = new JSONObject();
                    idMsg.put("op", 2);
                    idMsg.put("d", idData);

                    try {
                        os.write((idMsg.build() + "\n").getBytes("UTF-8"));
                    }
                    catch (UnsupportedEncodingException e) {
                        os.write((idMsg.build() + "\n").getBytes());
                    }
                    os.flush();
                }
            }
        }
        catch (Exception e) {
            disconnect();
            s.disp.setCurrent(new GatewayAlert(s, e.toString()));
        }
    }
}