package com.gtrxac.discord;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class HTTPThread extends Thread {
    static final int FETCH_GUILDS = 0;
    static final int FETCH_CHANNELS = 1;
    static final int FETCH_DM_CHANNELS = 2;
    static final int FETCH_MESSAGES = 3;
    static final int SEND_MESSAGE = 4;
    static final int FETCH_ATTACHMENTS = 5;
    static final int FETCH_ICON = 6;
    static final int SEND_ATTACHMENT = 7;
    static final int VIEW_ATTACHMENT_TEXT = 8;
    static final int EDIT_MESSAGE = 9;
    static final int DELETE_MESSAGE = 10;

    private static final String BOUNDARY = "----WebKitFormBoundary7MA4YWykTrZu0gW";
    private static final String LINE_FEED = "\r\n";

    State s;
    int action;

    // Parameters for FETCH_MESSAGES
    String fetchMsgsBefore;
    String fetchMsgsAfter;

    // Parameters for FETCH_ICON
    HasIcon iconTarget;  // item (guild or DM channel) that this icon should be assigned to

    // Parameters for SEND_ATTACHMENT
    String attachPath;
    String attachName;

    // Parameters for VIEW_ATTACHMENT_TEXT
    Attachment viewAttach;

    // Parameters for EDIT_MESSAGE
    Message editMessage;
    String editContent;

    public HTTPThread(State s, int action) {
        this.s = s;
        this.action = action;
        setPriority(Thread.MAX_PRIORITY);
    }

    private byte[] createFormPart(String name, String filename) {
        StringBuffer r = new StringBuffer();

        r.append("--").append(BOUNDARY).append(LINE_FEED);
        r.append("Content-Disposition: form-data; name=\"").append(name).append("\"");
        if (filename != null) {
            r.append("; filename=\"").append(filename).append("\"");
        }
        r.append(LINE_FEED);
        if (filename != null) {
            r.append("Content-Type: application/octet-stream").append(LINE_FEED);
        }
        r.append(LINE_FEED);

        return r.toString().getBytes();
    }

    private boolean shouldShowLoadScreen() {
        return !s.dontShowLoadScreen
            && action != FETCH_ATTACHMENTS
            && action != FETCH_ICON
            && action != SEND_MESSAGE
            && action != EDIT_MESSAGE
            && action != DELETE_MESSAGE;
    }

    public void run() {
        boolean showLoad = shouldShowLoadScreen();
        s.dontShowLoadScreen = false;

        Displayable prevScreen = s.disp.getCurrent();
        if (showLoad) s.disp.setCurrent(new LoadingScreen(s));

        if (s.myUserId == null) {
            try {
                JSONObject resp = JSON.getObject(s.http.get("/users/@me"));
                s.myUserId = resp.getString("id", "");
                s.isLiteProxy = resp.getBoolean("_liteproxy", false);
            }
            catch (Exception e) {
                s.error(e);
                s.myUserId = "";
            }
        }
        
        try {
            switch (action) {
                case FETCH_GUILDS: {
                    JSONArray guilds = JSON.getArray(s.http.get("/users/@me/guilds"));
                    s.guilds = new Vector();

                    for (int i = 0; i < guilds.size(); i++) {
                        s.guilds.addElement(new Guild(s, guilds.getObject(i)));
                    }
                    s.guildSelector = new GuildSelector(s);
                    s.disp.setCurrent(s.guildSelector);
                    break;
                }

                case FETCH_CHANNELS: {
                    // Fetch role data (role colors) for this server if needed
                    if (s.gatewayActive() && s.selectedGuild.roles == null && s.useNameColors) {
                        String roleData = s.http.get("/guilds/" + s.selectedGuild.id + "/roles");
                        JSONArray roleArr = JSON.getArray(roleData);

                        s.selectedGuild.roles = new Vector();

                        if (s.isLiteProxy) {
                            // Sorted server-side: load roles as-is
                            for (int i = roleArr.size() - 1; i >= 0; i--) {
                                JSONObject data = roleArr.getObject(i);
                                
                                if (data.getInt("color") == 0) continue;
                                
                                s.selectedGuild.roles.addElement(new Role(data));
                            }
                        } else {
                            // Not sorted server-side: manually sort based on 'position' field
                            for (int i = roleArr.size() - 1; i >= 0; i--) {
                                for (int a = roleArr.size() - 1; a >= 0; a--) {
                                    JSONObject data = roleArr.getObject(a);

                                    if (data.getInt("position", i) != i) continue;
                                    if (data.getInt("color") == 0) continue;

                                    s.selectedGuild.roles.addElement(new Role(data));
                                }
                            }
                        }
                    }

                    s.selectedGuild.channels = Channel.parseChannels(
                        JSON.getArray(s.http.get("/guilds/" + s.selectedGuild.id + "/channels"))
                    );

                    s.channels = s.selectedGuild.channels;
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
            
                        s.dmChannels.addElement(new DMChannel(s, ch));
                    }
                    s.dmSelector = new DMSelector(s);
                    s.disp.setCurrent(s.dmSelector);
                    break;
                }

                case SEND_MESSAGE: {
                    if (!showLoad && s.channelView != null) {
                        s.disp.setCurrent(s.channelView);
                        s.channelView.bannerText = "Sending message";
                        s.channelView.repaint();
                    }

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

                    s.http.post("/channels/" + id + "/messages", json);

                    // If gateway enabled, don't need to fetch new messages
                    if (s.gatewayActive()) {
                        if (!showLoad && s.channelView != null) {
                            s.channelView.bannerText = null;
                            s.channelView.repaint();
                        }
                        break;
                    }

                    // fall through (if gateway disabled, fetch messages because there might have 
                    // been other messages sent during the time the user was writing their message)
                }

                case FETCH_MESSAGES: {
                    if (!showLoad && s.channelView != null) {
                        s.channelView.bannerText = "Loading messages";
                        s.channelView.repaint();
                    }

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
                        else s.channelView.requestUpdate(false);
                    }

                    // Show the channel view screen (hide the loading screen)
                    if (s.oldUI) {
                        s.disp.setCurrent(s.oldChannelView);
                    } else {
                        s.channelView.bannerText = null;
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
                    int layout = Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE;

                    for (int i = 0; i < attachments.size(); i++) {
                        Attachment attach = (Attachment) attachments.elementAt(i);

                        StringItem titleItem = new StringItem(null, attach.name + " (" + attach.size + ")");
                        s.attachmentView.append(titleItem);

                        if (attach.supported) {
                            // Supported attachment (image/video) -> show it
                            // For videos, only the first frame is shown (Discord media proxy converts to image)
                            try {
                                Image image = s.http.getImage(attach.previewUrl);
                                ImageItem item = new ImageItem(null, image, Item.LAYOUT_DEFAULT, null);
                                item.setLayout(layout);
                                s.attachmentView.append(item);
                            }
                            catch (Exception e) {
                                StringItem item = new StringItem(null, e.toString());
                                item.setLayout(layout);
                                s.attachmentView.append(item);
                            }
                        } else {
                            if (attach.isText) {
                                // Unsupported -> show a button to view it as text
                                // Note: showCommand has a priority starting at 100, so when it's pressed, 
                                //       we can distinguish it from 'open in browser' buttons
                                Command showCommand = new Command("Show", Command.ITEM, i + 100);
                                StringItem showButton = new StringItem(null, "Show as text", Item.BUTTON);
                                showButton.setLayout(layout);
                                showButton.setDefaultCommand(showCommand);
                                showButton.setItemCommandListener(s.attachmentView);
                                s.attachmentView.append(showButton);
                            }
                        }

                        Command openCommand = new Command("Open", Command.ITEM, i);
                        StringItem openButton = new StringItem(null, "Open in browser", Item.BUTTON);
                        openButton.setLayout(layout);
                        openButton.setDefaultCommand(openCommand);
                        openButton.setItemCommandListener(s.attachmentView);
                        s.attachmentView.append(openButton);

                        Spacer sp = new Spacer(s.attachmentView.getWidth(), s.attachmentView.getHeight()/10);
                        s.attachmentView.append(sp);
                    }
                    break;
                }

                case FETCH_ICON: {
                    if (s.cdn == null || s.cdn.length() == 0) {
                        throw new Exception("CDN URL has not been defined. Server icons and user avatars are not available.");
                    }

                    String format = (s.useJpeg ? "jpg" : "png");
                    String size = (s.iconSize == 1) ? "16" : "32";
                    String type = iconTarget.getIconType();
                    String id = iconTarget.getIconID();
                    String hash = iconTarget.getIconHash();
                    Image icon = s.http.getImage(s.cdn + type + id + "/" + hash + "." + format + "?size=" + size);

                    s.iconCache.set(hash, icon);
                    iconTarget.iconLoaded(s);
                    break;
                }

                case SEND_ATTACHMENT: {
                    Displayable screen = s.disp.getCurrent();
                    if (screen instanceof LoadingScreen) {
                        ((LoadingScreen) screen).text = "Sending";
                    }

                    HttpConnection httpConn = null;
                    DataOutputStream os = null;

                    try {
                        String id = s.isDM ? s.selectedDmChannel.id : s.selectedChannel.id;
                        httpConn = s.http.openConnection("/channels/" + id + "/upload");
                        httpConn.setRequestMethod(HttpConnection.POST);
                        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

                        os = httpConn.openDataOutputStream();

                        os.write(createFormPart("token", null));
                        os.write(s.http.token.getBytes());
                        os.write(LINE_FEED.getBytes());

                        os.write(createFormPart("content", null));
                        os.write(LINE_FEED.getBytes());

                        os.write(createFormPart("files", attachName));

                        FileConnection fc = (FileConnection) Connector.open(attachPath, Connector.READ);
                        InputStream fileInputStream = fc.openInputStream();

                        byte[] buffer = new byte[1024];
                        int bytesRead = -1;

                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }

                        os.write(LINE_FEED.getBytes());
                        os.write(("--" + BOUNDARY + "--" + LINE_FEED).getBytes());
                        os.flush();

                        fileInputStream.close();
                        fc.close();
                        
                        s.http.sendRequest(httpConn);
                        new HTTPThread(s, FETCH_MESSAGES).start();
                    }
                    catch (Exception e) {
                        s.error("Error while sending file: " + e.toString());
                    }
                    finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                            if (httpConn != null) {
                                httpConn.close();
                            }
                        } catch (Exception ex) {}
                    }
                    break;
                }

                case VIEW_ATTACHMENT_TEXT: {
                    String text;
                    byte[] textBytes = s.http.getBytes(viewAttach.browserUrl);
                    try {
                        text = new String(textBytes, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        text = new String(textBytes);
                    }
                    MessageCopyBox copyBox = new MessageCopyBox(s, viewAttach.name, text);
                    copyBox.lastScreen = s.attachmentView;
                    s.disp.setCurrent(copyBox);
                    break;
                }

                case EDIT_MESSAGE: {
                    if (!showLoad && s.channelView != null) {
                        s.channelView.bannerText = "Editing message";
                        s.channelView.repaint();
                    }
                    
                    JSONObject newMessage = new JSONObject();
                    newMessage.put("content", editContent);

                    String channelId = s.isDM ? s.selectedDmChannel.id : s.selectedChannel.id;
                    String path = "/channels/" + channelId + "/messages/" + editMessage.id + "/edit";
                    s.http.post(path, newMessage);

                    // Manually update message content if gateway disabled
                    // (if enabled, new message content will come through gateway event)
                    if (!s.gatewayActive()) {
                        editMessage.content = editContent;
                        editMessage.rawContent = editContent;
                        editMessage.needUpdate = true;

                        if (s.oldUI) {
                            s.oldChannelView.update();
                        } else {
                            s.channelView.requestUpdate(false);
                        }
                    }

                    if (!showLoad && s.channelView != null) {
                        s.channelView.bannerText = null;
                        s.channelView.repaint();
                    }
                    break;
                }

                case DELETE_MESSAGE: {
                    if (!showLoad && s.channelView != null) {
                        s.channelView.bannerText = "Deleting message";
                        s.channelView.repaint();
                    }

                    String channelId = s.isDM ? s.selectedDmChannel.id : s.selectedChannel.id;

                    s.http.get("/channels/" + channelId + "/messages/" + editMessage.id + "/delete");

                    // Manually update message to be deleted if gateway disabled
                    // (if enabled, deletion event will come through gateway)
                    if (!s.gatewayActive()) {
                        editMessage.delete();

                        if (s.oldUI) {
                            s.oldChannelView.update();
                        } else {
                            s.channelView.requestUpdate(false);
                        }
                    }

                    if (!showLoad && s.channelView != null) {
                        s.channelView.bannerText = null;
                        s.channelView.repaint();
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            if (action == FETCH_ICON) {
                s.iconCache.removeRequest(iconTarget.getIconHash());
            } else {
                s.error(e, prevScreen);
            }
        }
    }
}
