package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Embed {
    public String title;
    public String description;
    // ifdef OVER_100KB
    public FormattedString titleFormatted;
    public FormattedString descFormatted;
    // else
    public String[] titleLines;
    public String[] descLines;
    // endif

    public Embed(JSONObject data) {
        title = data.getString("title", null);
        description = data.getString("description", null);
    }

    public int getHeight(int messageFontHeight) {
        // Padding (1/3 top, 1/3 bottom)
        int result = messageFontHeight*2/3;

        // Content lines
        // ifdef OVER_100KB
        if (title != null && titleFormatted != null) result += titleFormatted.height;
        if (description != null && descFormatted != null) result += descFormatted.height;
        // else
        if (title != null && titleLines != null) result += messageFontHeight*titleLines.length;
        if (description != null && descLines != null) result += messageFontHeight*descLines.length;
        // endif

        // Spacing between title and description
        if (title != null && description != null) result += messageFontHeight/4;

        return result;
    }
}