// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;

public class AttachmentPicker extends FilePicker implements Strings {
    private Message recipientMsg;

    public AttachmentPicker(Message recipientMsg) {
        this(recipientMsg, "file:///");
    }

    protected AttachmentPicker(Message recipientMsg, String currentPath) {
        super(Locale.get(ATTACHMENT_PICKER_TITLE), currentPath);
        this.recipientMsg = recipientMsg;
    }

    protected void directorySelected(String selectedPath) {
        App.disp.setCurrent(new AttachmentPicker(recipientMsg, selectedPath));
    }
    
    protected void fileSelected(FileConnection fc, String selected) {
        try {
            if (!Settings.useFilePreview) throw new Exception();
            // Try to show image preview (fails for non-image files)
            App.disp.setCurrent(new ImagePreviewScreen(recipientMsg, selected, fc));
        }
        catch (Exception e) {
            // File is probably not an image, or viewing it is unsupported by the OS, or the user disabled file previews.
            // Attach the file directly without previewing, and show the appropriate message text entry screen (normal message box or reply form).
            App.disp.setCurrent(App.createTextEntryScreen(recipientMsg, selected, fc));
        }
        catch (OutOfMemoryError e) {
            App.error(Locale.get(PREVIEW_NO_MEMORY), App.createTextEntryScreen(recipientMsg, selected, fc));
        }
    }

    protected void close() {
        App.openChannelView(false);
    }
}
// endif