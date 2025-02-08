package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class KeyMapper extends MyCanvas implements CommandListener, Strings {
    static final int HOTKEY_COUNT =
        // ifdef OVER_100KB
        8;
        // else
        6;
        // endif

    private State s;
    private int fontHeight;
    private int curHotkey;

    private Displayable lastScreen;

    private Command cancelCommand;
    private Command skipCommand;

    private String[] hotkeyStrings;

    private int[] newHotkeys;

    KeyMapper(State s) {
        this.s = s;
        setCommandListener(this);
        
        fontHeight = s.messageFont.getHeight();
        newHotkeys = new int[HOTKEY_COUNT];
        lastScreen = s.disp.getCurrent();
        
        cancelCommand = Locale.createCommand(CANCEL, Command.BACK, 0);
        skipCommand = Locale.createCommand(SKIP, Command.ITEM, 1);
        addCommand(cancelCommand);
        addCommand(skipCommand);

        hotkeyStrings = new String[HOTKEY_COUNT];
        hotkeyStrings[0] = Locale.get(HOTKEY_SEND);
        hotkeyStrings[1] = Locale.get(HOTKEY_REPLY);
        hotkeyStrings[2] = Locale.get(HOTKEY_COPY);
        hotkeyStrings[3] = Locale.get(HOTKEY_REFRESH);
        hotkeyStrings[4] = Locale.get(HOTKEY_BACK);
        hotkeyStrings[5] = Locale.get(HOTKEY_FULLSCREEN);
        // ifdef OVER_100KB
        hotkeyStrings[6] = Locale.get(HOTKEY_SCROLL_TOP);
        hotkeyStrings[7] = Locale.get(HOTKEY_SCROLL_BOTTOM);
        // endif
    }
    
    protected void paint(Graphics g) {
        // BlackBerry fix
        // ifdef BLACKBERRY
        g.setClip(0, 0, getWidth(), getHeight());
        // endif

        // Clear screen
        g.setColor(ChannelView.backgroundColors[s.theme]);
        g.fillRect(0, 0, getWidth(), getHeight());

        String prompt = Locale.get(KEY_MAPPER_PROMPT) + "\n" + hotkeyStrings[curHotkey];
        String[] promptLines = Util.wordWrap(prompt, getWidth(), s.messageFont);
        
        g.setFont(s.messageFont);
        g.setColor(ChannelView.messageColors[s.theme]);

        int y = (getHeight() - fontHeight*promptLines.length)/2;
        
        for (int i = 0; i < promptLines.length; i++) {
            g.drawString(promptLines[i], getWidth()/2, y, Graphics.TOP | Graphics.HCENTER);
            y += fontHeight;
        }
    }
    
    private void mapKey(int keycode) {
        // Map this key to the new action and go to next action
        newHotkeys[curHotkey] = keycode;
        curHotkey++;

        // If all actions have been mapped, save and close
        if (curHotkey == HOTKEY_COUNT) {
	        s.sendHotkey = newHotkeys[0];
	        s.replyHotkey = newHotkeys[1];
	        s.copyHotkey = newHotkeys[2];
	        s.refreshHotkey = newHotkeys[3];
	        s.backHotkey = newHotkeys[4];
            s.fullscreenHotkey = newHotkeys[5];
            // ifdef OVER_100KB
            s.scrollTopHotkey = newHotkeys[6];
            s.scrollBottomHotkey = newHotkeys[7];
            // endif
            LoginSettings.save(s);
            s.disp.setCurrent(lastScreen);
        } else {
            repaint();
        }
    }
    
    protected void keyPressed(int keycode) {
        // Check if key already mapped to another action
        for (int i = 0; i < curHotkey; i++) {
            if (newHotkeys[i] == keycode) {
                s.error(Locale.get(KEY_MAPPER_DUPLICATE) + hotkeyStrings[i]);
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