package com.gtrxac.discord;

import java.util.*;
import java.io.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;

public class UnreadManager {
    private static Hashtable channels;
    public static boolean autoSave;
    public static boolean needSave;
    public static String lastUnreadTime;  // previously unread message ID for the last channel that markRead() was used on
    public static boolean lastHadUnreads;

//#ifdef OVER_100KB
    // parameters for ack/"mark as read" API call
    public static String markReadChannelID;
    public static long markReadMessageID;
//#endif

    public static void init() {
        channels = new Hashtable();
        autoSave = true;
        needSave = false;
        lastUnreadTime = null;
        lastHadUnreads = false;

        // delete old version of unreads RMS
        try {
            RecordStore.deleteRecordStore("unread");
        }
        catch (Exception e) {}

        // Load last read message IDs from RMS (convert binary to hashtable)
        try {
            RecordStore rms = RecordStore.openRecordStore("u", true);

            if (rms.getNumRecords() >= 1) {
                byte[] record = rms.getRecord(1);
                int numEntries = record.length/16;
                ByteArrayInputStream rmsStream = new ByteArrayInputStream(record);
                DataInputStream rmsDataStream = new DataInputStream(rmsStream);

                for (int i = 0; i < numEntries; i++) {
                    String key = String.valueOf(rmsDataStream.readLong());
                    String value = String.valueOf(rmsDataStream.readLong());
                    channels.put(key, value);
                }
            }
            Util.closeRecordStore(rms);
        }
        catch (RecordStoreNotFoundException e) {}
        catch (Exception e) { App.error(e); }

        // Default last read time for channels is the timestamp of when the RMS was first initialized
        if (!channels.containsKey("0")) {
            channels.put("0", String.valueOf(System.currentTimeMillis() - App.DISCORD_EPOCH));
            save();
        }
    }

    public static void save() {
//#ifdef OVER_100KB
        if (
            (!App.isLiteProxy
//#ifdef PROXYLESS_SUPPORT
            || Settings.proxyless
//#endif
            ) && markReadChannelID != null
        ) {
            new HTTPThread(HTTPThread.MARK_AS_READ).start();
        }
//#endif

        ByteArrayOutputStream rmsStream = new ByteArrayOutputStream(channels.size()*8*2);
        DataOutputStream rmsDataStream = new DataOutputStream(rmsStream);

        try {
            for (Enumeration e = channels.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String value = (String) channels.get(key);

                rmsDataStream.writeLong(Long.parseLong(key));
                rmsDataStream.writeLong(Long.parseLong(value));
            }
        }
        catch (Exception e) {}

        try {
            RecordStore rms = RecordStore.openRecordStore("u", true);
            Util.setOrAddRecord(rms, 1, rmsStream.toByteArray());
            Util.closeRecordStore(rms);
        }
        catch (Exception e) { App.error(e); }

        needSave = false;
    }

    public static void manualSave() {
        if (needSave) save();
        needSave = false;
        autoSave = true;
    }

    private static void put(String channelID, String lastReadTime) {
        if (lastReadTime == null || lastReadTime.equals("0")) return;
        channels.put(channelID, lastReadTime);

        if (autoSave) save();
        else needSave = true;
    }

    public static boolean hasUnreads(String channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;

        String lastReadTime = (String) channels.get(channelID);
        if (lastReadTime == null) {
            lastReadTime = (String) channels.get("0");
        }

        return Long.parseLong(lastReadTime) < lastMessageTime;
    }

    public static void markRead(String channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;
        
        String lastReadTime = (String) channels.get(channelID);
        if (lastReadTime == null) {
            lastReadTime = (String) channels.get("0");
        }
        boolean isUnread = (Long.parseLong(lastReadTime) < lastMessageTime);

        if (autoSave) {
            lastUnreadTime = lastReadTime;
            lastHadUnreads = isUnread;
        }
        if (isUnread) {
            put(channelID, String.valueOf(lastMessageTime));
        }

//#ifdef OVER_100KB
        markReadChannelID = channelID;
        markReadMessageID = lastMessageID;
//#endif
    }

    // used for gateway read states
    public static void forceMarkRead(String channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;
        
        String lastReadTime = (String) channels.get(channelID);
        boolean isUnread = true;

        if (lastReadTime != null) {
            isUnread = (Long.parseLong(lastReadTime) < lastMessageTime);
        }
        if (isUnread) {
            put(channelID, String.valueOf(lastMessageTime));
        }
    }

    public static void markDMsRead() {
        if (App.dmSelector != null) markRead(App.dmSelector.lastDMs);
    }

    public static void markRead(HasUnreads[] v) {
        autoSave = false;
        for (int i = 0; i < v.length; i++) {
            v[i].markRead();
        }
        autoSave = true;
        save();
    }
}
