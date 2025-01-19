// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;

public class AttachmentPicker extends FilePicker implements Strings {
    private Message recipientMsg;

    public AttachmentPicker(State s, Message recipientMsg) {
        this(s, recipientMsg, "file:///");
    }

    protected AttachmentPicker(State s, Message recipientMsg, String currentPath) {
        super(s, Locale.get(ATTACHMENT_PICKER_TITLE), currentPath);
        this.recipientMsg = recipientMsg;
    }

    protected void directorySelected(String selectedPath) {
        s.disp.setCurrent(new AttachmentPicker(s, recipientMsg, selectedPath));
    }
    
    protected void fileSelected(FileConnection fc, String selected) {
        try {
            if (!s.useFilePreview) throw new Exception();
            // Try to show image preview (fails for non-image files)
            s.disp.setCurrent(new ImagePreviewScreen(s, recipientMsg, selected, fc));
        }
        catch (Exception e) {
            // File is probably not an image, or viewing it is unsupported by the OS, or the user disabled file previews.
            // Attach the file directly without previewing, and show the appropriate message text entry screen (normal message box or reply form).
            s.disp.setCurrent(s.createTextEntryScreen(recipientMsg, selected, fc));
        }
        catch (OutOfMemoryError e) {
            s.error(Locale.get(PREVIEW_NO_MEMORY), s.createTextEntryScreen(recipientMsg, selected, fc));
        }
    }

    protected void close() {
        s.openChannelView(false);
    }
}
// endif