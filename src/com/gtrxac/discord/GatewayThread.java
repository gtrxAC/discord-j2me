package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;

// ifdef PIGLER_SUPPORT
import org.pigler.tester.*;
// endif

// ifdef NOKIA_UI_SUPPORT
import com.nokia.mid.ui.*;
// endif

import cc.nnproject.json.*;

public class GatewayThread extends Thread implements Strings
// ifdef PIGLER_SUPPORT
, PiglerAPIHandlerLayer
// endif
{
    private State s;

    volatile boolean stop;
    volatile String stopMessage;

    private HeartbeatThread hbThread;

    private SocketConnection sc;
    private InputStream is;
    private OutputStream os;

    private static int reconnectAttempts;

    // ifdef PIGLER_SUPPORT
    private static Image appIcon;
    private static PiglerAPILayer pigler;
    private static boolean piglerInitFailed;
    private static final Object piglerLock = new Object();

    /**
     * Pigler notification UID -> Notification object
     */
    public static Hashtable piglerNotifs;
    // endif

    public GatewayThread(State s) {
        this.s = s;
        s.subscribedGuilds = new Vector();
        reconnectAttempts++;
    }

    private void disconnect() {
        if (hbThread != null) hbThread.stop = true;
        try { is.close(); } catch (Exception e) {}
        try { os.close(); } catch (Exception e) {}
        try { sc.close(); } catch (Exception e) {}
    }

    public void disconnected(String message) {
        disconnect();
        if (s.autoReConnect && reconnectAttempts < 3) {
            if (s.channelView != null) {
                s.channelView.bannerText = Locale.get(CHANNEL_VIEW_RECONNECTING);
                s.channelView.repaint();
            }
            s.gateway = new GatewayThread(s);
            s.gateway.start();
        } else {
            s.disp.setCurrent(new ReconnectDialog(s, message));
        }
    }

    public void send(JSONObject msg) {
        try {
            os.write((msg.build() + "\n").getBytes("UTF-8"));
            os.flush();
        }
        catch (Exception e) {}
    }

    private boolean shouldNotify(JSONObject msgData) {
        if (s.showNotifsAll) return true;

        // Check if this message is in a DM
        String guildID = msgData.getString("guild_id", null);
        if (guildID == null) return s.showNotifsDMs;

        // Check if this message pings us (note: only checks user pings, not role pings)
        if (!s.showNotifsPings) return false;
        
        JSONArray pings = msgData.getArray("mentions");

        for (int i = 0; i < pings.size(); i++) {
            if (pings.getObject(i).getString("id").equals(s.myUserId)) {
                return true;
            }
        }
        return false;
    }

    private void handleNotification(JSONObject msgData) {
        Message msg = new Message(s, msgData);

        // Display name of person who sent the message.
        String author = msg.author.name;

        // Name of the server and channel where the notification occurred.
        // "(unknown)" if server list not loaded.
        // "Server Name" if server list loaded, but channel list for that server not loaded.
        // "Server Name #channel" if server and channel lists loaded.
        // null if notification occurred in a DM
        String location = null;

        // Message content, limited to 50 characters, with attachments parsed into text, e.g. "Text content (3 attachments)"
        String content = null;

        // true if message sent in a direct message or DM group, false if sent in a server
        boolean isDM = false;

        String guildID = msgData.getString("guild_id", null);
        String channelID = msgData.getString("channel_id", null);
        
        if (guildID == null) {
            isDM = true;
        } else {
            // Get the name of the server where the message was sent
            // (only available if server list has been loaded)
            Guild g = Guild.getById(s, guildID);
            location = (g != null) ? g.name : Locale.get(NAME_UNKNOWN);

            // Get the name of the channel
            // (only available if channel list for that server has been loaded)
            Channel c = Channel.getByID(s, channelID);
            if (c != null) location += " #" + c.name;
        }

        StringBuffer c = new StringBuffer();
        c.append(Util.stringToLength(msg.content, 50));

        if (msg.attachments != null) {
            if (msg.content.length() != 0) c.append(" ");
            c.append(Locale.get(NOTIFICATION_ATTACHMENT_PREFIX));
            c.append(msg.attachments.size());

            if (msg.attachments.size() != 1) {
                c.append(Locale.get(NOTIFICATION_ATTACHMENTS_SUFFIX));
            } else {
                c.append(Locale.get(NOTIFICATION_ATTACHMENT_SUFFIX));
            }
        }
        content = c.toString();

        Notification notif = new Notification(guildID, channelID);

        if (s.showNotifAlert) {
            s.disp.setCurrent(new NotificationDialog(s, notif, location, msg));
        }
        
        // ifdef PIGLER_SUPPORT
        synchronized (piglerLock) {
            if (s.showNotifPigler && pigler != null) {
                try {
                    int uid;
                    if (isDM) {
                        uid = pigler.createNotification(author, content, appIcon, true);
                    } else {
                        uid = pigler.createNotification(location, author + ": " + content, appIcon, true);
                    }
                    piglerNotifs.put(new Integer(uid), notif);
                }
                catch (Exception e) {}
            }
        }
        // endif

        // ifdef NOKIA_UI_SUPPORT
        if (s.showNotifNokiaUI && Util.supportsNokiaUINotifs) {
            try {
                SoftNotification sn = SoftNotification.newInstance();
                sn.setText(
                    NotificationDialog.createString(notif, location, msg),
                    (isDM ? author : location) + ": " + content
                );
                sn.post();
            }
            catch (Throwable e) {}
        }
        // endif
    }

    // ifdef PIGLER_SUPPORT
    public void checkInitPigler() {
        synchronized (piglerLock) {
            if (s.showNotifPigler) {
                if (pigler == null && !piglerInitFailed) {
                    initPigler();
                }
            } else {
                appIcon = null;
                pigler = null;
                piglerInitFailed = false;
                piglerNotifs = null;
            }
        }
    }

    private void initPigler() {
		if (!Util.supportsPigler) {
            s.error(Locale.get(PIGLER_NOT_SUPPORTED));
            piglerInitFailed = true;
			return;
		}

        try {
            appIcon = Image.createImage("/icon.png");
        }
        catch (Exception e) {}
        
		try {
            synchronized (piglerLock) {
                piglerNotifs = new Hashtable();
                pigler = new PiglerAPILayer();
                pigler.setListener(this);
                int missedUid = pigler.init("Discord");
                if (missedUid != 0) handleNotificationTap(missedUid);
            }
		} catch (Exception e) {
			e.printStackTrace();
            s.error(Locale.get(PIGLER_ERROR) + e.toString());
		}
    }
    
    public void handleNotificationTap(int uid) {
        Integer uidObject = new Integer(uid);
        Notification notif = (Notification) piglerNotifs.get(uidObject);
        if (notif == null) return;

        piglerNotifs.remove(uidObject);
        notif.view(s);
    }
    // endif

    public void run() {
        try {
            // ifdef PIGLER_SUPPORT
            checkInitPigler();
            // endif

            sc = (SocketConnection) Connector.open(s.getPlatformSpecificUrl(s.gatewayUrl));

            // Not supported on JBlend (e.g. some Samsungs)
            try {
                sc.setSocketOption(SocketConnection.KEEPALIVE, 1);
            }
            catch (Exception e) {}

            is = sc.openInputStream();
            os = sc.openOutputStream();
            
            StringBuffer sb = new StringBuffer();
            String msgStr;

            while (true) {
                // Get message
                while (true) {
                    if (stop) {
                        if (stopMessage != null) disconnected(stopMessage);
                        else disconnect();
                        return;
                    }

                    int ch = is.read();
                    if (ch == '\n' || ch == -1) {
                        if (sb.length() > 0) {
                            // This message has been fully received, start processing it
                            msgStr = new String(sb.toString().getBytes("ISO-8859-1"), "UTF-8");
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
                        events.add("J2ME_MESSAGE_CREATE");
                        events.add("MESSAGE_DELETE");
                        events.add("MESSAGE_UPDATE");
                        events.add("TYPING_START");
                        events.add("GUILD_MEMBERS_CHUNK");
                        events.add("J2ME_READY");

                        JSONObject connData = new JSONObject();
                        connData.put("supported_events", events);
                        connData.put("url", "wss://gateway.discord.gg/?v=9&encoding=json");

                        JSONObject connMsg = new JSONObject();
                        connMsg.put("op", -1);
                        connMsg.put("t", "GATEWAY_CONNECT");
                        connMsg.put("d", connData);
                        send(connMsg);

                        // Remove "Reconnecting" banner message if auto reconnected
                        if (s.channelView != null && Locale.get(CHANNEL_VIEW_RECONNECTING).equals(s.channelView.bannerText)) {
                            s.channelView.bannerText = null;
                            s.channelView.repaint();
                        }
                        reconnectAttempts = 0;
                    }
                    else if (op.equals("GATEWAY_DISCONNECT")) {
                        String reason = message.getObject("d").getString("message");
                        disconnected(reason);
                        return;
                    }
                    else if (op.equals("J2ME_MESSAGE_CREATE")) {
                        JSONObject msgData = message.getObject("d");
                        String msgId = msgData.getString("id");
                        String chId = msgData.getString("channel_id");
                        String authorID = msgData.getObject("author").getString("id");

                        // Mark this channel as unread if it's not the currently opened channel
                        if (
                            !s.channelIsOpen
                            || (s.isDM && !chId.equals(s.selectedDmChannel.id))
                            || (!s.isDM && !chId.equals(s.selectedChannel.id))
                        ) {
                            // Don't set unread indicator if message was sent by the logged in user
                            if (authorID.equals(s.myUserId)) continue;

                            if (shouldNotify(msgData)) {
                                if (s.playNotifSound) AlertType.ALARM.playSound(s.disp);
                                if (
                                    s.showNotifAlert
                                    // ifdef PIGLER_SUPPORT
                                    || s.showNotifPigler
                                    // endif
                                ) handleNotification(msgData);
                            }

                            Channel ch = Channel.getByID(s, chId);
                            if (ch != null) {
                                ch.lastMessageID = Long.parseLong(msgId);
                                s.updateUnreadIndicators(false, chId);
                                continue;
                            }
                            DMChannel dmCh = DMChannel.getById(s, chId);
                            if (dmCh != null) {
                                dmCh.lastMessageID = Long.parseLong(msgId);
                                s.updateUnreadIndicators(true, chId);
                            }
                            continue;
                        }
                        
                        // If message was sent in the currently opened channel, update the channel view accordingly:

                        // If the message is already shown, don't show it again (check for duplicate ID)
                        boolean skip = false;
                        for (int i = 0; i < s.messages.size(); i++) {
                            Message m = (Message) s.messages.elementAt(i);
                            if (m.id.equals(msgId)) {
                                skip = true;
                                break;
                            }
                        }
                        if (skip) continue;

                        // If we're on the newest page, make the new message visible
                        if (s.channelView.page == 0 && !s.channelView.outdated) {
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
                            for (int i = 0; i < s.typingUsers.size(); i++) {
                                if (s.typingUserIDs.elementAt(i).equals(authorID)) {
                                    s.typingUsers.removeElementAt(i);
                                    s.typingUserIDs.removeElementAt(i);
                                }
                            }
                        }

                        // Redraw the message list and mark it as read
                        if (s.channelView.page == 0) {
                            s.channelView.requestUpdate(true, true);
                            s.unreads.autoSave = false;
                            s.unreads.markRead(chId, Long.parseLong(msgId));
                            s.unreads.autoSave = true;
                        } else {
                            // If user is not on the newest page of messages, ask them to refresh
                            // There is no easy way to do it any other way without breaking pagination
                            s.channelView.outdated = true;
                        }

                        s.channelView.repaint();
                        s.channelView.serviceRepaints();
                    }
                    else if (op.equals("MESSAGE_DELETE")) {
                        if (s.channelView == null) continue;

                        JSONObject msgData = message.getObject("d");

                        String channel = msgData.getString("channel_id", "");
                        String selected = s.isDM ? s.selectedDmChannel.id : s.selectedChannel.id;
                        if (!channel.equals(selected)) continue;

                        String messageId = msgData.getString("id");

                        for (int i = 0; i < s.messages.size(); i++) {
                            Message msg = (Message) s.messages.elementAt(i);
                            if (!msg.id.equals(messageId)) continue;

                            msg.delete();

                            s.channelView.requestUpdate(true, false);
                            s.channelView.repaint();
                            s.channelView.serviceRepaints();
                            break;
                        }
                    }
                    else if (op.equals("MESSAGE_UPDATE")) {
                        if (s.channelView == null) continue;

                        JSONObject msgData = message.getObject("d");

                        // Check if content was changed (other parts of the message can change too,
                        // but currently we can only update the content)
                        String newContent = msgData.getString("content", null);
                        if (newContent == null) continue;

                        String channel = msgData.getString("channel_id", "");
                        String selected = s.isDM ? s.selectedDmChannel.id : s.selectedChannel.id;
                        if (!channel.equals(selected)) continue;

                        String messageId = msgData.getString("id");

                        for (int i = 0; i < s.messages.size(); i++) {
                            Message msg = (Message) s.messages.elementAt(i);
                            if (!msg.id.equals(messageId)) continue;

                            msg.content = newContent;
                            msg.needUpdate = true;

                            s.channelView.requestUpdate(true, false);
                            s.channelView.repaint();
                            s.channelView.serviceRepaints();
                            break;
                        }
                    }
                    else if (op.equals("TYPING_START")) {
                        if (s.channelView == null) continue;

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
                                
                                String author = userObj.getString("global_name", null);
                                if (author == null) {
                                    author = userObj.getString("username", Locale.get(NAME_UNKNOWN));
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

                        s.channelView.repaint();
                    }
                    else if (op.equals("GUILD_MEMBERS_CHUNK")) {
                        if (s.channelView == null || s.selectedGuild == null) continue;

                        JSONObject data = message.getObject("d");
                        JSONArray members = data.getArray("members");

                        if (s.disp.getCurrent() instanceof MentionForm) {
                            // Guild member request was for inserting a mention
                            ((MentionForm) s.disp.getCurrent()).searchCallback(members);
                        } else {
                            // Guild member request was for role data (name colors)
                            String guildId = data.getString("guild_id");
                            JSONArray notFound = data.getArray("not_found");

                            for (int i = 0; i < notFound.size(); i++) {
                                String id = notFound.getString(i);
                                s.nameColorCache.set(id + guildId, 0);
                            }

                            for (int i = 0; i < members.size(); i++) {
                                int resultColor = 0;

                                JSONObject member = members.getObject(i);
                                JSONArray memberRoles = member.getArray("roles");

                                for (int r = 0; r < s.selectedGuild.roles.size(); r++) {
                                    Role role = (Role) s.selectedGuild.roles.elementAt(r);
                                    if (memberRoles.indexOf(role.id) == -1) continue;

                                    resultColor = role.color;
                                    break;
                                }

                                String id = member.getObject("user").getString("id");
                                s.nameColorCache.set(id + guildId, resultColor);
                            }
                            s.nameColorCache.activeRequest = false;
                        }
                    }
                    else if (op.equals("J2ME_READY")) {
                        if (s.myUserId == null) {
                            s.myUserId = message.getObject("d").getString("id");
                        }
                    }
                }
                else if (message.getInt("op", 0) == 10) {
                    int heartbeatInterval = message.getObject("d").getInt("heartbeat_interval");
                    hbThread = new HeartbeatThread(this, heartbeatInterval);
                    hbThread.start();

                    // Identify
                    JSONObject idProps = new JSONObject();
                    idProps.put("os", "Linux");
                    idProps.put("browser", "Firefox");
                    idProps.put("device", "");
            
                    JSONObject idData = new JSONObject();
                    idData.put("token", s.token);
                    idData.put("capabilities", 30717);
                    idData.put("properties", idProps);
            
                    JSONObject idMsg = new JSONObject();
                    idMsg.put("op", 2);
                    idMsg.put("d", idData);
                    send(idMsg);
                }
            }
        }
        catch (Exception e) {
            disconnected(e.toString());
        }
    }
}