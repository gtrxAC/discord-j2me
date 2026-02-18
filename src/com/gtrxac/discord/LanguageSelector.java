package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

public class LanguageSelector extends ListScreen implements CommandListener, Strings {
    private Object lastScreen;

    // Should match the order that is in Locale.langIds
    private static String[] langNames = {
        "العربية",
        "Български",
        "Català", // ca (Catalan)
        "Deutsch",
        "English (UK)",      // en
        "English (US)",
        "Castellano",
        "Suomi",        // fi (Finnish)
        "Français",
        "Hrvatski",
        "Magyar",
        "Bahasa Indonesia", // id
        "Italiano",     // it (Italian)
        "日本語",  // ja (Japanese)
        "Bahasa Melayu",
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
        "繁體中文 (台灣)", // zh_TW (Mandarin - Taiwan)
        "繁體中文（香港）", // zh_HK (Mandarin - Hong Kong)
        "简体中文"        // zh_CN (Mandarin - China)
    };

    public LanguageSelector() {
        super(Locale.get(LANGUAGE_SELECTOR_TITLE), List.IMPLICIT);
        setCommandListener(this);
        lastScreen = App.disp.getCurrent();

        App.ic = null;
        App.ic = new Icons(Icons.TYPE_LANGUAGE);

        Image[] flags = {
            App.ic.flagAR, App.ic.flagBG, App.ic.flagCA, App.ic.flagDE, App.ic.flagGB, App.ic.flagUS, App.ic.flagES, App.ic.flagFI, App.ic.flagFR, App.ic.flagHR, App.ic.flagHU, App.ic.flagID,
            App.ic.flagIT, App.ic.flagJP, App.ic.flagMY, App.ic.flagPL, App.ic.flagPT, App.ic.flagBR, App.ic.flagRO,
            App.ic.flagRU, App.ic.flagSV, App.ic.flagTH, App.ic.flagTR,
            App.ic.flagUK, App.ic.flagVI, App.ic.flagTW, App.ic.flagHK, App.ic.flagCN
        };

        for (int i = 0; i < Locale.langIds.length; i++) {
            append(langNames[i], flags[i]);
        }
    }

    public void commandAction(Command c, Displayable d) {
        App.ic = null;  // don't need to load new icons because the old ones are kept by lastscreen

        if (c == SELECT_COMMAND) {
            Settings.language = Locale.langIds[getSelectedIndex()];
            Locale.setLanguage();
            Settings.save();

            // Clear servers/DMs (so the lists get refreshed, which in turn updates the softkey labels)
            App.guilds = null;
            App.dmChannels = null;
        }
        App.disp.setCurrent(lastScreen);
    }
}
