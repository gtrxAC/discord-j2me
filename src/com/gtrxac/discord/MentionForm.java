package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;
import cc.nnproject.json.*;

public class MentionForm extends Form implements CommandListener {
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
        super("Insert mention");
        this.s = s;
        lastScreen = s.disp.getCurrent();
        setCommandListener(this);

        searchField = new TextField("Enter username", "", 32, 0);
        searchField.setInitialInputMode("MIDP_LOWERCASE_LATIN");

        resultsGroup = new ChoiceGroup("Search results", ChoiceGroup.EXCLUSIVE);

        insertCommand = new Command("Insert", Command.OK, 0);
        searchCommand = new Command("Search", Command.OK, 1);
        backCommand = new Command("Back", Command.BACK, 2);
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
            append(new StringItem(null, "Loading"));
            removeCommand(insertCommand);
        }
        else if (searchResults != null) {
            if (searchResults.size() == 0) {
                append(new StringItem(null, "No results found"));
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

    private void insertMention(User u) {
        if (lastScreen instanceof MessageBox) {
            MessageBox msgBox = (MessageBox) lastScreen;
            String str = msgBox.getString();
            int caret = msgBox.getCaretPosition();
            msgBox.setString(str.substring(0, caret) + "<@" + u.id + ">" + str.substring(caret));
        }
        else if (lastScreen instanceof ReplyForm) {
            TextField field = ((ReplyForm) lastScreen).replyField;
            String str = field.getString();
            int caret = field.getCaretPosition();
            field.setString(str.substring(0, caret) + "<@" + u.id + ">" + str.substring(caret));
        }
        s.disp.setCurrent(lastScreen);
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
                s.error("User not selected");
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