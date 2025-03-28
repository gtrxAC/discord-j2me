package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class KeyMapper extends MyCanvas implements CommandListener, Strings {
    static final int HOTKEY_COUNT =
        // ifdef OVER_100KB
        8;
        // else
        6;
        // endif

    private int fontHeight;
    private int curHotkey;

    private Displayable lastScreen;

    private Command cancelCommand;
    private Command skipCommand;

    private String[] hotkeyStrings;

    private int[] newHotkeys;

    KeyMapper() {
        setCommandListener(this);
        
        fontHeight = App.messageFont.getHeight();
        newHotkeys = new int[HOTKEY_COUNT];
        lastScreen = App.disp.getCurrent();
        
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
        clearScreen(g, Theme.keyMapperBackgroundColor);

        String prompt = Locale.get(KEY_MAPPER_PROMPT) + "\n" + hotkeyStrings[curHotkey];
        String[] promptLines = Util.wordWrap(prompt, getWidth(), App.messageFont);
        
        g.setFont(App.messageFont);
        g.setColor(Theme.keyMapperTextColor);

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
	        Settings.sendHotkey = newHotkeys[0];
	        Settings.replyHotkey = newHotkeys[1];
	        Settings.copyHotkey = newHotkeys[2];
	        Settings.refreshHotkey = newHotkeys[3];
	        Settings.backHotkey = newHotkeys[4];
            Settings.fullscreenHotkey = newHotkeys[5];
            // ifdef OVER_100KB
            Settings.scrollTopHotkey = newHotkeys[6];
            Settings.scrollBottomHotkey = newHotkeys[7];
            // endif

            Settings.defaultHotkeys = false;
            ((SettingsScreen) lastScreen).values[2][4] = 0;
            ((SettingsScreen) lastScreen).updateMenuItem(4);

            Settings.save();
            App.disp.setCurrent(lastScreen);
        } else {
            repaint();
        }
    }
    
    protected void keyPressed(int keycode) {
        // Check if key already mapped to another action
        for (int i = 0; i < curHotkey; i++) {
            if (newHotkeys[i] == keycode) {
                App.error(Locale.get(KEY_MAPPER_DUPLICATE) + hotkeyStrings[i]);
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
            App.disp.setCurrent(lastScreen);
        }
    }
}