package com.gtrxac.discord;

import cc.nnproject.json.*;
import java.util.*;

public class Message {
    public String id;
    public String author;
    public String recipient;
    public String content;
    public Vector attachments;
	public Vector stickers;
	public long timestamp;

    public Message(JSONObject data) {
        id = data.getString("id");
        timestamp = parseTimestamp(data.getNullableString("timestamp"));
        author = data.getObject("author").getString("global_name", "(no name)");
        if (author == null) {
            author = data.getObject("author").getString("username", "(no name)");
        }
        content = data.getString("content", "(no content)");
        if (content.length() == 0) content = "(no content)";

        try {
            recipient = data
                .getObject("referenced_message")
                .getObject("author")
                .getString("global_name", "(no name)");

            if (recipient == null) {
                recipient = data
                    .getObject("referenced_message")
                    .getObject("author")
                    .getString("username", "(no name)");
            }
        }
        catch (Exception e) {}

        try {
        	JSONArray attachments = data.getArray("attachments");
        	if(attachments != null && attachments.size() > 0) {
	        	this.attachments = new Vector();
	        	for(int i = 0; i < attachments.size(); i++) {
	        		this.attachments.addElement(attachments.getObject(i));
	        	}
        	}
        }
        catch (Exception e) {}

        try {
            JSONArray stickers = data.getArray("sticker_items");
        	if(stickers != null && stickers.size() > 0) {
	        	this.stickers = new Vector();
	        	for(int i = 0; i < stickers.size(); i++) {
	        		this.stickers.addElement(stickers.getObject(i));
	        	}
        	}
        }
        catch (Exception e) {}
    }

    public static void fetchMessages(State s) throws Exception {
        String id;
        if (s.isDM) id = s.selectedDmChannel.id;
        else id = s.selectedChannel.id;
        
        JSONArray messages = JSON.getArray(s.http.get("/channels/" + id + "/messages?limit=20"));
        s.messages = new Vector();

        for (int i = 0; i < messages.size(); i++) {
            s.messages.addElement(new Message(messages.getObject(i)));
        }
    }

    public static void send(State s, String message) throws Exception {
        String id;
        if (s.isDM) id = s.selectedDmChannel.id;
        else id = s.selectedChannel.id;

        JSONObject json = new JSONObject();
        json.put("content", message);
        json.put("flags", 0);
        json.put("mobile_network_type", "unknown");
        json.put("tts", false);

        s.http.post("/channels/" + id + "/messages", json.build());
    }
    
    // date utils
    
    static long parseTimestamp(String date) {
    	Calendar c = Calendar.getInstance();
		if(date.indexOf('T') != -1) {
			String[] dateSplit = split(date.substring(0, date.indexOf('T')), '-');
			String[] timeSplit = split(date.substring(date.indexOf('T')+1), ':');
			String second = split(timeSplit[2], '.')[0];
			int i = second.indexOf('+');
			if(i == -1) {
				i = second.indexOf('-');
			}
			if(i != -1) {
				second = second.substring(0, i);
			}
			c.set(Calendar.YEAR, Integer.parseInt(dateSplit[0]));
			c.set(Calendar.MONTH, Integer.parseInt(dateSplit[1])-1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[2]));
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSplit[0]));
			c.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]));
			c.set(Calendar.SECOND, Integer.parseInt(second));
		} else {
			String[] dateSplit = split(date, '-');
			c.set(Calendar.YEAR, Integer.parseInt(dateSplit[0]));
			c.set(Calendar.MONTH, Integer.parseInt(dateSplit[1])-1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[2]));
		}
		return c.getTime().getTime() + c.getTimeZone().getRawOffset() - parseTimeZone(date);
	}
    
    static int parseTimeZone(String date) {
		int i = date.lastIndexOf('+');
		boolean m = false;
		if(i == -1) {
			i = date.lastIndexOf('-');
			m = true;
		}
		if(i == -1)
			return 0;
		date = date.substring(i + 1);
		int offset = date.lastIndexOf(':');
		offset = (Integer.parseInt(date.substring(0, offset)) * 3600000) +
				(Integer.parseInt(date.substring(offset + 1)) * 60000);
		return m ? -offset : offset;
	}
	
	static String[] split(String str, char d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + 1);
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}
}
