package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;
import cc.nnproject.json.*;

public class MentionForm extends Form implements CommandListener, Strings {
    private Displayable lastScreen;
    private Vector searchResults;
    private boolean loading;

    private Command searchCommand;
    private Command insertCommand;
    private Command backCommand;

    private TextField searchField;
    private ChoiceGroup resultsGroup;

    MentionForm() {
        super(Locale.get(MENTION_FORM_TITLE));
        lastScreen = App.disp.getCurrent();
        setCommandListener(this);

        searchField = new TextField(Locale.get(ENTER_USERNAME), "", 32, 0);
        searchField.setInitialInputMode("MIDP_LOWERCASE_LATIN");

        resultsGroup = new ChoiceGroup(Locale.get(SEARCH_RESULTS), ChoiceGroup.EXCLUSIVE);

        insertCommand = Locale.createCommand(INSERT, Command.OK, 0);
        searchCommand = Locale.createCommand(SEARCH, Command.OK, 1);
        backCommand = Locale.createCommand(BACK, Command.BACK, 2);
        addCommand(searchCommand);
        addCommand(backCommand);

        update();
    }

    public void searchCallback(JSONArray data) {
        if (data.size() == 1) {
            insertMention(new User(data.getObject(0).getObject("user")));
            return;
        }
        searchResults = new Vector();

        for (int i = 0; i < data.size(); i++) {
            searchResults.addElement(new User(data.getObject(i).getObject("user")));
        }

        loading = false;
        update();
    }

    private void update() {
        deleteAll();
        append(searchField);

        if (loading) {
            append(new StringItem(null, Locale.get(LOADING)));
            removeCommand(insertCommand);
        }
        else if (searchResults != null) {
            if (searchResults.size() == 0) {
                append(new StringItem(null, Locale.get(NO_RESULTS)));
                removeCommand(insertCommand);
            } else {
                append(resultsGroup);
                addCommand(insertCommand);

                for (int i = 0; i < searchResults.size(); i++) {
                    User u = (User) searchResults.elementAt(i);
                    resultsGroup.append(u.name, null);
                }
            }
        }
    }

    // also used by emoji picker
    public static int insertTextToMessageBox(Displayable lastScreen, String text, int caretPos) {
        MessageBox msgBox = null;
        TextField field = null;
        String oldStr = null;

        if (lastScreen instanceof MessageBox) {
            msgBox = (MessageBox) lastScreen;
            oldStr = msgBox.getString();
            if (caretPos == -1) caretPos = msgBox.getCaretPosition();
        } else {
            field = ((ReplyForm) lastScreen).replyField;
            oldStr = field.getString();
            if (caretPos == -1) caretPos = field.getCaretPosition();
        }

        // Prevent string index error (seen on some Sony Ericssons)
        if (caretPos < 0) caretPos = 0;
        else if (caretPos > oldStr.length) caretPos = oldStr.length;

        String beginSpace =
            (caretPos > 0 && " ([{".indexOf(oldStr.charAt(caretPos - 1)) == -1)
            ? " " : "";

        String endSpace =
            (caretPos == oldStr.length() || " )]}".indexOf(oldStr.charAt(caretPos)) == -1)
            ? " " : "";

        String addedText = beginSpace + text + endSpace;
        String newStr = oldStr.substring(0, caretPos) + addedText + oldStr.substring(caretPos);

        if (lastScreen instanceof MessageBox) {
            msgBox.setString(newStr);
        } else {
            field.setString(newStr);
        }

        return caretPos + addedText.length();
    }

    private void insertMention(User u) {
        insertTextToMessageBox(lastScreen, "<@" + u.id + ">", -1);
        App.disp.setCurrent(lastScreen);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == searchCommand) {
            JSONObject reqData = new JSONObject();
            reqData.put("guild_id", App.selectedGuild.id);
            reqData.put("query", searchField.getString());
            reqData.put("limit", 10);

            JSONObject msg = new JSONObject();
            msg.put("op", 8);
            msg.put("d", reqData);
            App.gateway.send(msg);

            loading = true;
            update();
        }
        else if (c == insertCommand) {
            int selIndex = resultsGroup.getSelectedIndex();
            if (selIndex == -1) {
                App.error(Locale.get(USER_NOT_SELECTED));
                return;
            }
            
            User selected = (User) searchResults.elementAt(selIndex);
            insertMention(selected);
        }
        else if (c == backCommand) {
            App.disp.setCurrent(lastScreen);
        }
    }
}