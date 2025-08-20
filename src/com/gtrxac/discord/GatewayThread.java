package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.rms.*;

//#ifdef PIGLER_SUPPORT
import org.pigler.tester.*;
//#endif

//#ifdef NOKIA_UI_SUPPORT
import com.nokia.mid.ui.*;
//#endif

import cc.nnproject.json.*;

public class GatewayThread extends Thread implements Strings
//#ifdef PIGLER_SUPPORT
, PiglerAPIHandlerLayer
//#endif
{
	volatile boolean stop;
	volatile String stopMessage;

//#ifdef OVER_100KB
	private HeartbeatThread hbThread;
//#else
	private Threads100kb hbThread;
//#endif

	private SocketConnection sc;
	private InputStream is;
	private OutputStream os;

	private static int reconnectAttempts;

//#ifdef PIGLER_SUPPORT
	private static Image appIcon;
	private static PiglerAPILayer pigler;
	private static boolean piglerInitFailed;
	private static final Object piglerLock = new Object();

	/**
	 * Pigler notification UID -> Notification object
	 */
	public static Hashtable piglerNotifs;
//#endif

	public GatewayThread() {
		App.subscribedGuilds = new Vector();
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
		if (Settings.autoReConnect && reconnectAttempts < 3) {
			if (App.channelView != null) {
				App.channelView.bannerText = Locale.get(CHANNEL_VIEW_RECONNECTING);
				App.channelView.repaint();
			}
			App.gateway = new GatewayThread();
			App.gateway.start();
		} else {
//#ifdef OVER_100KB
			App.disp.setCurrent(new ReconnectDialog(message));
//#else
			App.disp.setCurrent(new Dialogs100kb(message));
//#endif
		}
	}

	/**
	 * Send JSON message to gateway socket.
	 */
	public void send(JSONObject msg) {
		try {
			os.write((msg.build() + "\n").getBytes("UTF-8"));
			os.flush();
		}
		catch (Exception e) {}
	}

	/**
	 * Check if a message mentions/pings the currently logged in user.
	 * Note: only checks direct user pings, not role pings.
	 * @param msgData JSON data of message
	 * @return true if the user was mentioned in this message, false if not
	 */
	private boolean isPing(JSONObject msgData) {
		JSONArray pings;
		try {
			pings = msgData.getArray("mentions");
		}
		catch (Exception e) {
			return false;
		}
		for (int i = 0; i < pings.size(); i++) {
			if (pings.getObject(i).getString("id").equals(App.myUserId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a received message should trigger a notification based on the user's notification settings.
	 * @param msgData JSON data of message
	 */
	private boolean shouldNotify(JSONObject msgData) {
		String guildID = msgData.getString("guild_id", null);
		String channelID = msgData.getString("channel_id");
		boolean isDM = (guildID == null);
		boolean isPing = isPing(msgData);

//#ifdef OVER_100KB
		boolean isMutedGuild = isDM ? false : FavoriteGuilds.isMuted(guildID);
		boolean isMuted = isMutedGuild || FavoriteGuilds.isMuted(channelID);
//#else
		final boolean isMuted = false;
//#endif

		// All notifications enabled - always notify except for muted messages that aren't mentions.
		if (Settings.showNotifsAll) return !isMuted || isPing;

		// DM notifs enabled - notify for DMs if that person is not muted
		if (isDM && Settings.showNotifsDMs && !isMuted) return true;

		// Lastly, check for mention. If mention notifications enabled, always notify for mentions even if muted.
		return isPing && Settings.showNotifsPings;
	}

	private void handleNotification(JSONObject msgData) {
		Message msg = new Message(msgData);

		// Display name of person who sent the message.
		String author = msg.author.name;

		// Name of the server and channel where the notification occurred.
		// "(unknown)" if server list not loaded.
		// "Server Name" if server list loaded, but channel list for that server not loaded.
		// "Server Name #channel" if server and channel lists loaded.
		// null if notification occurred in a DM
		String location = null;

		// Message content, limited to 50 characters, with attachments parsed into text, e.g. "Text content (3 attachments)"
		// This is now stored in msg.content

		// true if message sent in a direct message, false if sent in a server or DM group
		boolean isDM = false;

		String guildID = msgData.getString("guild_id", null);
		String channelID = msgData.getString("channel_id", null);
		
		if (guildID == null) {
			// Get DM channel name, show it as the location if message was sent in a group DM
			DMChannel c = DMChannel.getById(channelID);
			if (c != null && c.isGroup) {
				location = c.name;
			} else {
				isDM = true;
			}
		} else {
			// Get the name of the server where the message was sent
			// (only available if server list has been loaded)
			Guild g = Guild.getById(guildID);
			location = (g != null) ? g.name : Locale.get(NAME_UNKNOWN);

			// Get the name of the channel
			// (only available if channel list for that server has been loaded)
			Channel c = Channel.getByID(channelID);
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
		msg.content = c.toString();

		Notification notif = new Notification(guildID, channelID);

		if (Settings.showNotifAlert) {
//#ifdef OVER_100KB
			App.disp.setCurrent(new NotificationDialog(notif, location, msg));
//#else
			App.disp.setCurrent(new Dialogs100kb(notif, location, msg));
//#endif
		}
		
//#ifdef PIGLER_SUPPORT
		synchronized (piglerLock) {
			if (Settings.showNotifPigler && pigler != null) {
				try {
					int uid;
					String notificationText = isDM ? msg.content : (author + ": " + msg.content);
					uid = pigler.createNotification(location, notificationText, appIcon, true);
					try {
						pigler.showGlobalPopup(location, notificationText, 0);
					} catch (Throwable ignored) {}
					piglerNotifs.put(new Integer(uid), notif);
				}
				catch (Exception e) {}
			}
		}
//#endif

//#ifdef NOKIA_UI_SUPPORT
		if (Settings.showNotifNokiaUI && Util.supportsNokiaUINotifs) {
			try {
				SoftNotification sn = SoftNotification.newInstance();
				sn.setText(
					Notification.createString(location, msg),
					(isDM ? author : location) + ": " + msg.content
				);
				sn.post();
			}
			catch (Throwable e) {}
		}
//#endif
	}

//#ifdef PIGLER_SUPPORT
	public void checkInitPigler() {
		synchronized (piglerLock) {
			if (Settings.showNotifPigler) {
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
			App.error(Locale.get(PIGLER_NOT_SUPPORTED));
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
			App.error(Locale.get(PIGLER_ERROR) + e.toString());
		}
	}
	
	public void handleNotificationTap(int uid) {
		Integer uidObject = new Integer(uid);
		Notification notif = (Notification) piglerNotifs.get(uidObject);
		if (notif == null) return;

		piglerNotifs.remove(uidObject);
		notif.view();
	}
//#endif

//#ifdef OVER_100KB
	public Player playNotificationSound() {
		Player result = null;

		if (Settings.playNotifSound) {
			RecordStore rms = null;
			InputStream is = null;
			String fileName = "/notify.mid";
			try {
				rms = RecordStore.openRecordStore("notifsound", false);
				is = new ByteArrayInputStream(rms.getRecord(2));
				fileName = Util.bytesToString(rms.getRecord(1));
			}
			catch (Exception e) {}

			Util.closeRecordStore(rms);

			if (is == null) is = getClass().getResourceAsStream("/notify.mid");
			
			try {
				result = NotificationSoundDialog.playSound(fileName, is);
			}
			catch (Exception e) {
				AlertType.ALARM.playSound(App.disp);
			}
		}
		if (Settings.playNotifVibra) {
			App.disp.vibrate(1000);
		}
		return result;
	}
//#else
	public void playNotificationSound() {
		if (Settings.playNotifSound) {
			AlertType.ALARM.playSound(App.disp);
		}
		if (Settings.playNotifVibra) {
			App.disp.vibrate(1000);
		}
	}
//#endif


	public void run() {
		try {
//#ifdef PIGLER_SUPPORT
			checkInitPigler();
//#endif

			sc = (SocketConnection) Connector.open(App.getPlatformSpecificUrl(Settings.gatewayUrl));

			// Not supported on JBlend (e.g. some Samsungs)
//#ifdef OVER_100KB
			try {
				sc.setSocketOption(SocketConnection.KEEPALIVE, 1);
			}
			catch (Exception e) {}
//#endif

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
						events.add("J2ME_MESSAGE_UPDATE");
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
						if (App.channelView != null && Locale.get(CHANNEL_VIEW_RECONNECTING).equals(App.channelView.bannerText)) {
							App.channelView.bannerText = null;
							App.channelView.repaint();
						}
//#ifdef EMOJI_SUPPORT
						App.gatewayToggleGuildEmoji();
//#endif
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
							!App.channelIsOpen
							|| (App.isDM && !chId.equals(App.selectedDmChannel.id))
							|| (!App.isDM && !chId.equals(App.selectedChannel.id))
						) {
							// Don't set unread indicator if message was sent by the logged in user
							if (authorID.equals(App.myUserId)) continue;

							if (shouldNotify(msgData)) {
								// If alert window is enabled, the sound will instead be played when it is shown on screen
								// so it does not get cut off due to the current screen changing
								if (!Settings.showNotifAlert) {
//#ifdef OVER_100KB
									Player player = playNotificationSound();
									new Thread(new NotificationDialog(player)).start();  // start thread which will close the player later
//#else
									playNotificationSound();
//#endif
								}

								if (
									Settings.showNotifAlert
//#ifdef PIGLER_SUPPORT
									|| Settings.showNotifPigler
//#endif
								) handleNotification(msgData);
							}

							Channel ch = Channel.getByID(chId);
							if (ch != null) {
								ch.lastMessageID = Long.parseLong(msgId);
								App.updateUnreadIndicators(false, chId);
								continue;
							}
							DMChannel dmCh = DMChannel.getById(chId);
							if (dmCh != null) {
								dmCh.lastMessageID = Long.parseLong(msgId);
								App.updateUnreadIndicators(true, chId);
							}
							continue;
						}
						
						// If message was sent in the currently opened channel, update the channel view accordingly:

						// If the message is already shown, don't show it again (check for duplicate ID)
						boolean skip = false;
						for (int i = 0; i < App.messages.size(); i++) {
							Message m = (Message) App.messages.elementAt(i);
							if (m.id.equals(msgId)) {
								skip = true;
								break;
							}
						}
						if (skip) continue;

						// If we're on the newest page, make the new message visible
						if (App.channelView.page == 0 && !App.channelView.outdated) {
							// Add the new message to the message list
							App.messages.insertElementAt(new Message(msgData), 0);

							// Remove the oldest message in the message list so it doesn't break pagination
							// Except for channels that have less messages than the full page capacity
							if (App.messages.size() > Settings.messageLoadCount) {
								App.messages.removeElementAt(App.messages.size() - 1);
							}
						}

						// Remove this user's typing indicator
						if (App.isDM) {
							if (App.typingUsers.size() >= 1) {
								App.typingUsers.removeElementAt(0);
								App.typingUserIDs.removeElementAt(0);
							}
						} else {
							for (int i = 0; i < App.typingUsers.size(); i++) {
								if (App.typingUserIDs.elementAt(i).equals(authorID)) {
									App.typingUsers.removeElementAt(i);
									App.typingUserIDs.removeElementAt(i);
								}
							}
						}

						// Redraw the message list and mark it as read
						if (App.channelView.page == 0) {
							App.channelView.requestUpdate(true, true);
							UnreadManager.autoSave = false;
							UnreadManager.markRead(chId, Long.parseLong(msgId));
							UnreadManager.autoSave = true;
						} else {
							// If user is not on the newest page of messages, ask them to refresh
							// There is no easy way to do it any other way without breaking pagination
							App.channelView.outdated = true;
						}

						App.channelView.repaint();
						App.channelView.serviceRepaints();
					}
					else if (op.equals("MESSAGE_DELETE")) {
						if (App.channelView == null) continue;

						JSONObject msgData = message.getObject("d");

						String channel = msgData.getString("channel_id", "");
						String selected = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;
						if (!channel.equals(selected)) continue;

						String messageId = msgData.getString("id");

						for (int i = 0; i < App.messages.size(); i++) {
							Message msg = (Message) App.messages.elementAt(i);
							if (!msg.id.equals(messageId)) continue;

							msg.delete();

							App.channelView.requestUpdate(true, false);
							App.channelView.repaint();
							App.channelView.serviceRepaints();
							break;
						}
					}
					else if (op.equals("J2ME_MESSAGE_UPDATE")) {
						if (App.channelView == null) continue;

						JSONObject msgData = message.getObject("d");

						// Check if content was changed (other parts of the message can change too,
						// but currently we can only update the content)
						String newContent = msgData.getString("content", null);
						if (newContent == null) continue;

						String channel = msgData.getString("channel_id", "");
						String selected = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;
						if (!channel.equals(selected)) continue;

						String messageId = msgData.getString("id");

						for (int i = 0; i < App.messages.size(); i++) {
							Message msg = (Message) App.messages.elementAt(i);
							if (!msg.id.equals(messageId)) continue;

							msg.content = newContent;
							msg.rawContent = newContent;
							msg.needUpdate = true;
//#ifdef OVER_100KB
							msg.isEdited = true;
//#endif

							App.channelView.requestUpdate(true, false);
							App.channelView.repaint();
							App.channelView.serviceRepaints();
							break;
						}
					}
					else if (op.equals("TYPING_START")) {
						if (App.channelView == null) continue;

						JSONObject msgData = message.getObject("d");
						String channel = msgData.getString("channel_id");

						if (App.isDM) {
							// Check that the opened channel (if there is any) is the one where the typing event happened
							if (!channel.equals(App.selectedDmChannel.id)) continue;

							// Typing events not supported in group DMs (typing event contains guild member info if it happened in a server, but not user info; in a group DM, there's no easy way to know who started typing)
							if (App.selectedDmChannel.isGroup) continue;

							// If we are in a one person DM, then we know the typing user is the other participant
							// If we already have a typing indicator, don't create a dupe
							if (App.typingUsers.size() >= 1) continue;

							App.typingUsers.addElement(App.selectedDmChannel.name);
							App.typingUserIDs.addElement("0");

							// Remove the name from the typing list after 10 seconds
//#ifdef OVER_100KB
							new StopTypingThread("0").start();
//#else
							new Threads100kb("0").start();
//#endif
						} else {
							if (!channel.equals(App.selectedChannel.id)) continue;

							try {
								// Get this user's name and add it to the typing users list
								JSONObject userObj = msgData.getObject("member").getObject("user");
								
								String author = userObj.getString("global_name", null);
								if (author == null) {
									author = userObj.getString("username", Locale.get(NAME_UNKNOWN));
								}

								// If this user is already in the list, don't add them again
								String id = userObj.getString("id");
								if (App.typingUserIDs.indexOf(id) != -1) continue;

								// If this user is the person using the app, don't add
								if (id.equals(App.myUserId)) continue;

								App.typingUsers.addElement(author);
								App.typingUserIDs.addElement(id);

//#ifdef OVER_100KB
								new StopTypingThread(id).start();
//#else
								new Threads100kb(id).start();
//#endif
							}
							catch (Exception e) {}
						}

						App.channelView.repaint();
					}
					else if (op.equals("GUILD_MEMBERS_CHUNK")) {
						if (App.channelView == null || App.selectedGuild == null) continue;

						JSONObject data = message.getObject("d");
						JSONArray members = data.getArray("members");

						if (App.disp.getCurrent() instanceof MentionForm) {
							// Guild member request was for inserting a mention
							((MentionForm) App.disp.getCurrent()).searchCallback(members);
						} else {
							// Guild member request was for role data (name colors)
							String guildId = data.getString("guild_id");
							JSONArray notFound = data.getArray("not_found");

							for (int i = 0; i < notFound.size(); i++) {
								String id = notFound.getString(i);
								NameColorCache.set(id + guildId, 0);
							}

							for (int i = 0; i < members.size(); i++) {
								int resultColor = 0;

								JSONObject member = members.getObject(i);
								JSONArray memberRoles = member.getArray("roles");

								for (int r = 0; r < App.selectedGuild.roles.size(); r++) {
									Role role = (Role) App.selectedGuild.roles.elementAt(r);
									if (memberRoles.indexOf(role.id) == -1) continue;

									resultColor = role.color;
									break;
								}

								String id = member.getObject("user").getString("id");
								NameColorCache.set(id + guildId, resultColor);
							}
							NameColorCache.activeRequest = false;
						}
					}
					else if (op.equals("J2ME_READY")) {
						if (App.myUserId == null) {
							App.myUserId = message.getObject("d").getString("id");
						}
						reconnectAttempts = 0;
					}
				}
				else if (message.getInt("op", 0) == 10) {
					int heartbeatInterval = message.getObject("d").getInt("heartbeat_interval");
//#ifdef OVER_100KB
					hbThread = new HeartbeatThread(heartbeatInterval);
//#else
					hbThread = new Threads100kb(heartbeatInterval);
//#endif
					hbThread.start();

					// Identify
					JSONObject idProps = new JSONObject();
					idProps.put("os", "Linux");
					idProps.put("browser", "Firefox");
					idProps.put("device", "");
			
					JSONObject idData = new JSONObject();
					idData.put("token", Settings.token);
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