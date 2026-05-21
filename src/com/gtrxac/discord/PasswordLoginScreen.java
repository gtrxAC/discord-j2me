//#ifdef PASSWORD_LOGIN
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;
import cc.nnproject.json.*;

public class PasswordLoginScreen extends Form implements Runnable, CommandListener, Strings {
    private Object lastScreen;

    TextField emailField;
    TextField passwordField;
    TextField totpField;
    String email;
    String password;
    static String fingerprint;
    String ticket;
    String loginInstanceID;
    boolean haveEnteredTotp;

    private Command loginCommand;
    private Command continueCommand;

    public PasswordLoginScreen() {
        super(Locale.get(LOGIN_FORM_TITLE));
        lastScreen = App.disp.getCurrent();

        append("To use password login, you must have multi-factor authentication (authenticator app) enabled on your account.");
        emailField = new TextField("Email", Settings.passwordLoginLastEmail, 100, TextField.EMAILADDR);
        passwordField = new TextField("Password", "", 100, TextField.PASSWORD);
        append(emailField);
        append(passwordField);

        addCommand(Locale.createCommand(BACK, Command.BACK, 0));
        loginCommand = Locale.createCommand(LOG_IN, Command.OK, 0);
        addCommand(loginCommand);

        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == loginCommand) {
            email = emailField.getString();
            Settings.passwordLoginLastEmail = email;
            Settings.save();

            password = passwordField.getString();

            new Thread(this).start();
        }
        else if (c == continueCommand) {
            haveEnteredTotp = true;
        }
        else {
            App.disp.setCurrent(lastScreen);
        }
    }

    public void run() {
        try {
            deleteAll();
            append("Initiating login...");

            //String method, String url, Object data, String contentType, boolean authorize
            JSONObject experiments = JSON.getObject(Util.bytesToString(HTTP.request(
                "GET", "https://discord.com/api/v9/experiments?with_guild_experiments=true",
                null, null, false)));

            fingerprint = experiments.getString("fingerprint", "");
            System.out.println("Fingerprint: '" + fingerprint + "'");

            // {"login":"EMAIL","password":"PASSWORD","undelete":false,"login_source":null,"gift_code_sku_id":null}

            deleteAll();
            append("Sending login request...");

            JSONObject loginObject = new JSONObject();
            loginObject.put("login", email);
            loginObject.put("password", password);
            loginObject.put("undelete", false);
            loginObject.put("login_source", (Object) null);
            loginObject.put("gift_code_sku_id", (Object) null);

            JSONObject loginResponse = JSON.getObject(Util.bytesToString(HTTP.request(
                "POST", "https://discord.com/api/v9/auth/login", loginObject.build(), null, false)));

            String token = loginResponse.getString("token", null);
            if (token != null) {
                gotToken(token);
                return;
            }

            if (!loginResponse.getBoolean("mfa")) {
                throw new Exception("Multi-factor authentication is not enabled for this account.");
            }
            if (!loginResponse.getBoolean("totp")) {
                throw new Exception("Multi-factor authentication via authenticator app is not enabled for this account.");
            }

            ticket = loginResponse.getString("ticket");
            loginInstanceID = loginResponse.getString("login_instance_id");

            deleteAll();
            append("Enter the 6-digit code from your authenticator app (e.g. Google Authenticator).");

            totpField = new TextField("Authentication code", "", 6, TextField.NUMERIC);
            append(totpField);

            continueCommand = new Command("Continue", Command.OK, 0);//Locale.createCommand(CONTINUE, Command.OK, 0);
            addCommand(continueCommand);
            removeCommand(loginCommand);

            while (!haveEnteredTotp) {
                Util.sleep(100);
            }

            deleteAll();
            append("Sending authentication code...");

            String totp = totpField.getString();

            JSONObject totpObject = new JSONObject();
            totpObject.put("code", totp);
            totpObject.put("ticket", ticket);
            totpObject.put("login_source", (Object) null);
            totpObject.put("gift_code_sku_id", (Object) null);
            totpObject.put("login_instance_id", loginInstanceID);

            JSONObject totpResponse = JSON.getObject(Util.bytesToString(HTTP.request(
                "POST", "https://discord.com/api/v9/auth/mfa/totp", totpObject.build(), null, false)));

            token = totpResponse.getString("token");
            gotToken(token);
        }
        catch (Exception e) {
            e.printStackTrace();
            App.error(e, lastScreen);
        }
    }

    public void gotToken(String token) {
        Settings.token = token;
        Settings.save();
        App.disp.setCurrent(lastScreen);
    }
}
//#endif