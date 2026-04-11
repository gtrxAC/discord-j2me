package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;
import cc.nnproject.json.*;

public class QRLoginScreen extends Form implements Runnable, CommandListener, Strings {
    public static volatile String authID;
    private Displayable lastScreen;
    private Command checkCommand;

    public QRLoginScreen() {
        super(Locale.get(LOGIN_FORM_TITLE));
        lastScreen = App.disp.getCurrent();

        authID = null;

        append(Locale.get(QR_LOGIN_RETRIEVING));
        new Thread(this).start();

        addCommand(Locale.createCommand(BACK, Command.BACK, 0));
        setCommandListener(this);

        checkCommand = new Command("Check", Command.OK, 0);//Locale.createCommand(BACK, Command.BACK, 0));
    }

    public void exit() {
        App.disp.setCurrent(lastScreen);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == checkCommand) {
            new HTTPThread(HTTPThread.HTTP_QR_LOGIN_CHECK).start();
        } else {
            exit();
        }
    }

    public void run() {
        try {
            qrLoginHttp();
        }
        catch (Exception ee) {
            App.error(ee, lastScreen);
        }
    }

    private void qrLoginHttp() throws Exception {
        Image qrImage = HTTP.getImage(Settings.api + "/api/v9/qr/init");
        int[] newSize = Util.resizeFit(qrImage.getWidth(), qrImage.getHeight(), getWidth(), getHeight()*9/10);
        qrImage = Util.resizeImageBilinear(qrImage, newSize[0], newSize[1]);

        if (authID == null) {
            throw new Exception("Login session not received");
        }

        deleteAll();
        append(qrImage);
        append(new Spacer(getWidth(), 1));
        append("Select 'Check' after you've authorized the login on your smartphone.");
        addCommand(checkCommand);
    }
}