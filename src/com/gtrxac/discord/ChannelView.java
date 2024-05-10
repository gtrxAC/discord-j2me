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

    boolean outdated;
    int scroll;
    int maxScroll;
    int pressY;
    // int selectedMessage;

    int messageFontHeight;
    int authorFontHeight;

    //                                            Dark        Light       Black
    public static final int[] backgroundColors = {0x00313338, 0x00FFFFFF, 0x00000000};
    public static final int[] messageColors =    {0x00FFFFFF, 0x00111111, 0x00EEEEEE};
    public static final int[] authorColors =     {0x00FFFFFF, 0x00000000, 0x00FFFFFF};
    public static final int[] timestampColors =  {0x00AAAAAA, 0x00888888, 0x00999999};

    public ChannelView(State s) throws Exception {
        super();

        setCommandListener(this);
        this.s = s;
        messageFontHeight = s.messageFont.getHeight();
        authorFontHeight = s.authorFont.getHeight();

        backCommand = new Command("Back", Command.BACK, 0);
        sendCommand = new Command("Send", "Send message", Command.ITEM, 0);
        refreshCommand = new Command("Refresh", Command.ITEM, 1);
        olderCommand = new Command("Older", "View older messages", Command.ITEM, 2);
        newerCommand = new Command("Newer", "View newer messages", Command.ITEM, 3);

        addCommand(backCommand);
        addCommand(sendCommand);
        addCommand(refreshCommand);

        getMessages();
    }

    public void update() {
        maxScroll = 0;
        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            msg.contentLines = WordWrap.getStringArray(msg.content, getWidth(), s.messageFont);
            maxScroll += getMessageHeight(msg);
        }

        if (s.messages.size() > 0 && page > 0) addCommand(newerCommand);
        else removeCommand(newerCommand);

        if (s.messages.size() > 0) addCommand(olderCommand);
        else removeCommand(olderCommand);

        maxScroll -= getHeight();

        // If user selected Show newer messages, go to the top of the
        // message list, so it's more intuitive to scroll through
        scroll = (after != null) ? 0 : maxScroll;

        repaint();
    }

    public void getMessages() throws Exception {
        if (s.isDM) {
            if (s.selectedDmChannel.isGroup) {
                setTitle(s.selectedDmChannel.name);
            } else {
                setTitle("@" + s.selectedDmChannel.name);
            }
        } else {
            setTitle("#" + s.selectedChannel.name);
        }

        if (page > 0) setTitle(getTitle() + " (old)");

        Message.fetchMessages(s, before, after);
        update();
    }

    /**
     * @return amount of vertical pixels taken up by the message
     */
    public int getMessageHeight(Message msg) {
        // Each content line + one line for message author + little bit of spacing between messages
        return messageFontHeight*msg.contentLines.length + authorFontHeight + messageFontHeight/4;
    }

    public void drawMessage(Graphics g, Message msg, int y) {
        // Draw author (and recipient if applicable)
        g.setColor(authorColors[s.theme]);
        g.setFont(s.authorFont);
        String authorStr = msg.author + (msg.recipient != null ? (" -> " + msg.recipient) : "");
        g.drawString(authorStr, 1, y, Graphics.TOP|Graphics.LEFT);

        // Draw timestamp
        g.setColor(timestampColors[s.theme]);
        g.setFont(s.timestampFont);
        g.drawString(
            "  " + msg.timestamp, 1 + s.authorFont.stringWidth(authorStr), y,
            Graphics.TOP|Graphics.LEFT
        );
        y += authorFontHeight;

        // Draw message content
        g.setColor(messageColors[s.theme]);
        g.setFont(s.messageFont);
        for (int i = 0; i < msg.contentLines.length; i++) {
            g.drawString(msg.contentLines[i], 1, y, Graphics.TOP|Graphics.LEFT);
            y += messageFontHeight;
        }
    }

    protected void paint(Graphics g) {
        g.setFont(s.messageFont);
        g.setColor(backgroundColors[s.theme]);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (s.messages.size() == 0) {
            g.setColor(timestampColors[s.theme]);
            g.drawString("Nothing to see here", getWidth()/2, getHeight()/2, Graphics.HCENTER|Graphics.VCENTER);
            return;
        }

        int y = -scroll;
        for (int i = s.messages.size() - 1; i >= 0; i--) {
            Message msg = (Message) s.messages.elementAt(i);
            int msgHeight = getMessageHeight(msg);
            if (y > getHeight()) break;
            
            if (y + msgHeight >= 0) {
                // highlight selected message
                // if (i == selectedMessage) {
                //     g.setColor(highlightColors[s.theme]);
                //     g.fillRect(0, y, getWidth(), msgHeight);
                // }
                drawMessage(g, msg, y);
            }
            y += msgHeight;
        }

        if (outdated) {
            g.setFont(s.messageFont);
            String[] lines = WordWrap.getStringArray("Refresh to read new messages", getWidth(), s.messageFont);
            g.setColor(0x00AA1122);
            g.fillRect(0, 0, getWidth(), messageFontHeight*lines.length + 2);

            g.setColor(0x00FFFFFF);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(lines[i], getWidth()/2, i*messageFontHeight + 1, Graphics.TOP|Graphics.HCENTER);
            }
        }
    }
    
    private void keyEvent(int keycode) {
        int action = getGameAction(keycode);

        if (action == Canvas.UP) {
            scroll -= messageFontHeight*2;
            if (scroll < 0) scroll = 0;
            repaint();
        }
        else if (action == Canvas.DOWN) {
            scroll += messageFontHeight*2;
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
            if (s.isDM) s.openDMSelector(false);
            else s.openChannelSelector(false);
        }
        if (c == sendCommand) {
            s.disp.setCurrent(new MessageBox(s));
        }
        if (c == refreshCommand) {
            s.openChannelView(true);
        }
        if (c == olderCommand) {
            page++;
            after = null;
            before = ((Message) s.messages.elementAt(s.messages.size() - 1)).id;
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