package com.gtrxac.discord;

import java.util.*;
import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class DMSearchForm extends Form implements CommandListener, Strings {
    private TextField textField;
    private Command okCommand;
    private Command backCommand;

    public DMSearchForm() {
        super(Locale.get(DM_SEARCH_TITLE));
        setCommandListener(this);

        textField = new TextField(Locale.get(ENTER_USERNAME), "", 32, 0);
        textField.setInitialInputMode("MIDP_LOWERCASE_LATIN");
        okCommand = Locale.createCommand(OK, Command.OK, 0);
        backCommand = Locale.createCommand(BACK, Command.BACK, 1);

        append(textField);
        addCommand(okCommand);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            App.openDMSelector(false, false);
        }
        if (c == okCommand) {
            String query = textField.getString().toLowerCase();

            for (int i = 0; i < App.dmChannels.size(); i++) {
                DMChannel ch = (DMChannel) App.dmChannels.elementAt(i);
                if (!ch.name.toLowerCase().equals(query) && !query.equals(ch.username)) continue;

                App.isDM = true;
                App.selectedDmChannel = ch;
                App.openChannelView(true);
                return;
            }

            App.error(Locale.get(DM_SEARCH_FAILED));
        }
    }
}