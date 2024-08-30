package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import javax.microedition.io.*;

public class HTTPThread extends Thread {
    static final int FETCH_GUILDS = 0;
    static final int FETCH_CHANNELS = 1;
    static final int FETCH_DM_CHANNELS = 2;
    static final int FETCH_MESSAGES = 3;
    static final int SEND_MESSAGE = 4;
    static final int EDIT_MESSAGE = 5;
    static final int DELETE_MESSAGE = 6;

    State s;
    int action;

    // Parameters for FETCH_GUILDS
    boolean showFavGuilds;

    // Parameters for FETCH_MESSAGES
    String fetchMsgsBefore;
    String fetchMsgsAfter;

	// Parameters for SEND_MESSAGE
	String sendMessage;
	String sendReference;  // ID of the message the user is replying to
	boolean sendPing;

    // Parameters for EDIT_MESSAGE
    // editMessage is also used for DELETE_MESSAGE
    Message editMessage;
    String editContent;

    public HTTPThread(State s, int action) {
        this.s = s;
        this.action = action;
        setPriority(Thread.MAX_PRIORITY);
    }

    public void run() {
        Displayable prevScreen = s.disp.getCurrent();
        
        // Create and show loading screen
        Form loadScr = new Form(null);
        loadScr.append(new StringItem(null, "Loading"));
        s.disp.setCurrent(loadScr);
        
        try {
            // Get the currently logged in user's ID (to determine which messages can be edited/deleted)
            // Note: this ID does not match the actual Discord ID but is generated based on it (see proxy script)
            if (s.myUserId == null) s.myUserId = s.http.get("/users/@me");

            switch (action) {
                case FETCH_GUILDS: {
                    JSONArray guilds = JSON.getArray(s.http.get("/users/@me/guilds"));
                    s.guilds = new Vector();

                    for (int i = 0; i < guilds.size(); i++) {
                        s.guilds.addElement(new DiscordObject(guilds.getArray(i)));
                    }
                    s.guildSelector = new GuildSelector(s, s.guilds);
                    s.disp.setCurrent(s.guildSelector);
                    break;
                }

                case FETCH_CHANNELS: {
                    JSONArray channels = JSON.getArray(s.http.get("/guilds/" + s.selectedGuild.id + "/channels"));
                    s.channels = new Vector();

                    for (int i = 0; i < channels.size(); i++) {
                        s.channels.addElement(new DiscordObject(channels.getArray(i)));
                    }
                    s.channelSelector = new ChannelSelector(s);
                    s.disp.setCurrent(s.channelSelector);
                    break;
                }

                case FETCH_DM_CHANNELS: {
                    JSONArray channels = JSON.getArray(s.http.get("/users/@me/channels"));
                    s.channels = new Vector();
            
                    for (int i = 0; i < channels.size(); i++) {
                        s.channels.addElement(new DiscordObject(channels.getArray(i)));
                    }
                    s.channelSelector = new ChannelSelector(s);
                    s.disp.setCurrent(s.channelSelector);
                    break;
                }

                case SEND_MESSAGE: {
                    JSONObject json = new JSONObject();
                    json.put("content", sendMessage);
                    json.put("flags", 0);
                    json.put("mobile_network_type", "unknown");
                    json.put("tts", false);

                    // Reply
                    if (sendReference != null) {
                        JSONObject ref = new JSONObject();
                        ref.put("channel_id", s.selectedChannel.id);
                        if (!s.isDM) ref.put("guild_id", s.selectedGuild.id);
                        ref.put("message_id", sendReference);
                        json.put("message_reference", ref);

                        if (!sendPing && !s.isDM) {
                            JSONObject ping = new JSONObject();
                            ping.put("replied_user", false);
                            json.put("allowed_mentions", ping);
                        }
                    }

                    s.http.post("/channels/" + s.selectedChannel.id + "/messages", json);

                    // fall through (fetch messages because there might have 
                    // been other messages sent during the time the user was writing their message)
                }

                case FETCH_MESSAGES: {
                    StringBuffer url = new StringBuffer(
                        "/channels/" + s.selectedChannel.id + "/messages?limit=" + s.messageLoadCount
                    );
                    if (fetchMsgsBefore != null) url.append("&before=" + fetchMsgsBefore);
                    if (fetchMsgsAfter != null) url.append("&after=" + fetchMsgsAfter);

                    JSONArray messages = JSON.getArray(s.http.get(url.toString()));
                    s.messages = new Vector();

                    for (int i = 0; i < messages.size(); i++) {
                        s.messages.addElement(new Message(s, messages.getArray(i)));
                    }

                    if ((fetchMsgsBefore == null && fetchMsgsAfter == null) || sendMessage != null) {
                        // If user opened a new channel or sent a message, create a new channel view
                        s.channelView = new ChannelView(s);
                    } else {
                        // If user scrolled a page back or forward, keep reusing the same channel view
                        s.channelView.requestUpdate();
                    }

                    // Show the channel view screen (hide the loading screen)
                    s.disp.setCurrent(s.channelView);
                    s.channelView.repaint();
                    break;
                }

                case EDIT_MESSAGE: {
                    JSONObject newMessage = new JSONObject();
                    newMessage.put("content", editContent);

                    String path = "/channels/" + s.selectedChannel.id + "/messages/" + editMessage.id + "/edit";
                    s.http.post(path, newMessage);

                    // Update displayed message content
                    editMessage.content = editContent;
                    editMessage.needUpdate = true;

                    s.disp.setCurrent(s.channelView);
                    s.channelView.requestUpdate();
                    s.channelView.repaint();
                    break;
                }

                case DELETE_MESSAGE: {
                    s.http.get("/channels/" + s.selectedChannel.id + "/messages/" + editMessage.id + "/delete");

                    // Update message to show as deleted
                    editMessage.content = "(deleted)";
                    editMessage.isStatus = true;
                    editMessage.needUpdate = true;

                    s.disp.setCurrent(s.channelView);
                    s.channelView.requestUpdate();
                    s.channelView.repaint();
                    break;
                }
            }
        }
        catch (Exception e) {
            s.error(e, prevScreen);
        }
    }
}
