package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class URLList extends List implements CommandListener {
    public static final String[] urlStarts = {"https://", "http://", "www."};
    private static final String[] urlEnds = {" ", "[", "]", "(", ")", "<", ">", "\"", "'", "\t", "\r", "\n"};

    private State s;
    private Command backCommand;

    public URLList(State s, String content) {
        super("Select URL", List.IMPLICIT);
        this.s = s;
        setCommandListener(this);

        backCommand = new Command("Back", Command.BACK, 0);
        addCommand(backCommand);

        int index = 0;

        while (true) {
            // Find start of next URL. If not found, stop.
            int urlStartIndex = Util.indexOfAny(content, urlStarts, index);
            if (urlStartIndex == -1) break;

            // Find a character that indicates the end of an URL. If not found, the rest of the string is the URL.
            int urlEndIndex = Util.indexOfAny(content, urlEnds, urlStartIndex);
            if (urlEndIndex == -1) {
                urlEndIndex = content.length() - 1;
            }

            // Add this URL
            append(content.substring(urlStartIndex, urlEndIndex), null);

            // If searched through whole string, stop
            if (urlEndIndex >= content.length() - 1) break;

            // Begin next search iteration where we left off
            index = urlEndIndex;
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            s.platformRequest(getString(getSelectedIndex()));
        }
        else if (c == backCommand) {
            s.openChannelView(false);
        }
    }
}