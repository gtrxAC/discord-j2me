package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class KeyMapper extends Canvas implements CommandListener {
    private State s;
    private int messageFontHeight;
    private int curHotkey;

    private Displayable lastScreen;

    private Command cancelCommand;
    private Command skipCommand;

    private static final String[] hotkeyStrings = {
        "sending a message",
        "replying to message",
        "copying message content",
        "refreshing messages",
        "going back"
    };

    private int[] newHotkeys;

    KeyMapper(State s) {
        this.s = s;
        setCommandListener(this);
        
        messageFontHeight = s.messageFont.getHeight();
        newHotkeys = new int[5];
        lastScreen = s.disp.getCurrent();
        
        cancelCommand = new Command("Cancel", Command.BACK, 0);
        skipCommand = new Command("Skip", Command.ITEM, 1);
        addCommand(cancelCommand);
        addCommand(skipCommand);
    }
    
    protected void paint(Graphics g) {
        // BlackBerry fix
        g.setClip(0, 0, getWidth(), getHeight());

        // Clear screen
        g.setColor(ChannelView.backgroundColors[s.theme]);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setFont(s.messageFont);
        g.setColor(ChannelView.messageColors[s.theme]);
        g.drawString(
            "Press the key to use for:",
            getWidth()/2, getHeight()/2 - messageFontHeight,
            Graphics.TOP | Graphics.HCENTER
        );
        g.drawString(
            hotkeyStrings[curHotkey],
            getWidth()/2, getHeight()/2,
            Graphics.TOP | Graphics.HCENTER
        );
    }
    
    private void mapKey(int keycode) {
        // Map this key to the new action and go to next action
        newHotkeys[curHotkey] = keycode;
        curHotkey++;

        // If all actions have been mapped, save and close
        if (curHotkey == 5) {
	        s.sendHotkey = newHotkeys[0];
	        s.replyHotkey = newHotkeys[1];
	        s.copyHotkey = newHotkeys[2];
	        s.refreshHotkey = newHotkeys[3];
	        s.backHotkey = newHotkeys[4];
            ((SettingsForm) lastScreen).saveKeyMappings();
            s.disp.setCurrent(lastScreen);
        } else {
            repaint();
        }
    }
    
    protected void keyPressed(int keycode) {
        // Check if key already mapped to another action
        for (int i = 0; i < curHotkey; i++) {
            if (newHotkeys[i] == keycode) {
                s.error("This key is already mapped to " + hotkeyStrings[i]);
                return;
            }
        }
        mapKey(keycode);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == skipCommand) {
            mapKey(0);
        }
        else if (c == cancelCommand) {
            s.disp.setCurrent(lastScreen);
        }
    }
}