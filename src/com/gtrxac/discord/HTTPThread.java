package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import javax.microedition.io.*;

/**
 * Class for all networking functionality: HTTP implementation, API functions
 */
public class HTTPThread extends Thread {
    static final int FETCH_GUILDS = 0;
    static final int FETCH_CHANNELS = 1;
    static final int FETCH_DM_CHANNELS = 2;
    static final int FETCH_MESSAGES = 3;
    static final int SEND_MESSAGE = 4;
    static final int EDIT_MESSAGE = 5;
    static final int DELETE_MESSAGE = 6;

    int action;

	// Parameters for SEND_MESSAGE
    // content is also used for EDIT_MESSAGE
    String content;
	String sendReference;  // ID of the message the user is replying to
	boolean sendPing;

    // Parameters for EDIT_MESSAGE and DELETE_MESSAGE
    Message editMessage;

    public HTTPThread(int action) {
        this.action = action;
        setPriority(Thread.MAX_PRIORITY);
    }

    public void run() {
        Displayable prevScreen = App.disp.getCurrent();
        
        // Create and show loading screen
        Form loadScr = new Form(null);
        loadScr.append(new StringItem(null, "Loading"));
        App.disp.setCurrent(loadScr);
        
        try {
            switch (action) {
                case FETCH_GUILDS: {
                    JSONArray guilds = JSONObject.parseArray(get("/users/@me/guilds"));
                    App.guilds = new Vector();

                    for (int i = 0; i < guilds.size(); i++) {
                        App.guilds.addElement(new DiscordObject(guilds.getArray(i)));
                    }
                    App.guildSelector = new GuildSelector(App.guilds, false);
                    App.disp.setCurrent(App.guildSelector);
                    break;
                }

                case FETCH_CHANNELS: {
                    String url = "/guilds/" + App.selectedGuild.id + "/channels" + (App.listTimestamps ? "?t=1" : "");

                    JSONArray channels = JSONObject.parseArray(get(url));
                    App.channels = new Vector();

                    for (int i = 0; i < channels.size(); i++) {
                        App.channels.addElement(new DiscordObject(channels.getArray(i)));
                    }
                    App.loadedGuild = App.selectedGuild;
                    App.channelSelector = new ChannelSelector();
                    App.disp.setCurrent(App.channelSelector);
                    break;
                }

                case FETCH_DM_CHANNELS: {
                    String url = "/users/@me/channels" + (App.listTimestamps ? "?t=1" : "");

                    JSONArray channels = JSONObject.parseArray(get(url));
                    App.channels = new Vector();
            
                    for (int i = 0; i < channels.size(); i++) {
                        App.channels.addElement(new DiscordObject(channels.getArray(i)));
                    }
                    App.loadedGuild = null;
                    App.selectedGuild = null;
                    App.channelSelector = new ChannelSelector();
                    App.disp.setCurrent(App.channelSelector);
                    break;
                }

                case SEND_MESSAGE: {
                    JSONObject json = new JSONObject();
                    json.put("content", content);
                    json.put("flags", 0);
                    json.put("mobile_network_type", "unknown");
                    json.put("tts", false);

                    // Reply
                    if (sendReference != null) {
                        JSONObject ref = new JSONObject();
                        ref.put("channel_id", App.selectedChannel.id);
                        if (!App.isDM) ref.put("guild_id", App.selectedGuild.id);
                        ref.put("message_id", sendReference);
                        json.put("message_reference", ref);

                        if (!sendPing && !App.isDM) {
                            JSONObject ping = new JSONObject();
                            ping.put("replied_user", false);
                            json.put("allowed_mentions", ping);
                        }
                    }

                    post("/channels/" + App.selectedChannel.id + "/messages", json);

                    // fall through (fetch messages because there might have 
                    // been other messages sent during the time the user was writing their message)
                }

                case FETCH_MESSAGES: {
                    StringBuffer url = new StringBuffer(
                        "/channels/" + App.selectedChannel.id + "/messages?limit=" + App.messageLoadCount + "&m=" + (App.markAsRead ? 1 : 0)
                    );

                    String fetchBefore = null;
                    String fetchAfter = null;
                    if (App.channelView != null) {
                        fetchBefore = App.channelView.before;
                        fetchAfter = App.channelView.after;
                    }

                    if (fetchBefore != null) url.append("&before=").append(fetchBefore);
                    if (fetchAfter != null) url.append("&after=").append(fetchAfter);

                    JSONArray messages = JSONObject.parseArray(get(url.toString()));
                    App.messages = new Vector();

                    for (int i = 0; i < messages.size(); i++) {
                        App.messages.addElement(new Message(messages.getArray(i)));
                    }

                    if ((fetchBefore == null && fetchAfter == null) || content != null) {
                        // If user opened a new channel or sent a message, create a new channel view
                        App.channelView = new ChannelView(false);
                    } else {
                        // If user scrolled a page back or forward, keep reusing the same channel view
                        App.channelView.requestUpdate();
                        App.channelView.repaint();
                    }

                    // Show the channel view screen (hide the loading screen)
                    App.disp.setCurrent(App.channelView);
                    break;
                }

                case EDIT_MESSAGE: {
                    JSONObject newMessage = new JSONObject();
                    newMessage.put("content", content);

                    String path = "/channels/" + App.selectedChannel.id + "/messages/" + editMessage.id + "/edit";
                    post(path, newMessage);

                    // Update displayed message content
                    editMessage.content = content;
                    editMessage.needUpdate = true;

                    App.disp.setCurrent(App.channelView);
                    App.channelView.requestUpdate();
                    App.channelView.repaint();
                    break;
                }

                case DELETE_MESSAGE: {
                    get("/channels/" + App.selectedChannel.id + "/messages/" + editMessage.id + "/delete");

                    // Update message to show as deleted
                    editMessage.content = "(deleted)";
                    editMessage.isStatus = true;
                    editMessage.needUpdate = true;

                    App.disp.setCurrent(App.channelView);
                    App.channelView.requestUpdate();
                    App.channelView.repaint();
                    break;
                }
            }
        }
        catch (Exception e) {
            App.error(e, prevScreen);
        }
    }

    private static HttpConnection openConnection(String url) throws IOException {
        String fullUrl = App.api + "/api/l" + url;

        char paramDelimiter = (fullUrl.indexOf("?") != -1) ? '&' : '?';
        fullUrl += paramDelimiter + "token=" + App.token;

        HttpConnection c = (HttpConnection) Connector.open(fullUrl);
        return c;
    }

    private static String sendRequest(HttpConnection c) throws Exception {
        InputStream is = null;

        try {
            int respCode = c.getResponseCode();
            is = c.openDataInputStream();
            
            // Read response
            StringBuffer stringBuffer = new StringBuffer();
            int ch;
            while ((ch = is.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            String response = stringBuffer.toString().trim();

            if (respCode == HttpConnection.HTTP_OK) {
                return response;
            }
            if (respCode == HttpConnection.HTTP_UNAUTHORIZED) {
                throw new Exception("Token is invalid or has expired");
            }

            try {
                String message = JSONObject.parseObject(response).getString("message");
                throw new Exception(message);
            }
            catch (RuntimeException e) {
                throw new Exception("HTTP error " + respCode);
            }
        } finally {
            if (is != null) is.close();
        }
    }

    private static String sendData(String method, String url, String data) throws Exception {
        HttpConnection c = null;
        OutputStream os = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(method);
            
            byte[] b;
            try {
                b = data.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                b = data.getBytes();
            }

            os = c.openOutputStream();
            os.write(b);

            return sendRequest(c);
        } finally {
            if (os != null) os.close();
            if (c != null) c.close();
        }
    }

    private static String get(String url) throws Exception {
        HttpConnection c = null;
        try {
            c = openConnection(url);
            c.setRequestMethod(HttpConnection.GET);
            return sendRequest(c);
        } finally {
            if (c != null) c.close();
        }
    }

    private static String post(String url, String data) throws Exception {
        return sendData(HttpConnection.POST, url, data);
    }
    private static String get(String url, String data) throws Exception {
        return sendData(HttpConnection.GET, url, data);
    }
    private static String post(String url, JSONObject data) throws Exception {
        return sendData(HttpConnection.POST, url, data.build());
    }
    private static String get(String url, JSONObject data) throws Exception {
        return sendData(HttpConnection.GET, url, data.build());
    }
}
