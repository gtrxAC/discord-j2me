package com.gtrxac.discord;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.rms.*;

public class HTTPThread extends Thread implements Strings {
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
    static final int VIEW_NOTIFICATION = 11;
    static final int FETCH_LANGUAGE = 12;
    static final int FETCH_THREADS = 13;
//#ifdef EMOJI_SUPPORT
    static final int FETCH_EMOJIS = 14;
//#endif
//#ifdef OVER_100KB
    static final int VIEW_ATTACHMENT_AUDIO = 15;
    static final int SET_THEME = 16;
//#endif

    private static final String BOUNDARY = "----WebKitFormBoundary7MA4YWykTrZu0gW";
    private static final String LINE_FEED = "\r\n";

    private static boolean haveFetchedUserInfo;
//#ifdef EMOJI_SUPPORT
    private static int newEmojiJsonVersion;
    private static JSONArray newEmojiSheetVersions;
//#endif

    int action;
    boolean silent;

    // Parameters for FETCH_GUILDS
    boolean showFavGuilds;
    boolean forceReload;

    // Parameters for FETCH_MESSAGES
    String fetchMsgsBefore;
    String fetchMsgsAfter;

    // Parameters for SEND_MESSAGE (and SEND_ATTACHMENT)
	String sendMessage;
	String sendReference;  // ID of the message the user is replying to
	boolean sendPing;

    // Parameters for FETCH_ICON
    HasIcon iconTarget;  // item (guild or DM channel) that this icon should be assigned to

    // Parameters for SEND_ATTACHMENT
    FileConnection attachFc;
    String attachName;

    // Parameters for VIEW_ATTACHMENT_TEXT and VIEW_ATTACHMENT_AUDIO
    Attachment viewAttach;

    // Parameters for EDIT_MESSAGE
    Message editMessage;
    String editContent;

    // Parameters for VIEW_NOTIFICATION
    boolean isDM;
    String guildID;
    String channelID;

    // Parameters for FETCH_LANGUAGE
    String langID;

    private boolean showLoad;

    public HTTPThread(int action) {
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

        try {
            return r.toString().getBytes("ISO-8859-1");
        }
        catch (UnsupportedEncodingException e) {
            return r.toString().getBytes();
        }
    }

    private boolean shouldShowLoadScreen() {
        return !App.dontShowLoadScreen
            && action != FETCH_ICON
            && action != FETCH_ATTACHMENTS
            && action != SEND_MESSAGE
            && action != EDIT_MESSAGE
            && action != DELETE_MESSAGE
            && action != FETCH_LANGUAGE;
    }

    private void setBannerText(String newText) {
        if (!showLoad && App.channelView != null) {
            App.channelView.bannerText = newText;
            App.channelView.repaint();
        }
    }

    private void runSilentHTTP(int action) {
        HTTPThread h = new HTTPThread(action);
        App.dontShowLoadScreen = true;
        h.silent = true;
        h.start();
        try {
            h.join();
        }
        catch (Exception e) {}
    }

    private void setScreen(Displayable d) {
        if (!silent) App.disp.setCurrent(d);
    }

    public void run() {
        showLoad = shouldShowLoadScreen();
        App.dontShowLoadScreen = false;

        Displayable prevScreen = App.disp.getCurrent();
        LoadingScreen loadScreen = null;

        if (showLoad) {
            loadScreen = new LoadingScreen();
            App.disp.setCurrent(loadScreen);
        }

        // Fix HTTP error 404 - check that API URL does not end with a "/", which would cause duplicate "/"s in requested URLs
//#ifdef OVER_100KB
        while (Settings.api.endsWith("/")) {
            Settings.api = Settings.api.substring(0, Settings.api.length() - 1);
        }
//#endif

        try {
            // Fetch user info if needed (upon first API request after starting app)
            // If Discord J2ME-specific proxy server is in use, this also checks for auto updates to app and emoji data
            if ((!haveFetchedUserInfo || App.myUserId == null) && action != FETCH_LANGUAGE) {
                JSONObject resp = JSON.getObject(HTTP.get("/users/@me"));
                App.myUserId = resp.getString("id", "");
                App.isLiteProxy = resp.getBoolean("_liteproxy", false);
                App.uploadToken = resp.getString("_uploadtoken", Settings.token);
                haveFetchedUserInfo = true;

                int latest = resp.getInt("_latestbeta", 0);

                if (latest > App.VERSION_CODE && Settings.autoUpdate == Settings.AUTO_UPDATE_ALL) {
                    String latestName = resp.getString("_latestbetaname", Locale.get(NAME_UNKNOWN));
//#ifdef OVER_100KB
                    App.disp.setCurrent(new UpdateDialog(latestName, true));
//#else
                    App.disp.setCurrent(new Dialogs100kb(latestName, true));
//#endif
                    return;
                }
                
                latest = resp.getInt("_latest", 0);

                if (latest > App.VERSION_CODE && Settings.autoUpdate != Settings.AUTO_UPDATE_OFF) {
                    String latestName = resp.getString("_latestname", Locale.get(NAME_UNKNOWN));
//#ifdef OVER_100KB
                    App.disp.setCurrent(new UpdateDialog(latestName, false));
//#else
                    App.disp.setCurrent(new Dialogs100kb(latestName, false));
//#endif
                    return;
                }

//#ifdef EMOJI_SUPPORT
                newEmojiJsonVersion = resp.getInt("_emojiversion", 0);
                newEmojiSheetVersions = resp.getArray("_emojisheets", null);
//#endif
            }

//#ifdef EMOJI_SUPPORT
            // Check for updates to emoji data and download new json/sheet data if needed
            // (upon first API request after starting app with emojis enabled, or first API request after enabling emojis)
            if (
                showLoad && haveFetchedUserInfo && newEmojiSheetVersions != null &&
                (FormattedString.emojiMode != FormattedString.EMOJI_MODE_OFF || action == FETCH_EMOJIS)
            ) {
                // Emoji RMS layout:
                // - 1st record is a json array of version numbers (the first number is for the json, the rest are for the sheets)
                // - 2nd record is emoji.json data
                // - the rest are the sheet pngs
                
                RecordStore rms = null;
                JSONArray curVersionsArray = null;
                boolean needRmsWrite = false;
                
                try {
                    // Check if emoji JSON needs to be updated (if saved ver is outdated)
                    rms = RecordStore.openRecordStore("emoji", true);
                    int numRecs = rms.getNumRecords();
                    int curJsonVersion;

                    if (numRecs >= 1) {
                        curVersionsArray = JSON.getArray(Util.bytesToString(rms.getRecord(1)));
                        curJsonVersion = curVersionsArray.getInt(0, -1);
                    } else {
                        curVersionsArray = new JSONArray();
                        curJsonVersion = -1;
                        byte[] empty = "[]".getBytes();
                        rms.addRecord(empty, 0, empty.length);
                        needRmsWrite = true;
                    }
    
                    // Emoji JSON is outdated or not downloaded - download the latest one
                    if (curJsonVersion < newEmojiJsonVersion || numRecs < 2) {
                        loadScreen.text = Locale.get(DOWNLOADING);
                        String emojiJson = Util.bytesToString(HTTP.getBytes(Settings.api + "/emoji/emoji.json"));
    
                        curVersionsArray.put(0, newEmojiJsonVersion);
                        Util.setOrAddRecord(rms, 2, emojiJson);
                        needRmsWrite = true;
                    }
    
                    int sheetCount = newEmojiSheetVersions.size();
    
                    // Download the required new emoji sheets. If an emoji sheet is already downloaded and isn't outdated, we don't download it again.
                    for (int i = 0; i < sheetCount; i++) {
                        int newVersion = newEmojiSheetVersions.getInt(i);
                        int currentVersion = curVersionsArray.getInt(i + 1, -1);
                        
                        if (currentVersion < newVersion) {
                            loadScreen.text = Locale.get(DOWNLOADING);
                            loadScreen.text2 = Locale.get(LEFT_PAREN) + i + "/" + sheetCount + Locale.get(RIGHT_PAREN);
                            byte[] img = HTTP.getBytes(Settings.api + "/emoji/emoji" + i + ".png");
                            Util.setOrAddRecord(rms, 3 + i, img);
                            curVersionsArray.put(i + 1, newVersion);
                            needRmsWrite = true;
                        }
                    }
    
                    newEmojiSheetVersions = null;
                    loadScreen.text = Locale.get(LOADING);
                    loadScreen.text2 = "";
                    FormattedStringPartEmoji.loadEmoji(App.messageFont.getHeight());
                } finally {
                    try {
                        if (needRmsWrite) Util.setOrAddRecord(rms, 1, Util.stringToBytes(curVersionsArray.build()));
                    }
                    catch (Exception e) {}
                    Util.closeRecordStore(rms);
                }
            }
//#endif

            switch (action) {
                case FETCH_GUILDS: {
                    JSONArray guilds = null;
                    RecordStore rms = null;
                    boolean wasFetched = false;

                    try {
                        rms = RecordStore.openRecordStore("guild", false);
                    }
                    catch (Exception e) {}

                    if (!forceReload) {
                        try {
                            String savedID = Util.bytesToString(rms.getRecord(2));

                            if (savedID.equals(App.myUserId)) {
                                guilds = JSON.getArray(Util.bytesToString(rms.getRecord(3)));
                            }
                        }
                        catch (Exception e) {}
                    }

                    if (guilds == null) {
                        String url = "/users/@me/guilds";
                        if (!forceReload && App.isLiteProxy) url += "?c";  // query parameter for using proxy cache
                        guilds = JSON.getArray(HTTP.get(url));
                        wasFetched = true;
                    }
                    App.guilds = new Vector();

                    for (int i = 0; i < guilds.size(); i++) {
                        App.guilds.addElement(new Guild(guilds.getObject(i)));
                    }

                    if (wasFetched
//#ifndef UNLIMITED_RMS
                        && rms != null
//#endif
                    ) GuildSelector.saveGuilds(false);

                    Util.closeRecordStore(rms);

                    if (showFavGuilds) {
                        FavoriteGuilds.openSelector(false, false);
                    } else {
                        App.guildSelector = new GuildSelector(App.guilds, false);
                        setScreen(App.guildSelector);
                    }
                    break;
                }

                case FETCH_CHANNELS: {
                    // Fetch role data (role colors) for this server if needed
                    if (App.gatewayActive() && App.selectedGuild.roles == null && Settings.useNameColors) {
                        String roleData = HTTP.get("/guilds/" + App.selectedGuild.id + "/roles");
                        JSONArray roleArr = JSON.getArray(roleData);

                        App.selectedGuild.roles = new Vector();

                        if (App.isLiteProxy) {
                            // Sorted server-side: load roles as-is
                            for (int i = roleArr.size() - 1; i >= 0; i--) {
                                JSONObject data = roleArr.getObject(i);
                                
                                if (data.getInt("color") == 0) continue;
                                
                                App.selectedGuild.roles.addElement(new Role(data));
                            }
                        } else {
                            // Not sorted server-side: manually sort based on 'position' field
                            for (int i = roleArr.size() - 1; i >= 0; i--) {
                                for (int a = roleArr.size() - 1; a >= 0; a--) {
                                    JSONObject data = roleArr.getObject(a);

                                    if (data.getInt("position", i) != i) continue;
                                    if (data.getInt("color") == 0) continue;

                                    App.selectedGuild.roles.addElement(new Role(data));
                                }
                            }
                        }
                    }

                    App.selectedGuild.channels = Channel.parseChannels(
                        JSON.getArray(HTTP.get("/guilds/" + App.selectedGuild.id + "/channels"))
                    );

                    App.channels = App.selectedGuild.channels;
                    App.channelSelector = new ChannelSelector();
                    setScreen(App.channelSelector);
                    break;
                }

                case FETCH_DM_CHANNELS: {
                    JSONArray channels = JSON.getArray(HTTP.get("/users/@me/channels"));
                    App.dmChannels = new Vector();
            
                    for (int i = 0; i < channels.size(); i++) {
                        JSONObject ch = channels.getObject(i);
                        int type = ch.getInt("type", 1);
                        if (type != 1 && type != 3) continue;
            
                        App.dmChannels.addElement(new DMChannel(ch));
                    }
                    App.dmSelector = new DMSelector();
                    setScreen(App.dmSelector);
                    break;
                }

                case SEND_MESSAGE: {
                    if (!showLoad && App.channelView != null) {
                        App.disp.setCurrent(App.channelView);
                        App.channelView.bannerText = Locale.get(CHANNEL_VIEW_SENDING);
                        App.channelView.repaint();
                    }

                    String channelId = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;

                    JSONObject json = new JSONObject();
                    json.put("content", sendMessage);
                    json.put("flags", 0);
                    json.put("mobile_network_type", "unknown");
                    json.put("tts", false);

                    // Reply
                    if (sendReference != null) {
                        JSONObject ref = new JSONObject();
                        ref.put("channel_id", channelId);
                        if (!App.isDM) ref.put("guild_id", App.selectedGuild.id);
                        ref.put("message_id", sendReference);
                        json.put("message_reference", ref);

                        if (!sendPing && !App.isDM) {
                            JSONObject ping = new JSONObject();
                            ping.put("replied_user", false);
                            json.put("allowed_mentions", ping);
                        }
                    }

                    JSONObject message = JSON.getObject(HTTP.post("/channels/" + channelId + "/messages", json));

                    // If gateway enabled, don't need to fetch new messages
                    if (App.gatewayActive()) {
                        setBannerText(null);
                        break;
                    }

                    // Mark our message as read (only needed when gateway not active)
                    UnreadManager.autoSave = false;
                    UnreadManager.markRead(channelId, Long.parseLong(message.getString("id")));
                    UnreadManager.autoSave = true;

                    // fall through (if gateway disabled, fetch messages because there might have 
                    // been other messages sent during the time the user was writing their message)
                }

                case FETCH_MESSAGES: {
                    setBannerText(Locale.get(CHANNEL_VIEW_LOADING));

                    String channelId = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;
                    
                    StringBuffer url = new StringBuffer(
                        "/channels/" + channelId + "/messages?limit=" + Settings.messageLoadCount
                    );
                    if (fetchMsgsBefore != null) url.append("&before=" + fetchMsgsBefore);
                    if (fetchMsgsAfter != null) url.append("&after=" + fetchMsgsAfter);
//#ifdef OVER_100KB
                    if (App.isLiteProxy) {
//#ifdef EMOJI_SUPPORT
                        if (FormattedString.emojiMode == FormattedString.EMOJI_MODE_ALL) {
                            url.append("&emoji=1");
                        }
//#endif
                        if (FormattedString.useMarkdown) {
                            url.append("&edit=1");
                        }
                    }
//#endif

                    JSONArray messages = JSON.getArray(HTTP.get(url.toString()));
                    App.messages = new Vector();

                    for (int i = 0; i < messages.size(); i++) {
                        App.messages.addElement(new Message(messages.getObject(i)));
                    }

                    if ((fetchMsgsBefore == null && fetchMsgsAfter == null) || sendMessage != null) {
                        // If user opened a new channel or sent a message, create a new channel view
                        App.channelView = new ChannelView();
                        App.markCurrentChannelRead();
                    } else {
                        // If user scrolled a page back or forward, keep reusing the same channel view
                        App.channelView.requestUpdate(false, false);
                    }

                    // Show the channel view screen (hide the loading screen)
                    App.channelView.bannerText = null;
                    App.channelView.updateTitle();
                    App.disp.setCurrent(App.channelView);
                    App.channelView.repaint();

                    App.typingUsers = new Vector();
                    App.typingUserIDs = new Vector();
                    break;
                }

                case FETCH_ATTACHMENTS: {
                    if (Settings.cdn == null || Settings.cdn.length() == 0) {
                        throw new Exception(Locale.get(CDN_ERROR_ATTACHMENT));
                    }

                    Vector attachments = App.attachmentView.msg.attachments;
                    int layout = Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE;

                    // Fix for https://github.com/nikita36078/J2ME-Loader/pull/1046
//#ifdef J2ME_LOADER
                    Util.sleep(100);
//#endif

                    for (int i = 0; i < attachments.size(); i++) {
                        Attachment attach = (Attachment) attachments.elementAt(i);

                        String attachName = attach.name +
                            Locale.get(LEFT_PAREN) + attach.size + Locale.get(RIGHT_PAREN);

                        StringItem titleItem = new StringItem(null, attachName);
                        App.attachmentView.append(titleItem);
                        
                        StringItem showButton = null;

                        if (attach.supported) {
                            // Supported attachment (image/video) -> show it
                            // For videos, only the first frame is shown (Discord media proxy converts to image)
                            try {
                                Image image = HTTP.getImage(attach.previewUrl);
                                ImageItem item = new ImageItem(null, image, Item.LAYOUT_DEFAULT, null);
                                item.setLayout(layout);
                                App.attachmentView.append(item);
                            }
                            catch (Exception e) {
                                StringItem item = new StringItem(null, e.toString());
                                item.setLayout(layout);
                                App.attachmentView.append(item);
                            }
                        } else {
                            final boolean isAudio =
//#ifdef OVER_100KB
                                attach.isAudio;
//#else
                                false;
//#endif

                            if (attach.isText || isAudio) {
                                // Unsupported -> show a button to view it as text, or if it's a sound file, option to use it as a notification sound
                                // Note: showCommand has a priority starting at 100, so when it's pressed, 
                                //       we can distinguish it from 'open in browser' buttons
                                final int label = isAudio ? USE_AS_NOTIFY_SOUND : SHOW;

                                Command showCommand = Locale.createCommand(label, Command.ITEM, i + 100);
                                showButton = new StringItem(null, Locale.get(label + 1), Item.BUTTON);
                                showButton.setLayout(layout);
                                showButton.setDefaultCommand(showCommand);
                                showButton.setItemCommandListener(App.attachmentView);
                                App.attachmentView.append(showButton);
                            }

//#ifdef OVER_100KB
                            if (attach.name.endsWith(".json")) {
                                // Command showCommand = Locale.createCommand(label, Command.ITEM, i + 200);
                                // showButton = new StringItem(null, Locale.get(label + 1), Item.BUTTON);
                                Command themeCommand = new Command("Use", Command.ITEM, i + 200);
                                StringItem themeButton = new StringItem(null, "Use as theme", Item.BUTTON);
                                themeButton.setLayout(layout);
                                themeButton.setDefaultCommand(themeCommand);
                                themeButton.setItemCommandListener(App.attachmentView);
                                App.attachmentView.append(themeButton);
                            }
//#endif
                        }

                        Command openCommand = Locale.createCommand(OPEN_IN_BROWSER, Command.ITEM, i);
                        StringItem openButton = new StringItem(null, Locale.get(OPEN_IN_BROWSER_L), Item.BUTTON);
                        openButton.setLayout(layout);
                        openButton.setDefaultCommand(openCommand);
                        openButton.setItemCommandListener(App.attachmentView);
                        App.attachmentView.append(openButton);

                        // Fix unselectable buttons on early Symbian 9.3 (e.g. N86)
//#ifdef MIDP2_GENERIC
                        if (Util.isSymbian93 && i == 0) {
                            App.disp.setCurrentItem((showButton != null) ? showButton : openButton);
                        }
//#endif

                        Spacer sp = new Spacer(App.attachmentView.getWidth(), App.attachmentView.getHeight()/10);
                        App.attachmentView.append(sp);
                    }
                    break;
                }

                case FETCH_ICON: {
                    if (Settings.cdn == null || Settings.cdn.length() == 0) throw new Exception();

                    String type = iconTarget.getIconType();
                    String id = iconTarget.getIconID();
                    String hash = iconTarget.getIconHash();
                    // Choose image file format based on user settings. Emojis are always png.
//#ifdef EMOJI_SUPPORT
                    boolean notEmoji = !(iconTarget instanceof FormattedStringPartGuildEmoji);
                    String format = ((Settings.useJpeg && notEmoji) ? "jpg" : "png");
//#else
                    String format = (Settings.useJpeg ? "jpg" : "png");
//#endif
                    int size = (Settings.pfpSize == Settings.ICON_SIZE_32) ? 32 : 16;

                    String urlHashPart
//#ifdef EMOJI_SUPPORT
                    = ""; if (notEmoji) urlHashPart
//#endif
                    = "/" + hash;

                    Image icon = HTTP.getImage(Settings.cdn + type + id + urlHashPart + "." + format + "?size=" + size);

                    // Resize menu icon if fetched size doesn't match requested size
                    if (!(iconTarget instanceof User) && size%16 != 0) {
                        icon = Util.resizeImageBilinear(icon, Settings.menuIconSize, Settings.menuIconSize);
                    }

                    IconCache.set(hash, icon);
                    iconTarget.iconLoaded();
                    break;
                }

                case SEND_ATTACHMENT: {
                    loadScreen.text = Locale.get(SENDING);

                    HttpConnection httpConn = null;
                    DataOutputStream os = null;

                    try {
                        String id = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;
                        httpConn = HTTP.openConnection("/channels/" + id + "/upload");
                        httpConn.setRequestMethod(HttpConnection.POST);
                        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

                        os = httpConn.openDataOutputStream();

                        os.write(createFormPart("token", null));
                        os.write(Settings.token.getBytes());
                        os.write(LINE_FEED.getBytes());

                        os.write(createFormPart("content", null));
                        if (sendMessage != null) {
                            os.write(Util.stringToBytes(sendMessage));
                        }
                        os.write(LINE_FEED.getBytes());

                        if (sendReference != null) {
                            os.write(createFormPart("reply", null));
                            os.write(sendReference.getBytes());
                            os.write(LINE_FEED.getBytes());

                            os.write(createFormPart("ping", null));
                            os.write(sendPing ? "1".getBytes() : "0".getBytes());
                            os.write(LINE_FEED.getBytes());
                        }

                        os.write(createFormPart("files", attachName));

                        InputStream fileInputStream = attachFc.openInputStream();

                        byte[] buffer = new byte[1024];
                        int bytesRead = -1;

                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }

                        os.write(LINE_FEED.getBytes());
                        os.write(("--" + BOUNDARY + "--" + LINE_FEED).getBytes());
                        os.flush();

                        fileInputStream.close();
                        HTTP.sendRequest(httpConn);
                        attachFc.close();
                        new HTTPThread(FETCH_MESSAGES).start();
                    }
                    finally {
                        try { os.close(); } catch (Exception e) {}
                        try { httpConn.close(); } catch (Exception e) {}
                    }
                    break;
                }

                case VIEW_ATTACHMENT_TEXT: {
                    byte[] textBytes = HTTP.getBytes(viewAttach.browserUrl);
                    String text = Util.bytesToString(textBytes);
                    
                    MessageCopyBox copyBox = new MessageCopyBox(viewAttach.name, text);
                    copyBox.lastScreen = App.attachmentView;
                    App.disp.setCurrent(copyBox);
                    break;
                }

                case EDIT_MESSAGE: {
                    setBannerText(Locale.get(CHANNEL_VIEW_EDITING));
                    
                    JSONObject newMessage = new JSONObject();
                    newMessage.put("content", editContent);

                    String channelId = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;
                    String path = "/channels/" + channelId + "/messages/" + editMessage.id + "/edit";
                    HTTP.post(path, newMessage);

                    // Manually update message content if gateway disabled
                    // (if enabled, new message content will come through gateway event)
                    if (!App.gatewayActive()) {
                        editMessage.content = editContent;
                        editMessage.rawContent = editContent;
                        editMessage.needUpdate = true;
                        App.channelView.requestUpdate(false, false);
                    }

                    setBannerText(null);
                    break;
                }

                case DELETE_MESSAGE: {
                    setBannerText(Locale.get(CHANNEL_VIEW_DELETING));

                    String channelId = App.isDM ? App.selectedDmChannel.id : App.selectedChannel.id;

                    HTTP.get("/channels/" + channelId + "/messages/" + editMessage.id + "/delete");

                    // Manually update message to be deleted if gateway disabled
                    // (if enabled, deletion event will come through gateway)
                    if (!App.gatewayActive()) {
                        editMessage.delete();
                        App.channelView.requestUpdate(false, false);
                    }

                    setBannerText(null);
                    break;
                }

                case VIEW_NOTIFICATION: {
                    App.isDM = (guildID == null);
                    if (App.isDM) {
                        if (App.dmSelector == null) {
                            runSilentHTTP(HTTPThread.FETCH_DM_CHANNELS);
                        }
                        App.selectedDmChannel = DMChannel.getById(channelID);
                    } else {
                        if (App.guilds == null || App.guildSelector == null) {
                            runSilentHTTP(HTTPThread.FETCH_GUILDS);
                        }
                        Guild prevSelectedGuild = App.selectedGuild;
                        App.selectedGuild = Guild.getById(guildID);

                        if (App.channels == null || App.channelSelector == null || prevSelectedGuild != App.selectedGuild) {
                            runSilentHTTP(HTTPThread.FETCH_CHANNELS);
                        }
                        App.selectedChannel = Channel.getByID(channelID);
                    }
                    App.dontShowLoadScreen = true;
                    new HTTPThread(HTTPThread.FETCH_MESSAGES).start();
                    break;
                }

                case FETCH_LANGUAGE: {
                    byte[] langBytes = null;

//#ifdef NOKIA_128PX
                    try {
                        langBytes = HTTP.getBytes(Settings.api + "/lang/" + langID + "-compact.json");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
//#endif
                    
                    if (langBytes == null) {
                        langBytes = HTTP.getBytes(Settings.api + "/lang/" + langID + ".json");
                    }

                    Locale.languageLoaded(langID, Util.bytesToString(langBytes));
                    if (App.disp.getCurrent() instanceof MainMenu) {
                        App.disp.setCurrent(MainMenu.get(true));
                    }
                    break;
                }

                case FETCH_THREADS: {
                    String threadData = HTTP.get(
                        "/channels/" +
                        App.selectedChannelForThreads.id +
                        "/threads/search?sort_by=last_message_time&sort_order=desc&limit=25&tag_setting=match_some&offset=0"
                    );

                    JSONArray threads = JSON.getObject(threadData).getArray("threads");
                    App.threads = new Vector();

                    for (int i = 0; i < threads.size(); i++) {
                        JSONObject thr = threads.getObject(i);
                        App.threads.addElement(new Channel(thr, true));
                    }

                    App.selectedChannelForThreads.threads = App.threads;
                    App.threadSelector = new ThreadSelector();
                    setScreen(App.threadSelector);
                    break;
                }

//#ifdef EMOJI_SUPPORT
                case FETCH_EMOJIS: {
                    // emoji data was already fetched (before the switch statement) so open emoji picker
                    EmojiPicker picker = new EmojiPicker();
                    picker.lastScreen = ((EmojiDownloadDialog) prevScreen).lastScreen;
                    App.disp.setCurrent(picker);
                    break;
                }
//#endif

//#ifdef OVER_100KB
                case VIEW_ATTACHMENT_AUDIO: {
                    byte[] bytes = HTTP.getBytes(viewAttach.browserUrl);
                    App.disp.setCurrent(new NotificationSoundDialog(viewAttach.name, bytes));
                    break;
                }

                case SET_THEME: {
                    byte[] textBytes = HTTP.getBytes(viewAttach.browserUrl);
                    String text = Util.bytesToString(textBytes);
                    App.channelView.pendingTheme = JSON.getObject(text);
                    Theme.loadJsonTheme(App.channelView.pendingTheme);
                    App.disp.setCurrent(App.channelView);
                    break;
                }
//#endif
            }
        }
        catch (Exception e) {
            switch (action) {
                case FETCH_ICON: {
                    IconCache.removeRequest(iconTarget.getIconHash());
                    break;
                }
                case SEND_ATTACHMENT: {
                    App.error(Locale.get(UPLOAD_ERROR) + e.toString(), prevScreen);
                    break;
                }
                case FETCH_LANGUAGE: {
                    // Wait until main menu is shown, then show error
                    while (!(App.disp.getCurrent() instanceof MainMenu)) {
                        Util.sleep(10);
                    }
                    App.error("Failed to download language data: " + e.toString(), MainMenu.get(false));
                    break;
                }
//#ifdef OVER_100KB
                case VIEW_ATTACHMENT_AUDIO: {
                    App.error(Locale.get(PLAY_SOUND_FAILED), prevScreen);
                    break;
                }
//#endif
                default: {
                    App.error(e, prevScreen);
                    break;
                }
            }
        }
    }
}
