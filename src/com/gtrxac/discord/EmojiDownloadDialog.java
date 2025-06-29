// ifdef EMOJI_SUPPORT
package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class EmojiDownloadDialog extends Dialog implements Strings, CommandListener {
    Displayable lastScreen;
    private Command yesCommand;
    private Command noCommand;

    EmojiDownloadDialog() {
        super(null, Locale.get(EMOJI_DOWNLOAD_PROMPT));
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            new HTTPThread(HTTPThread.FETCH_EMOJIS).start();
        }
        else if (c == noCommand) {
            App.disp.setCurrent(lastScreen);
        }
    }
}
// endif