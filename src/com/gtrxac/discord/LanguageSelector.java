package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class LanguageSelector extends ListScreen implements CommandListener, Strings {
    private State s;
    private Displayable lastScreen;

    // Should match the order that is in Locale.langIds
    private static String[] langNames = {
        "Български",
        "Deutsch",
        "English (UK)",      // en
        "English (US)",
        "Español",
        "Suomi",        // fi (Finnish)
        "Français",
        "Hrvatski",
        "Bahasa Indonesia", // id
        "Italiano",     // it (Italian)
        "Polski",       // pl (Polish)
        "Português",
        "Português (Brasil)", // pt_BR (Portuguese - Brazil)
        "Română",       // ro (Romanian)
        "Русский",      // ru (Russian)
        "Svenska",      // sv (Swedish)
        "ไทย",
        "Türkçe",       // tr (Turkish)
        "Українська",   // uk (Ukrainian)
        "Tiếng Việt",    // vi (Vietnamese)
        "繁體中文", // zh_TW (Mandarin - Taiwan)
        "廣東話" // yue (Cantonese)
    };

    public LanguageSelector(State s) {
        super(Locale.get(LANGUAGE_SELECTOR_TITLE), List.IMPLICIT);
        setCommandListener(this);
        this.s = s;
        lastScreen = s.disp.getCurrent();

        Image[] flags = {
            s.ic.flagBG, s.ic.flagDE, s.ic.flagGB, s.ic.flagUS, s.ic.flagES, s.ic.flagFI, s.ic.flagFR, s.ic.flagHR, s.ic.flagID,
            s.ic.flagIT, s.ic.flagPL, s.ic.flagPT, s.ic.flagBR, s.ic.flagRO,
            s.ic.flagRU, s.ic.flagSV, s.ic.flagTH, s.ic.flagTR,
            s.ic.flagUK, s.ic.flagVI, s.ic.flagTW, s.ic.flagHK
        };

        for (int i = 0; i < Locale.langIds.length; i++) {
            append(langNames[i], flags[i]);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == SELECT_COMMAND) {
            s.language = Locale.langIds[getSelectedIndex()];
            Locale.setLanguage(s);
            LoginSettings.save(s);

            // Clear servers/DMs (so the lists get refreshed, which in turn updates the softkey labels)
            s.guilds = null;
            s.dmChannels = null;
        }
        s.disp.setCurrent(lastScreen);
    }
}
