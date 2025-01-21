// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class EmojiDownloadDialog extends Dialog implements Strings, CommandListener {
    Displayable lastScreen;
    private Command yesCommand;
    private Command noCommand;
    private State s;

    EmojiDownloadDialog(State s) {
        super(s.disp, null, Locale.get(EMOJI_DOWNLOAD_PROMPT));
        setCommandListener(this);
        lastScreen = s.disp.getCurrent();
        this.s = s;

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            new HTTPThread(s, HTTPThread.FETCH_EMOJIS).start();
        }
        else if (c == noCommand) {
            s.disp.setCurrent(lastScreen);
        }
    }
}
// endif