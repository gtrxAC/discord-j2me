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
        // ifdef OVER_100KB
        try {
            if (!s.useFilePreview) throw new Exception();
            // Try to show image preview (fails for non-image files)
            s.disp.setCurrent(new ImagePreviewScreen(s, recipientMsg, selected, fc));
        }
        catch (Exception e) {
        // endif
            // File is probably not an image, or viewing it is unsupported by the OS, or the user disabled file previews.
            // Attach the file directly without previewing, and show the appropriate message text entry screen (normal message box or reply form).
            s.disp.setCurrent(createTextEntryScreen(s, recipientMsg, selected, fc));
        // ifdef OVER_100KB
        }
        catch (OutOfMemoryError e) {
            s.error(Locale.get(PREVIEW_NO_MEMORY), createTextEntryScreen(s, recipientMsg, selected, fc));
        }
        // endif
    }

    protected void close() {
        s.openChannelView(false);
    }
    
    public static Displayable createTextEntryScreen(State s, Message recipientMsg, String fileName, FileConnection fc) {
        if (recipientMsg != null) {
            return new ReplyForm(s, recipientMsg, fileName, fc);
        } else {
            return new MessageBox(s, fileName, fc);
        }
    }
}
