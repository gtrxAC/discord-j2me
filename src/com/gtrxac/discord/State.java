package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class State {
	public static final long DISCORD_EPOCH = 1420070400000L;

	MIDlet midlet;
	Display disp;
	Font smallFont;
	Font smallBoldFont;

	HTTPThing http;

	Vector guilds;
	Guild selectedGuild;
	GuildSelector guildSelector;

	Vector channels;
	Channel selectedChannel;
	ChannelSelector channelSelector;

	Vector messages;
	ChannelView channelView;

	boolean isDM;
	DMChannel selectedDmChannel;

	public State() {
		smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		smallBoldFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
	}

	public void error(String message) {
		Alert error = new Alert("Error");
		error.setString(message);
		disp.setCurrent(error);
	}

	public void openGuildSelector(boolean reload) {
		try {
			if (reload || guildSelector == null) {
				guildSelector = new GuildSelector(this);
			}
			disp.setCurrent(guildSelector);
		}
		catch (Exception e) {
			error(e.toString());
		}
	}

	public void openChannelSelector(boolean reload) {
		try {
			if (reload || channelSelector == null) {
				channelSelector = new ChannelSelector(this);
			}
			disp.setCurrent(channelSelector);
		}
		catch (Exception e) {
			error(e.toString());
		}
	}

	public void openChannelView(boolean reload) {
		try {
			if (reload || channelView == null) {
				channelView = new ChannelView(this);
			}
			disp.setCurrent(channelView);
		}
		catch (Exception e) {
			error(e.toString());
		}
	}
}
