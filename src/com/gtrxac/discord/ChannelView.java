package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends Form implements CommandListener {
    State s;
    private Command backCommand;
    private Command sendCommand;
    private Command refreshCommand;

    public ChannelView(State s) {
        super("");
        if (s.isDM) setTitle("@" + s.selectedDmChannel.name);
        else setTitle("#" + s.selectedChannel.name);

        setCommandListener(this);
        this.s = s;

        try {
            Message.fetchMessages(s);
            for (int i = 0; i < s.messages.size(); i++) {
                Message msg = (Message) s.messages.elementAt(i);
        		// TODO date
                StringItem msgItem = new StringItem(
                    msg.author + (msg.recipient != null ? (" -> " + msg.recipient) : ""),
                    msg.content
                );
                msgItem.setFont(s.smallFont);
                msgItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
                append(msgItem);
                
                // Photo attachments
                if (msg.attachments != null) {
                	int size = msg.attachments.size();
                	for (int j = 0; j < size; j++) {
                		JSONObject attachment = (JSONObject) msg.attachments.elementAt(j);
                		String filename = attachment.getNullableString("filename");
                		String url = attachment.getNullableString("url");
                		if(url == null) continue;
                		int k = url.indexOf("https://cdn.discordapp.com");
                		if(k != -1)  {
                			url = "http://cdndsc.uwmpr.online" + url.substring(k + "https://cdn.discordapp.com".length());
                		}
                		Image img = HTTPThing.getImage(url);
                		int ow = img.getWidth();
                		int oh = img.getHeight();
                		// TODO adapt to screen res
                		float ih = ((float) oh / ow) * 120;
                		// TODO async
                		img = ImageUtils.resize(img, (int) (((float) ow / oh) * ih), (int) ih);
                		ImageItem attItem = new ImageItem(
                                filename,
                                img,
                                0,
                                filename
                            );
                		attItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
                        append(attItem);
                	}
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            append("Failed to get messages: " + e.toString());
        }

        backCommand = new Command("Back", Command.BACK, 0);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            if (s.isDM) s.disp.setCurrent(new DMSelector(s));
            else s.disp.setCurrent(s.channelSelector);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageForm(s));
        }
        if (c == refreshCommand) {
            s.channelView = new ChannelView(s);
            s.disp.setCurrent(s.channelView);
        }
    }
}