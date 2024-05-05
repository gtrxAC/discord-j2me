package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Message list for channels (both guild channels and DM channels).
 */
public class ChannelView extends Canvas implements CommandListener {
    State s;
    private Command backCommand;
    private Command sendCommand;
    private Command refreshCommand;
    private Command olderCommand;
    private Command newerCommand;

    // Parameters for viewing old messages (message IDs)
    int page;
    String before;
    String after;

    int scroll;
    int maxScroll;
    int pressY;
    // int selectedMessage;

    int fontHeight;

    public ChannelView(State s) throws Exception {
        super();

        setCommandListener(this);
        this.s = s;
        fontHeight = s.smallFont.getHeight();

        backCommand = new Command("Back", Command.BACK, 0);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        olderCommand = new Command("Older", "View older messages", Command.ITEM, 2);
        newerCommand = new Command("Newer", "View newer messages", Command.ITEM, 3);

        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);
        addCommand(olderCommand);

        getMessages();
    }

    public void getMessages() throws Exception {
        if (s.isDM) setTitle("@" + s.selectedDmChannel.name);
        else setTitle("#" + s.selectedChannel.name);

        if (page > 0) setTitle(getTitle() + " (old)");

        maxScroll = 0;
        Message.fetchMessages(s, before, after);

        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            msg.contentLines = WordWrap.getStringArray(msg.content, getWidth(), s.smallFont);
            maxScroll += getMessageHeight(msg);
        }

        if (page > 0) addCommand(newerCommand);
        else removeCommand(newerCommand);

        maxScroll -= getHeight();

        // If user selected Show newer messages, go to the top of the
        // message list, so it's more intuitive to scroll through
        scroll = (after != null) ? 0 : maxScroll;

        repaint();
    }

    /**
     * @return amount of vertical pixels taken up by the message
     */
    public int getMessageHeight(Message msg) {
        // Each content line + one line for message author + little bit of spacing between messages
        return fontHeight*(msg.contentLines.length + 1) + fontHeight/4;
    }

    public void drawMessage(Graphics g, Message msg, int y) {
        // Draw author (and recipient if applicable)
        g.setColor(s.lightTheme ? 0x00000000 : 0x00FFFFFF);
        g.setFont(s.smallBoldFont);
        String authorStr = msg.author + (msg.recipient != null ? (" -> " + msg.recipient) : "");
        g.drawString(authorStr, 1, y, Graphics.TOP|Graphics.LEFT);

        // Draw timestamp
        g.setColor(0x00888888);
        g.setFont(s.smallFont);
        g.drawString(
            "  " + msg.timestamp, 1 + s.smallBoldFont.stringWidth(authorStr), y,
            Graphics.TOP|Graphics.LEFT
        );
        y += fontHeight;

        // Draw message content
        g.setColor(s.lightTheme ? 0x00111111 : 0x00EEEEEE);
        for (int i = 0; i < msg.contentLines.length; i++) {
            g.drawString(msg.contentLines[i], 1, y, Graphics.TOP|Graphics.LEFT);
            y += fontHeight;
        }
    }

    protected void paint(Graphics g) {
        g.setFont(s.smallFont);
        g.setColor(s.lightTheme ? 0x00FFFFFF : 0x00000000);
        g.fillRect(0, 0, getWidth(), getHeight());

        int y = -scroll;
        for (int i = s.messages.size() - 1; i >= 0; i--) {
            Message msg = (Message) s.messages.elementAt(i);
            int msgHeight = getMessageHeight(msg);
            if (y > getHeight()) break;
            
            if (y + msgHeight >= 0) {
                // highlight selected message
                // if (i == selectedMessage) {
                //     g.setColor(s.lightTheme ? 0x00DDDDDD : 0x00222222);
                //     g.fillRect(0, y, getWidth(), msgHeight);
                // }
                drawMessage(g, msg, y);
            }
            y += msgHeight;
        }
    }
    
    private void keyEvent(int keycode) {
        int action = getGameAction(keycode);

        if (action == Canvas.UP) {
            scroll -= fontHeight*2;
            if (scroll < 0) scroll = 0;
            repaint();
        }
        else if (action == Canvas.DOWN) {
            scroll += fontHeight*2;
            if (scroll > maxScroll) scroll = maxScroll;
            repaint();
        }
    }
    protected void keyPressed(int a) { keyEvent(a); }
    protected void keyRepeated(int a) { keyEvent(a); }

    protected void pointerPressed(int x, int y) {
        pressY = y;
    }
    protected void pointerRel(int x, int y) {
        scroll -= (y - pressY);
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;
        pressY = y;
        repaint();
    }
    protected void pointerReleased(int x, int y) { pointerRel(x, y); }
    protected void pointerDragged(int x, int y) { pointerRel(x, y); }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            if (s.isDM) s.disp.setCurrent(new DMSelector(s));
            else s.disp.setCurrent(s.channelSelector);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageForm(s));
        }
        if (c == refreshCommand) {
            s.openChannelView(true);
        }
        if (c == olderCommand) {
            page++;
            after = null;
            before = ((Message) s.messages.elementAt(19)).id;
            try {
                getMessages();
            }
            catch (Exception e) {
                s.error(e.toString());
            }
        }
        if (c == newerCommand) {
            page--;
            before = null;
            after = ((Message) s.messages.elementAt(0)).id;
            try {
                getMessages();
            }
            catch (Exception e) {
                s.error(e.toString());
            }
        }
    }
}