package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class LanguageSelector extends List implements CommandListener, Strings {
    State s;
    private Command backCommand;
    private Displayable lastScreen;

    private static String[] langIds = {
        "en", "fi", "id", "it", "pl", "pt_BR", "ru", "sv", "tr", "uk"
    };

    public LanguageSelector(State s) {
        super(Locale.get(LANGUAGE_SELECTOR_TITLE), List.IMPLICIT);
        setCommandListener(this);
        this.s = s;
        lastScreen = s.disp.getCurrent();

        for (int i = 0; i < langIds.length; i++) {
            append(langIds[i], null);
        }

        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        addCommand(backCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            s.language = langIds[getSelectedIndex()];
            Locale.setLanguage(s);
            LoginSettings.save(s);
        }
        s.disp.setCurrent(lastScreen);
    }
}