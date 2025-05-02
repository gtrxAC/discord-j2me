package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import cc.nnproject.json.*;
import java.util.*;

/**
 * Static class for save data (RMS) management, i.e. user settings and favorite servers list.
 */
public class Settings {
    private static RecordStore loginRms;
    private static int numRecords;
    private static int index;

    private static void open() throws Exception {
        loginRms = RecordStore.openRecordStore("a", true);
        numRecords = loginRms.getNumRecords();
        index = 1;
    }

    public static boolean isAvailable() {
        try {
            RecordStore.openRecordStore("a", false).closeRecordStore();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private static boolean hasBoldFont() {
        Font normal = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        Font bold = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);

        return (normal != bold) && (normal.isBold() != bold.isBold());
    }

    public static void load() {
        try {
            open();
            App.api = getStringRecord("http://146.59.80.3");
            App.token = getStringRecord("");
            App.authorFontSize = getByteRecord(3);
            App.messageFontSize = getByteRecord(3);
            App.use12hTime = getByteRecord(0) != 0;
            App.messageLoadCount = getByteRecord(15);
            index++; // skip unused record
            // dark theme default for color screens, dedicated monochrome theme default for mono screens
            App.theme = getByteRecord(App.disp.isColor() ? 1 : 0);
            App.listTimestamps = getByteRecord(0) != 0;
            App.markAsRead = getByteRecord(0) != 0;
        }
        catch (Exception e) {
            App.error(e);
        }
        finally {
            try {
                loginRms.closeRecordStore();
            }
            catch (Exception e) {}
        }

        if ("".equals(App.token)) {
            // default setting for token: try to find token from manifest/jad
            String manifestToken = App.instance.getAppProperty("Token");
            if (manifestToken != null) App.token = manifestToken;
        }

        if (App.messageFontSize >= 3)  {
            // default setting for message font size:
            // medium on s40 128x128 and 128x160, small on everything else
            App.messageFontSize = (App.isNokia && App.screenWidth == 128) ? 1 : 0;
        }

        if (App.authorFontSize >= 3) {
            // default setting for author font size:
            // make sure the author font is distinct enough from the message content font
            // if the device supports bold fonts, use the same font size as the message content, else use a larger font for the author
            App.authorFontSize = hasBoldFont() ? App.messageFontSize : (App.messageFontSize + 1);
        }

        if (App.messageLoadCount < 1 || App.messageLoadCount > 100) App.messageLoadCount = 15;
    }

    public static void save() {
        try {
            open();
            setStringRecord(App.api);
            setStringRecord(App.token);
            setByteRecord(App.authorFontSize);
            setByteRecord(App.messageFontSize);
            setByteRecord(App.use12hTime ? 1 : 0);
            setByteRecord(App.messageLoadCount);
            setByteRecord(0);
            setByteRecord(App.theme);
            setByteRecord(App.listTimestamps ? 1 : 0);
            setByteRecord(App.markAsRead ? 1 : 0);
        }
        catch (Exception e) {
            App.error(e);
        }
        finally {
            try {
                loginRms.closeRecordStore();
            }
            catch (Exception e) {}
        }
    }

    private static String getStringRecord(String def) {
        int thisIndex = index;
        index++;
        if (numRecords >= thisIndex) {
            try {
                String result = new String(loginRms.getRecord(thisIndex));
                if (result.length() > 0) return result;
            }
            catch (Exception e) {}
        }
        return def;
    }

    private static int getByteRecord(int def) throws Exception {
        int thisIndex = index;
        index++;
        if (numRecords >= thisIndex) {
            return (int) loginRms.getRecord(thisIndex)[0];
        }
        return def;
    }
    
    private static void setRecord(byte[] value) throws Exception {
        if (numRecords >= index) {
            loginRms.setRecord(index, value, 0, value.length);
        } else {
            loginRms.addRecord(value, 0, value.length);
            numRecords++;
        }
        index++;
    }

    private static void setByteRecord(int value) throws Exception {
        byte[] record = {new Integer(value).byteValue()};
        setRecord(record);
    }

    private static void setStringRecord(String value) throws Exception {
        setRecord(value.getBytes());
    }

    // Favorite servers list management

    private static JSONArray guilds;  // array of favorite guild IDs
    private static boolean hasChanged;  // list of fav guilds has changed (items added/removed)?
    public static final String favLabel;  // "Favorite" or "Favourite"
    public static final String favLabel2;  // "Favorites" or "Favourites"

    static {
        favLabel = ("en-US".equals(System.getProperty("microedition.locale"))) ? "Favorite" : "Favourite";
        favLabel2 = favLabel + "s";

        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("b", false);
        }
        catch (Exception e) {}
        
        try {
            guilds = JSONObject.parseArray(new String(rms.getRecord(1)));
        }
        catch (Exception e) {
            guilds = new JSONArray();
        }

		try {
			rms.closeRecordStore();
		}
		catch (Exception e) {}
    }

    private static void favSave() {
        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("b", true);
            byte[] bytes = guilds.build().getBytes();
            
            if (rms.getNumRecords() >= 1) {
                rms.setRecord(1, bytes, 0, bytes.length);
            } else {
                rms.addRecord(bytes, 0, bytes.length);
            }
        }
        catch (Exception e) {
            App.error(e);
        }

		try {
			rms.closeRecordStore();
		}
		catch (Exception e) {}
    }

    public static void favAdd(DiscordObject g) {
        if (favHas(g)) return;
        JSONArray gJson = new JSONArray();
        gJson.add(g.id);
        gJson.add(g.name);
        guilds.add(gJson);
        hasChanged = true;
        favSave();
    }

    public static void favRemove(int index) {
        guilds.remove(index);
        hasChanged = true;
        favSave();
    }

    public static boolean favEmpty() {
        return guilds.size() == 0;
    }

    public static boolean favHas(DiscordObject g) {
        for (int i = 0; i < guilds.size(); i++) {
            if (guilds.getArray(i).getString(0).equals(g.id)) return true;
        }
        return false;
    }

    public static void favOpenSelector() {
        if (App.guildSelector == null || !App.guildSelector.isFavGuilds || hasChanged) {
            Vector guildsVec = new Vector();

            for (int i = 0; i < guilds.size(); i++) {
                guildsVec.addElement(new DiscordObject(guilds.getArray(i)));
            }

            try {
                App.guildSelector = new GuildSelector(guildsVec, true);
            }
            catch (Exception e) {
                App.error(e);
                return;
            }
        }
        App.disp.setCurrent(App.guildSelector);

        hasChanged = false;
    }
}