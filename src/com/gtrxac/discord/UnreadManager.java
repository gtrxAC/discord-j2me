package com.gtrxac.discord;

import java.util.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;

public class UnreadManager {
    private Hashtable channels;
    private State s;
    public boolean autoSave;

    public UnreadManager(State s) {
        this.s = s;
        channels = new Hashtable();
        autoSave = true;

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
            rms.closeRecordStore();
        }
        catch (RecordStoreNotFoundException e) {}
        catch (Exception e) { s.error(e); }
    }

    public void save() {
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
            
            if (rms.getNumRecords() >= 1) {
                rms.setRecord(1, data, 0, data.length);
            } else {
                rms.addRecord(data, 0, data.length);
            }
            rms.closeRecordStore();
        }
        catch (Exception e) { s.error(e); }
    }

    private void put(String channelID, String lastReadTime) {
        if (lastReadTime == null || lastReadTime.equals("0")) return;
        channels.put(channelID, lastReadTime);
        if (autoSave) save();
    }

    public boolean hasUnreads(String channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;

        String lastReadTime = (String) channels.get(channelID);
        if (lastReadTime == null) {
            put(channelID, String.valueOf(lastMessageTime));
            return false;
        }

        return Long.parseLong(lastReadTime) < lastMessageTime;
    }

    public boolean hasUnreads(Channel ch) {
        return hasUnreads(ch.id, ch.lastMessageID);
    }

    public boolean hasUnreads(DMChannel dmCh) {
        return hasUnreads(dmCh.id, dmCh.lastMessageID);
    }

    public void markRead(String channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;
        String lastReadTime = (String) channels.get(channelID);

        if (lastReadTime == null || Long.parseLong(lastReadTime) < lastMessageTime) {
            put(channelID, String.valueOf(lastMessageTime));
        }
    }

    public void markRead(Channel ch) {
        markRead(ch.id, ch.lastMessageID);
    }

    public void markRead(DMChannel dmCh) {
        markRead(dmCh.id, dmCh.lastMessageID);
    }

    public void markRead(Guild g) {
        if (g == null || g.channels == null) return;

        autoSave = false;
        for (int i = 0; i < g.channels.size(); i++) {
            Channel ch = (Channel) g.channels.elementAt(i);
            markRead(ch);
        }
        autoSave = true;
        save();
    }

    public void markDMsRead() {
        if (s.dmSelector == null) return;

        autoSave = false;
        for (int i = 0; i < s.dmSelector.lastDMs.size(); i++) {
            DMChannel dmCh = (DMChannel) s.dmSelector.lastDMs.elementAt(i);
            markRead(dmCh);
        }
        autoSave = true;
        save();
    }
}
