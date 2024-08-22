package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;
import language.*;

public class Locale {
    private static String[] strs;
    private static String[] defaultStrs;

    static {
        // English strings always loaded by default (in case translations are incomplete or have null values)
        defaultStrs = new en().getStrings();
    }

    static void setLanguage(State s) {
        try {
            setLanguage(s.language);
        }
        catch (Exception e) {
            s.error("Failed to load language data");
        }
    }

    static void setLanguage(String name) {
        strs = null;

        // unknown locale = use English (don't load extra language data)
        if (name == null) return;

        // selected language is English?
        String shortName = name.substring(0, 2);
        if ("en".equals(shortName)) return;

        Class langClass;
        try {
            // try full language code (e.g. language.en_US)
            langClass = Class.forName("language." + Util.replace(name, "-", "_"));
        }
        catch (Exception e) {
            try {
                // try short language code (e.g. language.en)
                langClass = Class.forName("language." + shortName);
            }
            catch (Exception ee) {
                // fallback to English
                return;
            }
        }
        try {
            strs = ((Language) langClass.newInstance()).getStrings();
        }
        catch (Exception e) {
            return;
        }
    }

    static String get(int id) {
        // If English is in use or translation doesn't have this string index, use English string
        if (strs == null || id >= strs.length) return defaultStrs[id];

        // Get translation string. If translation is null, fallback to English string.
        String result = strs[id];
        if (result == null) return defaultStrs[id];
        return result;
    }

    static Command createCommand(int id, int type, int prio) {
        return new Command(get(id), get(id + 1), type, prio);
    }
}