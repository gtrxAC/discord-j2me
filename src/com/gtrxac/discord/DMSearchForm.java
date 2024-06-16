package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class DMSearchForm extends Form implements CommandListener {
    State s;

    private TextField textField;
    private Command okCommand;
    private Command backCommand;

    public DMSearchForm(State s) {
        super("Search DMs");
        setCommandListener(this);
        this.s = s;

        textField = new TextField("Enter username", "", 32, 0);
        textField.setInitialInputMode("MIDP_LOWERCASE_LATIN");
        okCommand = new Command("OK", Command.OK, 0);
        backCommand = new Command("Back", Command.BACK, 1);

        append(textField);
        addCommand(okCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            s.openDMSelector(false);
        }
        if (c == okCommand) {
            for (int i = 0; i < s.dmChannels.size(); i++) {
                DMChannel ch = (DMChannel) s.dmChannels.elementAt(i);
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