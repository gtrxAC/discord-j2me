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

    private static final String[][] fileTypeMapping = {
        { ".aac", "audio/aac", null },
        { ".amr", "audio/amr", "audio/amr-wb" },
        { ".awb", "audio/amr-wb", "audio/amr" },
        { ".m4a", "audio/mp4", null },
        { ".mid", "audio/midi", "audio/mid" },
        { ".mmf", "application/vnd.smaf", null },
        { ".mxmf", "audio/vnd.nokia.mobile-xmf", null },
        { ".mp3", "audio/mpeg", "audio/mpeg3" },
        { ".ogg", "audio/ogg", null },
        { ".wav", "audio/x-wav", "audio/wav" },
        { ".wma", "audio/x-ms-wma", null },
    };

    public static Player playSound(String fileName, InputStream stream) throws Exception {
        String type = "application/octet-stream";
        String altType = null;
        fileName = fileName.toLowerCase();

        for (int i = 0; i < fileTypeMapping.length; i++) {
            if (fileName.endsWith(fileTypeMapping[i][0])) {
                type = fileTypeMapping[i][1];
                altType = fileTypeMapping[i][2];
                break;
            }
        }
        Player player = null;
        try {
            player = Manager.createPlayer(stream, type);
            player.start();
        }
        catch (Exception e) {
            try {
                player = Manager.createPlayer(stream, altType);
                player.start();
            }
            catch (Exception ee) {
                player = Manager.createPlayer(stream, null);
                player.start();
            }
        }
        return player;
    }

    NotificationSoundDialog(String fileName, byte[] soundData) throws Exception {
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

        player = playSound(fileName, new ByteArrayInputStream(soundData));
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
            try {
                player.close();
            }
            catch (Exception e) {
                App.error(e);
            }
            App.disp.setCurrent(App.attachmentView);
        }
    }
}
// endif