package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class State {
	public static final long DISCORD_EPOCH = 1420070400000L;

	MIDlet midlet;
	Display disp;

	int theme;  // 0 = dark, 1 = light, 2 = black
	boolean oldUI;
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
	OldChannelView oldChannelView;

	boolean isDM;
	Vector dmChannels;
	DMChannel selectedDmChannel;
	DMSelector dmSelector;

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

	public void openDMSelector(boolean reload) {
		try {
			if (reload || dmSelector == null) {
				dmSelector = new DMSelector(this);
			}
			disp.setCurrent(dmSelector);
		}
		catch (Exception e) {
			error(e.toString());
		}
	}

	public void openChannelView(boolean reload) {
		try {
			if (oldUI) {
				if (reload || oldChannelView == null) {
					oldChannelView = new OldChannelView(this);
					disp.setCurrent(oldChannelView);
				}
			} else {
				if (reload || channelView == null) {
					channelView = new ChannelView(this);
					disp.setCurrent(channelView);
				}
			}
		}
		catch (Exception e) {
			error(e.toString());
		}
	}
}
