package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class DMSelector extends Form implements CommandListener {
    State s;

    private TextField textField;
    private Command okCommand;
    private Command backCommand;

    public DMSelector(State s) {
        super("Direct Message");
        setCommandListener(this);
        this.s = s;

        textField = new TextField("Enter username", "", 32, 0);
        textField.setInitialInputMode("MIDP_LOWERCASE_LATIN");
        okCommand = new Command("OK", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 0);

        append(textField);
        addCommand(okCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.disp.setCurrent(s.guildSelector);
        }
        if (c == okCommand) {
            Vector channels;
            try {
                channels = DMChannel.fetchDMChannels(s);
            }
            catch (Exception e) {
                e.printStackTrace();
                s.error(e.toString());
                return;
            }

            for (int i = 0; i < channels.size(); i++) {
                DMChannel ch = (DMChannel) channels.elementAt(i);
                if (!ch.name.equals(textField.getString())) continue;

                s.isDM = true;
                s.selectedDmChannel = ch;
                s.openChannelView(true);
                return;
            }

            s.error("User not found. Try creating the DM from another client.");
        }
    }
}