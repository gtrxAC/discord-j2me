// ifdef OVER_100KB
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.io.*;

public class ImagePreviewScreen extends MyCanvas implements CommandListener, Strings {
    private State s;
    private FileConnection fc;
    private Image img;
    private String fileName;
    private Message recipientMsg;
    private Command yesCommand;
    private Command noCommand;
    private Displayable prevScreen;
    private int width;
    private int height;
    private int fontHeight;

    ImagePreviewScreen(State s, Message recipientMsg, String fileName, FileConnection fc) throws Exception {
        super();
        setTitle(fileName);
        setCommandListener(this);
        this.s = s;
        this.fc = fc;
        this.fileName = fileName;
        this.recipientMsg = recipientMsg;
        prevScreen = s.disp.getCurrent();

        InputStream is = fc.openInputStream();
        Image imgFull;
        try {
            imgFull = Image.createImage(is);
        } finally {
            is.close();
        }

        width = getWidth();
        height = getHeight();
        fontHeight = s.messageFont.getHeight();
        int[] size = Util.resizeFit(imgFull.getWidth(), imgFull.getHeight(), width, height - fontHeight*3/2);
        img = Util.resizeImage(imgFull, size[0], size[1]);
        imgFull = null;

        yesCommand = Locale.createCommand(YES, Command.OK, 0);
        noCommand = Locale.createCommand(NO, Command.BACK, 1);
        addCommand(yesCommand);
        addCommand(noCommand);
    }

    protected void sizeChanged(int w, int h) {
        width = w;
        height = h;
        repaint();
    }

    protected void paint(Graphics g) {
        // BlackBerry fix
        // ifdef BLACKBERRY
        g.setClip(0, 0, width, height);
        // endif

        g.setColor(ChannelView.backgroundColors[s.theme]);
        g.fillRect(0, 0, width, height);

        g.setFont(s.messageFont);
        g.setColor(ChannelView.messageColors[s.theme]);
        g.drawString(Locale.get(IMAGE_PREVIEW_PROMPT), width/2, fontHeight/4, Graphics.TOP | Graphics.HCENTER);

        g.drawImage(img, width/2, (height - fontHeight*3/2)/2 + fontHeight*3/2, Graphics.HCENTER | Graphics.VCENTER);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == yesCommand) {
            Displayable screen = AttachmentPicker.createTextEntryScreen(s, recipientMsg, fileName, fc);
            if (screen instanceof MessageBox) {
                ((MessageBox) screen).showedPreviewScreen = true;
            } else {
                ((ReplyForm) screen).showedPreviewScreen = true;
            }
            s.disp.setCurrent(screen);
        } else {
            try {
                fc.close();
            }
            catch (Exception e) {}
            s.disp.setCurrent(prevScreen);
        }
    }
}
// endif