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
    private InputStream is;
    private OutputStream os;

    public GatewayThread(State s, String gateway, String token) {
        this.s = s;
        this.gateway = s.getPlatformSpecificUrl(gateway);
        this.token = token;
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
                        hbThread.stop = true;
                        try {
                            if (is != null) is.close();
                            if (os != null) os.close();
                            if (sc != null) sc.close();
                        }
                        catch (Exception ee) {}
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

                // System.out.println(msgStr);

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
                        // events.add("READY");

                        JSONObject connData = new JSONObject();
                        connData.put("supported_events", events);
                        connData.put("url", "wss://gateway.discord.gg/?v=9&encoding=json");

                        JSONObject connMsg = new JSONObject();
                        connMsg.put("op", -1);
                        connMsg.put("t", "GATEWAY_CONNECT");
                        connMsg.put("d", connData);

                        os.write(connMsg.build().getBytes());
                        os.write("\n".getBytes());
                    }
                    else if (op.equals("MESSAGE_CREATE")) {
                        // Check that a channel is opened
                        if (s.disp.getCurrent() != s.channelView && s.disp.getCurrent() != s.oldChannelView) continue;
                        
                        // Check that the opened channel is the one where the message was sent
                        JSONObject msgData = message.getObject("d");
                        String channel = msgData.getString("channel_id");
                        if (s.isDM) {
                            if (!channel.equals(s.selectedDmChannel.id)) continue;
                        } else {
                            if (!channel.equals(s.selectedChannel.id)) continue;
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
                                s.channelView.update();

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
                    idData.put("token", token);
                    // GUILD_MESSAGES, DIRECT_MESSAGES, MESSAGE_CONTENT
                    // idData.put("intents", (1 << 9) | (1 << 12) | (1 << 15));
                    idData.put("capabilities", 1);
                    idData.put("properties", idProps);
            
                    JSONObject idMsg = new JSONObject();
                    idMsg.put("op", 2);
                    idMsg.put("d", idData);

                    os.write(idMsg.build().getBytes());
                    os.write("\n".getBytes());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            s.error("Gateway error: " + e.toString());

            if (hbThread != null) hbThread.stop = true;
            try {
                if (is != null) is.close();
                if (os != null) os.close();
                if (sc != null) sc.close();
            }
            catch (Exception ee) {}
        }
    }
}