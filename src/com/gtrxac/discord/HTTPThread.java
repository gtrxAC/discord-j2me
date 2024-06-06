package com.gtrxac.discord;

import java.util.Vector;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class HTTPThread extends Thread {
    public static final int FETCH_GUILDS = 0;
    public static final int FETCH_CHANNELS = 1;
    public static final int FETCH_DM_CHANNELS = 2;
    public static final int FETCH_MESSAGES = 3;
    public static final int SEND_MESSAGE = 4;
    public static final int FETCH_ATTACHMENTS = 5;

    State s;
    int action;

    String fetchMsgsBefore;
    String fetchMsgsAfter;

    public HTTPThread(State s, int action) {
        this.s = s;
        this.action = action;
        setPriority(Thread.MAX_PRIORITY);
    }

    public void run() {
        Displayable prevScreen = s.disp.getCurrent();
        if (action != FETCH_ATTACHMENTS) s.disp.setCurrent(new LoadingScreen());
        
        try {
            switch (action) {
                case FETCH_GUILDS: {
                    JSONArray guilds = JSON.getArray(s.http.get("/users/@me/guilds"));
                    s.guilds = new Vector();

                    for (int i = 0; i < guilds.size(); i++) {
                        s.guilds.addElement(new Guild(guilds.getObject(i)));
                    }
                    s.guildSelector = new GuildSelector(s);
                    s.disp.setCurrent(s.guildSelector);
                    break;
                }

                case FETCH_CHANNELS: {
                    JSONArray channels = JSON.getArray(
                        s.http.get("/guilds/" + s.selectedGuild.id + "/channels")
                    );
                    s.channels = new Vector();

                    for (int i = 0; i < channels.size(); i++) {
                        for (int a = 0; a < channels.size(); a++) {
                            JSONObject ch = channels.getObject(a);
                            if (ch.getInt("position", i) != i) continue;

                            int type = ch.getInt("type", 0);
                            if (type != 0 && type != 5) continue;

                            s.channels.addElement(new Channel(ch));
                        }
                    }
                    s.channelSelector = new ChannelSelector(s);
                    s.disp.setCurrent(s.channelSelector);
                    break;
                }

                case FETCH_DM_CHANNELS: {
                    JSONArray channels = JSON.getArray(s.http.get("/users/@me/channels"));
                    s.dmChannels = new Vector();
            
                    for (int i = 0; i < channels.size(); i++) {
                        JSONObject ch = channels.getObject(i);
                        int type = ch.getInt("type", 1);
                        if (type != 1 && type != 3) continue;
            
                        s.dmChannels.addElement(new DMChannel(ch));
                    }
                    s.dmSelector = new DMSelector(s);
                    s.disp.setCurrent(s.dmSelector);
                    break;
                }

                case SEND_MESSAGE: {
                    String id;
                    if (s.isDM) id = s.selectedDmChannel.id;
                    else id = s.selectedChannel.id;

                    JSONObject json = new JSONObject();
                    json.put("content", s.sendMessage);
                    json.put("flags", 0);
                    json.put("mobile_network_type", "unknown");
                    json.put("tts", false);

                    // Reply
                    if (s.sendReference != null) {
                        JSONObject ref = new JSONObject();
                        ref.put("channel_id", s.isDM ? s.selectedDmChannel.id : s.selectedChannel.id);
                        if (!s.isDM) ref.put("guild_id", s.selectedGuild.id);
                        ref.put("message_id", s.sendReference);
                        json.put("message_reference", ref);

                        if (!s.sendPing && !s.isDM) {
                            JSONObject ping = new JSONObject();
                            ping.put("replied_user", false);
                            json.put("allowed_mentions", ping);
                        }
                    }

                    s.http.post("/channels/" + id + "/messages", json.build());
                    // fall through
                }

                case FETCH_MESSAGES: {
                    String id;
                    if (s.isDM) id = s.selectedDmChannel.id;
                    else id = s.selectedChannel.id;
                    
                    StringBuffer url = new StringBuffer(
                        "/channels/" + id + "/messages?limit=" + s.messageLoadCount
                    );
                    if (fetchMsgsBefore != null) url.append("&before=" + fetchMsgsBefore);
                    if (fetchMsgsAfter != null) url.append("&after=" + fetchMsgsAfter);

                    JSONArray messages = JSON.getArray(s.http.get(url.toString()));
                    s.messages = new Vector();

                    for (int i = 0; i < messages.size(); i++) {
                        s.messages.addElement(new Message(s, messages.getObject(i)));
                    }

                    if ((fetchMsgsBefore == null && fetchMsgsAfter == null) || s.sendMessage != null) {
                        // If user opened a new channel or sent a message, create a new channel view
                        if (s.oldUI) s.oldChannelView = new OldChannelView(s);
                        else s.channelView = new ChannelView(s);
                    } else {
                        // If user scrolled a page back or forward, keep reusing the same channel view
                        if (s.oldUI) s.oldChannelView.update();
                        else s.channelView.update(false);
                    }

                    // Show the channel view screen (hide the loading screen)
                    if (s.oldUI) {
                        s.disp.setCurrent(s.oldChannelView);
                    } else {
                        s.disp.setCurrent(s.channelView);
                        s.channelView.repaint();
                    }

                    s.sendMessage = null;
                    s.typingUsers = new Vector();
                    s.typingUserIDs = new Vector();
                    break;
                }

                case FETCH_ATTACHMENTS: {
                    if (s.cdn == null || s.cdn.length() == 0) {
                        throw new Exception("CDN URL has not been defined. Attachments are not available.");
                    }

                    Vector attachments = s.attachmentView.msg.attachments;

                    for (int i = 0; i < attachments.size(); i++) {
                        Attachment attach = (Attachment) attachments.elementAt(i);

                        try {
                            Image image = s.http.getImage(attach.url);
                            ImageItem item = new ImageItem(null, image, Item.LAYOUT_DEFAULT, null);
                            s.attachmentView.append(item);
                        }
                        catch (Exception e) {
                            s.attachmentView.append(new StringItem(null, e.toString()));
                        }
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            s.error(e.toString(), prevScreen);
        }
    }
}
