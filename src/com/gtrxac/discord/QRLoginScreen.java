package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;
import cc.nnproject.json.*;

//#ifdef MODERNCONNECTOR
import tech.alicesworld.ModernConnector.*;
import java.security.*;
import java.math.*;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.encoders.*;
import org.bouncycastle.crypto.digests.*;
//#endif

public class QRLoginScreen extends Form implements Runnable, CommandListener, Strings {
    private Object lastScreen;
    private SocketConnection sc;
    private OutputStream os;
    private InputStream is;
    private boolean running;
    private boolean gotToken;

    public static volatile String authID;
    private Command checkCommand;

//#ifdef MODERNCONNECTOR
    WebSocketClient ws;
//#endif

    public QRLoginScreen() {
        super(Locale.get(LOGIN_FORM_TITLE));
        lastScreen = App.disp.getCurrent();

        authID = null;

        append(Locale.get(QR_LOGIN_CONNECTING));
        new Thread(this).start();

        addCommand(Locale.createCommand(BACK, Command.BACK, 0));
        setCommandListener(this);

        checkCommand = Locale.createCommand(CHECK, Command.OK, 0);
    }

    private void disconnect() {
        running = false;
		try { is.close(); } catch (Exception e) {}
		try { os.close(); } catch (Exception e) {}
		try { sc.close(); } catch (Exception e) {}
    }

//#ifdef MODERNCONNECTOR
    private void disconnectProxyless() {
        running = false;
		try { ws.close(); } catch (Exception e) {}
    }
//#endif

    private void exit() {
        disconnect();
//#ifdef MODERNCONNECTOR
        disconnectProxyless();
//#endif
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
        running = true;

//#ifdef MODERNCONNECTOR
        if (Settings.proxyless) {
            try {
                qrLoginSocketProxyless();
            }
            catch (Exception e) {
                if (running) {
                    e.printStackTrace();
                    disconnectProxyless();
                    tryQrLoginWithProxy();
                }
            }
        } else
//#endif
        tryQrLoginWithProxy();
    }

    private void tryQrLoginWithProxy() {
        try {
            qrLoginSocket();
        }
        catch (Exception e) {
            if (running) {
                e.printStackTrace();
                disconnect();

                try {
                    qrLoginHttp();
                }
                catch (Exception ee) {
                    App.error(ee, lastScreen);
                }
            }
        }
    }

//#ifdef MODERNCONNECTOR
private void SEND(String d) throws IOException {
    System.out.println("sending: " + d);
    ws.sendMessage(d);
}

    private void qrLoginSocketProxyless() throws Exception {
        ws = (WebSocketClient) ModernConnector.open("wss://remote-auth-gateway.discord.gg/?v=2");

        deleteAll();
        append(Locale.get(QR_LOGIN_RETRIEVING) + " proxyless");

        AsymmetricCipherKeyPair keyPair = null;
        OAEPEncoding cipher = new OAEPEncoding(
            new RSAEngine(),
            new SHA256Digest(),
            new SHA256Digest(),
            null);

        while (true) {
            try {
                String message = ws.receiveMessageString();
                System.out.println("received: " + message);

                // if (message.trim().length() == 0) return;

                JSONObject messageJson = JSON.getObject(message);
                String op = messageJson.getString("op");

                if ("hello".equals(op)) {
                    deleteAll();
                    append("Creating keys...");

                    RSAKeyPairGenerator generator = new RSAKeyPairGenerator();

                    generator.init(new RSAKeyGenerationParameters(
                        BigInteger.valueOf(0x10001), // public exponent 65537
                        new SecureRandom(),
                        2048,
                        80
                    ));

                    keyPair = generator.generateKeyPair();
                    cipher.init(false, keyPair.getPrivate());

                    // TODO: start heartbeat thread

                    // TODO: set disconnect timeout

                    SubjectPublicKeyInfo spki =
                        SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(keyPair.getPublic());

                    byte[] pubKey = spki.getEncoded();
                    String pubKeyBase64 = Util.bytesToString(Base64.encode(pubKey));

                    deleteAll();
                    append("Sending encryption key...");

                    JSONObject msg = new JSONObject();
                    msg.put("op", "init");
                    msg.put("encoded_public_key", pubKeyBase64);
                    SEND(msg.build());

                    deleteAll();
                    append("Waiting for response...");
                }
                else if ("nonce_proof".equals(op)) {
                    deleteAll();
                    append("Decrypting handshake message...");

                    String encNonceBase64 = messageJson.getString("encrypted_nonce");
                    byte[] encNonceBytes = Base64.decode(Util.stringToBytes(encNonceBase64));
                    byte[] nonceBytes = cipher.processBlock(encNonceBytes, 0, encNonceBytes.length);
                    String nonce = Util.bytesToString(UrlBase64.encode(nonceBytes));

                    // Bouncy Castle Base64URL adds periods at the end for padding, Discord does not accept them so remove them
                    while (nonce.endsWith(".")) {
                        nonce = nonce.substring(0, nonce.length() - 1);
                    }

                    deleteAll();
                    append("Sending handshake message...");

                    JSONObject msg = new JSONObject();
                    msg.put("op", "nonce_proof");
                    msg.put("nonce", nonce);
                    SEND(msg.build());

                    deleteAll();
                    append("Waiting for response...");
                }
                else if ("pending_remote_init".equals(op)) {
                    deleteAll();
                    append("Loading QR code...");

                    String qrFingerprint = messageJson.getString("fingerprint");
                    Image qrImage = HTTP.getImage(Settings.api + "/api/v9/qr/" + qrFingerprint);
                    int[] newSize = Util.resizeFit(qrImage.getWidth(), qrImage.getHeight(), getWidth(), getHeight()*9/10);
                    qrImage = Util.resizeImageBilinear(qrImage, newSize[0], newSize[1]);

                    deleteAll();
                    append(qrImage);
                }
                else if ("pending_login".equals(op)) {
                    JSONObject msg = new JSONObject();
                    msg.put("ticket", messageJson.getString("ticket"));

                    byte[] encTokenBase64Bytes = null;
                    try {
                        encTokenBase64Bytes = HTTP.request(
                            "POST", "https://discord.com/api/v9/users/@me/remote-auth/login",
                            msg.build(), null, false);
                    }
                    catch (Exception e) {
                        App.error(e, lastScreen);
                        ws.close();
                        return;
                    }

                    String encTokenBase64 = Util.bytesToString(encTokenBase64Bytes);
                    byte[] encToken = Base64.decode(encTokenBase64);
                    String token = Util.bytesToString(cipher.processBlock(encToken, 0, encToken.length));

                    gotToken = true;
                    Settings.token = messageJson.getString("d");
                    exit();
                }
                else if ("cancel".equals(op)) {
                    ws.close();
                    break;
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
//#endif

    private void qrLoginSocket() throws Exception {
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

    private void qrLoginHttp() throws Exception {
        Image qrImage = HTTP.getImage(Settings.api + "/api/v9/qr/init");
        int[] newSize = Util.resizeFit(qrImage.getWidth(), qrImage.getHeight(), getWidth(), getHeight()*9/10);
        qrImage = Util.resizeImageBilinear(qrImage, newSize[0], newSize[1]);

        if (authID == null) {
            throw new Exception(Locale.get(QR_LOGIN_NOT_RECEIVED));
        }

        deleteAll();
        append(qrImage);
        append(new Spacer(getWidth(), 1));
        append(Locale.get(QR_LOGIN_CHECK_HELP));
        addCommand(checkCommand);
    }

    public void tokenCallback() {
        gotToken = true;
        exit();
    }
}