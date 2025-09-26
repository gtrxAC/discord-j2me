package com.gtrxac.discord;

import java.util.*;
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

        // Load last read message IDs from RMS (convert JSON to hashtable)
        try {
            RecordStore rms = RecordStore.openRecordStore("unread", true);

            if (rms.getNumRecords() >= 1) {
                JSONArray json = JSON.getArray(new String(rms.getRecord(1)));

                for (int i = 0; i < json.size(); i++) {
                    JSONArray elem = json.getArray(i);
                    // Convert base-36 string -> long -> decimal string
                    long key = Long.parseLong(elem.getString(0), Character.MAX_RADIX);
                    long value = Long.parseLong(elem.getString(1), Character.MAX_RADIX);
                    String keyStr = String.valueOf(key);
                    String valStr = String.valueOf(value);
                    channels.put(keyStr, valStr);
                }
            }
            Util.closeRecordStore(rms);
        }
        catch (RecordStoreNotFoundException e) {}
        catch (Exception e) { App.error(e); }
    }

    public static void save() {
//#ifdef OVER_100KB
        if (
            (!App.isLiteProxy
//#ifdef PROXYLESS_SUPPORT
            || Settings.proxyless
//#endif
            ) && UnreadManager.markReadChannelID != null
        ) {
            new HTTPThread(HTTPThread.MARK_AS_READ).start();
        }
//#endif

        JSONArray json = new JSONArray();
        
        // Convert hashtable to JSON array of key/value pairs
        for (Enumeration e = channels.keys(); e.hasMoreElements();) {
            JSONArray elem = new JSONArray();
            String key = (String) e.nextElement();
            String value = (String) channels.get(key);

            // Convert decimal string -> long -> base-36 string
            elem.add(Long.toString(Long.parseLong(key), Character.MAX_RADIX));
            elem.add(Long.toString(Long.parseLong(value), Character.MAX_RADIX));
            json.add(elem);
        }

        // Write stringified JSON to RMS
        try {
            RecordStore rms = RecordStore.openRecordStore("unread", true);
            byte[] data = json.build().getBytes();
            Util.setOrAddRecord(rms, 1, data);
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
            put(channelID, String.valueOf(lastMessageTime));
            return false;
        }

        return Long.parseLong(lastReadTime) < lastMessageTime;
    }

    public static void markRead(String channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;
        String lastReadTime = (String) channels.get(channelID);
        boolean isUnread = lastReadTime == null || Long.parseLong(lastReadTime) < lastMessageTime;

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

    public static void markDMsRead() {
        if (App.dmSelector != null) markRead(App.dmSelector.lastDMs);
    }

    public static void markRead(Vector v) {
        autoSave = false;
        for (int i = 0; i < v.size(); i++) {
            HasUnreads ch = (HasUnreads) v.elementAt(i);
            ch.markRead();
        }
        autoSave = true;
        save();
    }
}
