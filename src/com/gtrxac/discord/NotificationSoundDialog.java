// ifdef OVER_100KB
package com.gtrxac.discord;

import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import javax.microedition.media.*;

public class NotificationSoundDialog extends Dialog implements Strings, CommandListener {
    private Command yesCommand;
    private Command noCommand;
    private Command okCommand;
    private Command replayCommand;

    private byte[] soundData;
    private Player player;
    private String fileName;

    NotificationSoundDialog(String fileName, byte[] soundData) {
        super(null, Locale.get(APPLY_NOTIF_SOUND_PROMPT));
        setCommandListener(this);
        this.lastScreen = lastScreen;
        this.fileName = fileName;
        this.soundData = soundData;

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        replayCommand = Locale.createCommand(PLAY_AGAIN, Command.ITEM, 2);
        addCommand(yesCommand);
        addCommand(noCommand);
        addCommand(replayCommand);

        try {
            player = Manager.createPlayer(new ByteArrayInputStream(soundData), null);
            player.start();
        }
        catch (Exception e) {
            App.error(e);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            RecordStore rms = null;
            try {
                rms = RecordStore.openRecordStore("notifsound", true);
                Util.setOrAddRecord(rms, 1, fileName);
                Util.setOrAddRecord(rms, 2, soundData);

                setString(Locale.get(APPLY_NOTIF_SOUND_SUCCESS));

                removeCommand(yesCommand);
                removeCommand(noCommand);
                removeCommand(replayCommand);
                okCommand = Locale.createCommand(OK, Command.OK, 0);
                addCommand(okCommand);
            }
            catch (Exception e) {
                App.error(e);
            }
            Util.closeRecordStore(rms);
        }
        else if (c == replayCommand) {
            try {
                player.start();
            }
            catch (Exception e) {
                App.error(e);
            }
        }
        else {
            // 'no' command or 'ok' command
            player.close();
            App.disp.setCurrent(App.attachmentView);
        }
    }
}
// endif