package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;
import cc.nnproject.json.*;

public class Locale {
    private static Vector strs;
    private static Vector defaultStrs;

    static {
        // English strings always loaded by default (in case translations are incomplete or have null values)
        try {
            defaultStrs = loadLanguage("en");
        }
        catch (Exception e) {}
    }

    static void setLanguage(State s) {
        try {
            setLanguage(s.language);
        }
        catch (Exception e) {
            s.error("Failed to load language data");
        }
    }

    static String readFile(String name) throws Exception {
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

    static Vector loadLanguage(String id) throws Exception {
        String data = readFile("/" + id + ".json");
        return JSON.getArray(data).toVector();
    }

    static void setLanguage(String name) {
        strs = null;

        // unknown locale = use English (don't load extra language data)
        if (name == null) return;

        // selected language is English (but not en-US) = don't load extra language data
        String shortName = name.substring(0, 2);
        if ("en".equals(shortName) && !"en-US".equals(name)) return;

        try {
            // try full language code (e.g. en-US.json)
            strs = loadLanguage(name);
        }
        catch (Exception e) {
            try {
                // try short language code (e.g. en.json)
                strs = loadLanguage(shortName);
            }
            catch (Exception ee) {
                // no translation available = fallback to English
                return;
            }
        }
    }

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

    static Command createCommand(int id, int type, int prio) {
        return new Command(get(id), get(id + 1), type, prio);
    }
}