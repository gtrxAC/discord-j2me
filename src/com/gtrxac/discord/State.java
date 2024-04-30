package com.gtrxac.discord;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class State {
	MIDlet midlet;
	Display disp;
	Font smallFont;

	HTTPThing http;

	Vector guilds;
	Guild selectedGuild;
	GuildSelector guildSelector;

	Vector channels;
	Channel selectedChannel;
	ChannelSelector channelSelector;

	Vector messages;

	public State() {
		smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	}
}
