package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import javax.microedition.io.*;

public class HTTPThread extends Thread {
    private static final String BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/+";

    static final int FETCH_GUILDS = 0;
    static final int FETCH_CHANNELS = 1;
    static final int FETCH_DM_CHANNELS = 2;
    static final int FETCH_MESSAGES = 3;
    static final int SEND_MESSAGE = 4;
    static final int EDIT_MESSAGE = 5;
    static final int DELETE_MESSAGE = 6;

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
            // If not done so already for this session, create a short hash of the currently logged in user's ID
            // (the actual full user ID is base64-encoded inside the token)
            // Equivalent short hashes are sent as part of message data (see proxy script) to determine which messages are owned by who (i.e. which messages were sent by us and can thus be edited/deleted)
            if (App.myUserId == null) {
                StringBuffer id = new StringBuffer();
                int b64idLength = App.token.indexOf('.');
                if (b64idLength == -1) {
                    throw new Exception("Token is written incorrectly");
                }
                String b64id = App.token.substring(0, b64idLength);
                int b64idChunkLength = (b64idLength + 3)/4*4;

                for (int i = 0; i < b64idChunkLength; i += 4) {
                    int first6 = BASE64_ALPHABET.indexOf(b64id.charAt(i));
                    int second6 = BASE64_ALPHABET.indexOf(b64id.charAt(i + 1));
                    int third6 = 0;
                    int fourth6 = 0;

                    int first8 = (first6 << 2) | (second6 >> 4);
                    id.append(first8 - 48);  // <- note! not a general-purpose base64 decoder! this only works for b64-encoded numeric IDs

                    if (i + 2 < b64idLength) {
                        third6 = BASE64_ALPHABET.indexOf(b64id.charAt(i + 2));
                        int second8 = ((second6 & 0xF) << 4) | (third6 >> 2);
                        id.append(second8 - 48);
                    }
                    if (i + 3 < b64idLength) {
                        fourth6 = BASE64_ALPHABET.indexOf(b64id.charAt(i + 3));
                        int third8 = ((third6 & 0x3) << 6) | fourth6;
                        id.append(third8 - 48);
                    }
                }
                try {
                    App.myUserId = Long.toString(Long.parseLong(id.toString())%100000, 36);
                }
                catch (Exception e) {
                    throw new Exception("Token is written incorrectly");
                }
            }

            switch (action) {
                case FETCH_GUILDS: {
                    JSONArray guilds = JSON.getArray(get("/users/@me/guilds"));
                    App.guilds = new Vector();

                    for (int i = 0; i < guilds.size(); i++) {
                        App.guilds.addElement(new DiscordObject(guilds.getArray(i)));
                    }
                    App.guildSelector = new GuildSelector(App.guilds, false);
                    App.disp.setCurrent(App.guildSelector);
                    break;
                }

                case FETCH_CHANNELS: {
                    JSONArray channels = JSON.getArray(get("/guilds/" + App.selectedGuild.id + "/channels"));
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
                    JSONArray channels = JSON.getArray(get("/users/@me/channels"));
                    App.channels = new Vector();
            
                    for (int i = 0; i < channels.size(); i++) {
                        App.channels.addElement(new DiscordObject(channels.getArray(i)));
                    }
                    App.loadedGuild = null;
                    App.channelSelector = new ChannelSelector();
                    App.disp.setCurrent(App.channelSelector);
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
                        "/channels/" + App.selectedChannel.id + "/messages?limit=" + App.messageLoadCount
                    );
                    if (fetchMsgsBefore != null) url.append("&before=" + fetchMsgsBefore);
                    if (fetchMsgsAfter != null) url.append("&after=" + fetchMsgsAfter);

                    JSONArray messages = JSON.getArray(get(url.toString()));
                    App.messages = new Vector();

                    for (int i = 0; i < messages.size(); i++) {
                        App.messages.addElement(new Message(messages.getArray(i)));
                    }

                    if ((fetchMsgsBefore == null && fetchMsgsAfter == null) || sendMessage != null) {
                        // If user opened a new channel or sent a message, create a new channel view
                        App.channelView = new ChannelView(false);
                    } else {
                        // If user scrolled a page back or forward, keep reusing the same channel view
                        App.channelView.requestUpdate();
                    }

                    // Show the channel view screen (hide the loading screen)
                    App.disp.setCurrent(App.channelView);
                    App.channelView.repaint();
                    break;
                }

                case EDIT_MESSAGE: {
                    JSONObject newMessage = new JSONObject();
                    newMessage.put("content", editContent);

                    String path = "/channels/" + App.selectedChannel.id + "/messages/" + editMessage.id + "/edit";
                    post(path, newMessage);

                    // Update displayed message content
                    editMessage.content = editContent;
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
                String message = JSON.getObject(response).getString("message");
                throw new Exception(message);
            }
            catch (JSONException e) {
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
