package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class LanguageSelector extends ListScreen implements CommandListener, Strings {
    private State s;
    private Displayable lastScreen;

    private static String[] langIds = {
        "en", "es", "fi", "id", "it", "pl", "pt_BR", "ro", "ru", "sv", "th", "tr", "uk", "vi"
    };

    private static String[] langNames = {
        "English",      // en
        "Español",
        "Suomi",        // fi (Finnish)
        "Bahasa Indonesia", // id
        "Italiano",     // it (Italian)
        "Polski",       // pl (Polish)
        "Português (Brasil)", // pt_BR (Portuguese - Brazil)
        "Română",       // ro (Romanian)
        "Русский",      // ru (Russian)
        "Svenska",      // sv (Swedish)
        "ไทย",
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
            s.ic.flagUS, s.ic.flagES, s.ic.flagFI, s.ic.flagID,
            s.ic.flagIT, s.ic.flagPL, s.ic.flagBR, s.ic.flagRO,
            s.ic.flagRU, s.ic.flagSV, s.ic.flagTH, s.ic.flagTR,
            s.ic.flagUK, s.ic.flagVI
        };

        for (int i = 0; i < langIds.length; i++) {
            append(langNames[i], flags[i]);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            s.language = langIds[getSelectedIndex()];
            Locale.setLanguage(s);
            LoginSettings.save(s);
        }
        s.disp.setCurrent(lastScreen);
    }
}