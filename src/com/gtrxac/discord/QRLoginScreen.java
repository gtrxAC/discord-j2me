package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;
import cc.nnproject.json.*;

public class QRLoginScreen extends Form implements Runnable, CommandListener, Strings {
    private Object lastScreen;
    private SocketConnection sc;
    private OutputStream os;
    private InputStream is;
    private boolean running;
    private boolean gotToken;

    public QRLoginScreen() {
        super(Locale.get(LOGIN_FORM_TITLE));
        lastScreen = App.disp.getCurrent();

        append(Locale.get(QR_LOGIN_CONNECTING));
        new Thread(this).start();

        addCommand(Locale.createCommand(BACK, Command.BACK, 0));
        setCommandListener(this);
    }

    private void disconnect() {
        running = false;
		try { is.close(); } catch (Exception e) {}
		try { os.close(); } catch (Exception e) {}
		try { sc.close(); } catch (Exception e) {}
    }

    private void exit() {
        disconnect();
        App.disp.setCurrent(lastScreen);
    }

    public void commandAction(Command c, Displayable d) {
        exit();
    }

    public void run() {
        running = true;

        try {
            sc = (SocketConnection) Connector.open(App.getPlatformSpecificUrl(Settings.gatewayUrl));

            is = sc.openInputStream();
            os = sc.openOutputStream();

            String msgStr = null;
            StringBuffer sb = new StringBuffer();

            deleteAll();
            append(Locale.get(QR_LOGIN_RETRIEVING));

            while (true) {
                // Get message
                while (true) {
                    int ch = is.read();
                    if (ch == '\n' || ch == -1) {
                        if (sb.length() > 0) {
                            // This message has been fully received, start processing it
                            msgStr = new String(sb.toString().getBytes("ISO-8859-1"), "UTF-8");
                            sb = new StringBuffer();
                            break;
                        }
                    } else {
                        sb.append((char) ch);
                    }
                }

                // Process message
                JSONObject message = JSON.getObject(msgStr);
                String op = message.getString("t", "");

                if (op.equals("GATEWAY_HELLO")) {
                    JSONObject msg = new JSONObject();
                    msg.put("op", -1);
                    msg.put("t", "GATEWAY_CONNECT_REMOTEAUTH");
                    GatewayThread.send(os, msg);
                }
                else if (op.equals("qrlogin_code")) {
                    String qrFingerprint = message.getString("d");
                    Image qrImage = HTTP.getImage(Settings.api + "/api/v9/qr/" + qrFingerprint);
                    int[] newSize = Util.resizeFit(qrImage.getWidth(), qrImage.getHeight(), getWidth(), getHeight()*9/10);
                    qrImage = Util.resizeImageBilinear(qrImage, newSize[0], newSize[1]);

                    deleteAll();
                    append(qrImage);
                }
                else if (op.equals("qrlogin_token")) {
                    gotToken = true;
                    Settings.token = message.getString("d");
                    exit();
                }
                else {
                    // it's a disconnect message
                    disconnect();
                    if (!gotToken) {
                        App.error(Locale.get(QR_LOGIN_DISCONNECTED), lastScreen);
                    }
                }
            }
        }
        catch (Exception e) {
            if (running) {
                disconnect();
                App.error(e, lastScreen);
            }
        }
    }
}