package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;
import cc.nnproject.json.*;

public class MentionForm extends Form implements CommandListener, Strings {
    private State s;
    private Displayable lastScreen;
    private Vector searchResults;
    private boolean loading;

    private Command searchCommand;
    private Command insertCommand;
    private Command backCommand;

    private TextField searchField;
    private ChoiceGroup resultsGroup;

    MentionForm(State s) {
        super(Locale.get(MENTION_FORM_TITLE));
        this.s = s;
        lastScreen = s.disp.getCurrent();
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
            insertMention(new User(s, data.getObject(0).getObject("user")));
            return;
        }
        searchResults = new Vector();

        for (int i = 0; i < data.size(); i++) {
            searchResults.addElement(new User(s, data.getObject(i).getObject("user")));
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
    public static void insertTextToMessageBox(State s, Displayable lastScreen, String text) {
        if (lastScreen instanceof MessageBox) {
            MessageBox msgBox = (MessageBox) lastScreen;
            String str = msgBox.getString();
            int caret = msgBox.getCaretPosition();
            msgBox.setString(str.substring(0, caret) + text + str.substring(caret));
        }
        else if (lastScreen instanceof ReplyForm) {
            TextField field = ((ReplyForm) lastScreen).replyField;
            String str = field.getString();
            int caret = field.getCaretPosition();
            field.setString(str.substring(0, caret) + text + str.substring(caret));
        }
        s.disp.setCurrent(lastScreen);
    }

    private void insertMention(User u) {
        insertTextToMessageBox(s, lastScreen, "<@" + u.id + ">");
    }

    public void commandAction(Command c, Displayable d) {
        if (c == searchCommand) {
            JSONObject reqData = new JSONObject();
            reqData.put("guild_id", s.selectedGuild.id);
            reqData.put("query", searchField.getString());
            reqData.put("limit", 10);

            JSONObject msg = new JSONObject();
            msg.put("op", 8);
            msg.put("d", reqData);
            s.gateway.send(msg);

            loading = true;
            update();
        }
        if (c == insertCommand) {
            int selIndex = resultsGroup.getSelectedIndex();
            if (selIndex == -1) {
                s.error(Locale.get(USER_NOT_SELECTED));
                return;
            }
            
            User selected = (User) searchResults.elementAt(selIndex);
            insertMention(selected);
        }
        else if (c == backCommand) {
            s.disp.setCurrent(lastScreen);
        }
    }
}