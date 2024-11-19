package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.util.*;
import java.io.*;
import cc.nnproject.json.*;

public class Locale {
    public static final String[] langIds = {
        "de", "en", "en-US", "es", "fi", "hr", "id", "it", "pl", "pt", "pt-BR", "ro", "ru", "sv", "th", "tr", "uk", "vi", "zh-TW", "yue"
    };

    private static Vector strs;
    private static Vector defaultStrs;

    static {
        // English strings always loaded by default (in case translations are incomplete or have null values)
        try {
            String data = readFile("/en.json");
            defaultStrs = JSON.getArray(data).toVector();
        }
        catch (Exception e) {}
    }

    /**
     * Reads a file's contents from the JAR into a string.
     * @param name File name
     * @return String representation of the file's entire contents (UTF-8)
     * @throws Exception Failed to open file, e.g. it doesn't exist
     */
    private static String readFile(String name) throws Exception {
        InputStream is = new Object().getClass().getResourceAsStream(name);
        DataInputStream dis = new DataInputStream(is);
        StringBuffer buf = new StringBuffer();

        int ch;
        while ((ch = dis.read()) != -1) {
            buf.append((char) ch);
        }

        String result = buf.toString();
        try {
            return new String(result.getBytes("ISO-8859-1"), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return result;
        }
    }

    /**
     * Loads strings for the specified language ID
     * @param s Discord J2ME state object
     * @param id Language ID to load strings for
     * @return Vector of strings for the specified language; null if language ID doesn't exist;
     *   English strings if language is not downloaded (in which case the language gets downloaded and languageLoaded is run after download is complete)
     */
    private static Vector loadLanguage(State s, String id) {
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
        Vector result = defaultStrs;
        
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
                HTTPThread h = new HTTPThread(s, HTTPThread.FETCH_LANGUAGE);
                h.langID = id;
                h.start();
                // return English strings - they will be replaced by the requested language's strings when those get loaded
            }
        }
        catch (Exception e) {}

        try {
            langRms.closeRecordStore();
        }
        catch (Exception e) {}
        
        return result;
    }

    private static void setOrAddRecord(RecordStore rms, int index, String data) throws Exception {
        byte[] bytes = Util.stringToBytes(data);
        if (rms.getNumRecords() >= index) {
            rms.setRecord(index, bytes, 0, bytes.length);
        } else {
            rms.addRecord(bytes, 0, bytes.length);
        }
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
            setOrAddRecord(langRms, 1, langId);
            setOrAddRecord(langRms, 2, jsonData);
        }
        catch (Exception e) {}

        try {
            langRms.closeRecordStore();
        }
        catch (Exception e) {}

        strs = JSON.getArray(jsonData).toVector();
    }

    /**
     * Set current UI language
     * @param s Discord J2ME State object (s.language contains the language ID)
     */
    static void setLanguage(State s) {
        strs = null;

        // unknown locale = use English (don't load extra language data)
        if (s.language == null) return;

        // selected language is English (but not en-US) = don't load extra language data
        String shortName = s.language.substring(0, 2);
        if ("en".equals(shortName) && !"en-US".equals(s.language)) return;

        // try full language code (e.g. en-US.json)
        strs = loadLanguage(s, s.language);

        if (strs == null) {
            // try short language code (e.g. en.json)
            strs = loadLanguage(s, shortName);
        }
        if (strs == null) {
            // no translation available = fallback to English
            return;
        }
    }

    /**
     * Get string in current language based on a string ID.
     * @param id Index number of string to get
     * @return String in currently selected language; if not found, English string
     */
    static String get(int id) {
        // If English is in use or translation doesn't have this string index, use English string
        if (strs == null || id >= strs.size()) {
            return (String) defaultStrs.elementAt(id);
        }

        // Get translation string. If translation is null (json_null which is a blank object), fallback to English string.
        Object result = strs.elementAt(id);
        if (!(result instanceof String)) {
            return (String) defaultStrs.elementAt(id);
        }
        return (String) result;
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