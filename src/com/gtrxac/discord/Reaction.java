//#ifdef EMOJI_SUPPORT
package com.gtrxac.discord;

import java.util.Vector;
import javax.microedition.lcdui.*;

public class Reaction {
    int x;
    int y;
    int width;
    FormattedStringPart[] parts;

    public Reaction(boolean isGuildEmoji, String nameOrId, int count) {
        StringBuffer src = new StringBuffer();

        if (isGuildEmoji) {
            src.append("<:aa:").append(nameOrId).append('>');
        } else {
            src.append(nameOrId);
        }
        src.append("**").append(count).append("**");

        FormattedStringParser parser = new FormattedStringParser(src.toString(), App.messageFont, true, false);
        Vector partsVec = parser.run();
        parts = new FormattedStringPart[partsVec.size()];
        partsVec.copyInto(parts);
    }
}
//#endif