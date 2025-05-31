package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.util.*;
import java.io.*;
import cc.nnproject.json.*;

public class Locale {
    public static final String[] langIds = {
        "ar", "bg", "ca", "de", "en", "en-US", "es", "fi", "fr", "hr", "id", "it", "ja", "ms", "pl", "pt", "pt-BR", "ro", "ru", "sv", "th", "tr", "uk", "vi", "zh-TW", "yue"
    };

    private static Vector strs;

    private static Vector getDefaultStrings() {
        try {
            return JSON.getArray(Util.readFile(
                // ifdef NOKIA_128PX
                "/en-compact.json"
                // else
                "/en.json"
                // endif
            )).toVector();
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Vector();
        }
    }

    private static void setStrings(Vector newStrs) {
        Vector defaultStrs = getDefaultStrings();
        if (newStrs == null) {
            strs = defaultStrs;
            return;
        }
        // Fill in any gaps (nulls) in the translation with the default English strings
        for (int i = 0; i < newStrs.size(); i++) {
            if (newStrs.elementAt(i) == JSON.json_null) {
                newStrs.setElementAt(defaultStrs.elementAt(i), i);
            }
        }
        // Fill in missing string indexes with English strings
        for (int i = newStrs.size(); i < defaultStrs.size(); i++) {
            newStrs.addElement(defaultStrs.elementAt(i));
        }
        strs = newStrs;
    }

    /**
     * Loads strings for the specified language ID
     * @param id Language ID to load strings for
     * @return Vector of strings for the specified language; null if language ID doesn't exist;
     *   null if language is not downloaded (in which case the language gets downloaded and languageLoaded is run after download is complete)
     */
    private static Vector loadLanguage(String id) {
        // Check if language exists
        boolean found = false;
        for (int i = 0; i < langIds.length; i++) {
            if (langIds[i].equals(id)) {
                found = true;
                break;
            }
        }
        if (!found) return null;

        RecordStore langRms = null;
        Vector result = null;
        
        try {
            langRms = RecordStore.openRecordStore("lang", true);

            // Get ID of language that is currently loaded in RMS storage
            String currentId = null;
            if (langRms.getNumRecords() == 2) {
                currentId = Util.bytesToString(langRms.getRecord(1));
            }

            if (id.equals(currentId)) {
                // Requested language is already in RMS: load it from RMS
                String jsonData = Util.bytesToString(langRms.getRecord(2));
                result = JSON.getArray(jsonData).toVector();
            } else {
                // Requested language is not loaded: download it
                HTTPThread h = new HTTPThread(HTTPThread.FETCH_LANGUAGE);
                h.langID = id;
                h.start();
                // return English strings - they will be replaced by the requested language's strings when those get loaded
            }
        }
        catch (Exception e) {}

        Util.closeRecordStore(langRms);
        return result;
    }

    /**
     * Callback for when language data has been downloaded. Used by HTTPThread.
     * @param langId ID of language that was loaded
     * @param jsonData String containing language's JSON data
     */
    static void languageLoaded(String langId, String jsonData) {
        RecordStore langRms = null;
        try {
            langRms = RecordStore.openRecordStore("lang", false);
            Util.setOrAddRecord(langRms, 1, langId);
            Util.setOrAddRecord(langRms, 2, jsonData);
        }
        catch (Exception e) {}
        
        Util.closeRecordStore(langRms);

        setStrings(JSON.getArray(jsonData).toVector());
    }

    /**
     * Set current UI language
     */
    static void setLanguage() {
        // unknown locale = use English (don't load extra language data)
        if (Settings.language == null) {
            setStrings(null);
            return;
        }

        // selected language is English (but not en-US) = don't load extra language data
        String shortName = Settings.language.substring(0, 2);
        if ("en".equals(shortName) && !"en-US".equals(Settings.language)) {
            setStrings(null);
            return;
        }

        // try full language code (e.g. en-US.json)
        Vector newStrs = loadLanguage(Settings.language);

        if (newStrs == null) {
            // try short language code (e.g. en.json)
            newStrs = loadLanguage(shortName);
        }
        setStrings(newStrs);
    }

    /**
     * Get string in current language based on a string ID.
     * @param id Index number of string to get
     * @return String in currently selected language; if not found, English string
     */
    static String get(int id) {
        return (String) strs.elementAt(id);
    }

    /**
     * Creates an LCDUI Command with localized labels based on a string ID.
     * @param id Index number of label string to get (id + 1 is the long label's index)
     * @param type Command type (see J2ME Docs)
     * @param prio Command priority number (see J2ME Docs)
     * @return Command where the label and long label are strings in the currently selected language
     */
    static Command createCommand(int id, int type, int prio) {
        return new Command(get(id), get(id + 1), type, prio);
    }
}
