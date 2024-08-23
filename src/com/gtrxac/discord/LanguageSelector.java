package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class LanguageSelector extends List implements CommandListener, Strings {
    State s;
    private Command backCommand;
    private Displayable lastScreen;

    private static String[] langIds = {
        "en", "fi", "id", "it", "pl", "pt_BR", "ro", "ru", "sv", "tr", "uk", "vi"
    };

    private static String[] langNames = {
        "English",      // en
        "Suomi",        // fi (Finnish)
        "Bahasa Indonesia", // id
        "Italiano",     // it (Italian)
        "Polski",       // pl (Polish)
        "Português (Brasil)", // pt_BR (Portuguese - Brazil)
        "Română",       // ro (Romanian)
        "Русский",      // ru (Russian)
        "Svenska",      // sv (Swedish)
        "Türkçe",       // tr (Turkish)
        "Українська",   // uk (Ukrainian)
        "Tiếng Việt"    // vi (Vietnamese)
    };

    public LanguageSelector(State s) {
        super(Locale.get(LANGUAGE_SELECTOR_TITLE), List.IMPLICIT);
        setCommandListener(this);
        this.s = s;
        lastScreen = s.disp.getCurrent();

        Image[] flags = {
            s.ic.flagUS, s.ic.flagFI, s.ic.flagID, s.ic.flagIT,
            s.ic.flagPL, s.ic.flagBR, s.ic.flagRO, s.ic.flagRU,
            s.ic.flagSV, s.ic.flagTR, s.ic.flagUK, s.ic.flagVI
        };

        for (int i = 0; i < langIds.length; i++) {
            append(langNames[i], flags[i]);
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