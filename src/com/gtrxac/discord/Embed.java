package com.gtrxac.discord;

import cc.nnproject.json.*;

public class Embed {
    public String title;
    public String description;
    public FormattedString titleFormatted;
    public FormattedString descFormatted;

    public Embed(JSONObject data) {
        title = data.getString("title", null);
        description = data.getString("description", null);
    }

    public int getHeight(int messageFontHeight) {
        // Padding (1/3 top, 1/3 bottom)
        int result = messageFontHeight*2/3;

        // Content lines
        if (title != null && titleFormatted != null) result += titleFormatted.height;
        if (description != null && descFormatted != null) result += descFormatted.height;

        // Spacing between title and description
        if (title != null && description != null) result += messageFontHeight/4;

        return result;
    }
}