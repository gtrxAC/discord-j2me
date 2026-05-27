//#ifdef PASSWORD_LOGIN
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;
import cc.nnproject.json.*;

// Reference:
// - https://docs.discord.food/authentication
// - https://github.com/Ayeris23/Discord-Classic/blob/master/src/Classes/API/DCLoginManager.m
// - inspecting Discord Web traffic during a login (devtools network tab)

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
    private Command backCommand;
    private Command continueCommand;

    public PasswordLoginScreen() {
        super(Locale.get(LOGIN_FORM_TITLE));
        lastScreen = App.disp.getCurrent();

        if (lastScreen instanceof Dialog) {
            lastScreen = ((Dialog) lastScreen).lastScreen;
        }

        append("Password login is currently EXPERIMENTAL. We have tried to ensure it is safe, but use it at your own risk. An alt account is strongly recommended.\n" + Locale.get(PASSWORD_LOGIN_MFA_INFO));
        emailField = new TextField(Locale.get(EMAIL_FIELD), Settings.passwordLoginLastEmail, 100, TextField.EMAILADDR);
        passwordField = new TextField(Locale.get(PASSWORD_FIELD), "", 100, TextField.PASSWORD);
        append(emailField);
        append(passwordField);

        loginCommand = Locale.createCommand(LOG_IN, Command.OK, 0);
        backCommand = Locale.createCommand(CANCEL, Command.BACK, 0);
        addCommand(loginCommand);
        addCommand(backCommand);

        setCommandListener(this);
    }

    private boolean totpIsValid() {
        String totp = totpField.getString();

        // totp code format: 123456
        if (totp.length() == 6) {
            try {
                Integer.parseInt(totp);
                return true;
            }
            catch (Exception e) {}
        }
        return false;
    }

    public void commandAction(Command c, Displayable d) {
        if (c == loginCommand) {
            email = emailField.getString();
            if (email.indexOf("@") == -1) {
                App.error(Locale.get(PASSWORD_LOGIN_ERROR_NO_EMAIL));
                return;
            }

            Settings.passwordLoginLastEmail = email;
            Settings.save();

            password = passwordField.getString();
            if (password.length() == 0) {
                App.error(Locale.get(PASSWORD_LOGIN_ERROR_NO_PASS));
                return;
            }

            new Thread(this).start();
        }
        else if (c == continueCommand) {
            if (totpIsValid()) {
                haveEnteredTotp = true;
            } else {
                App.error(Locale.get(PASSWORD_LOGIN_ERROR_INVALID_TOTP));
            }
        }
        else {
            App.disp.setCurrent(lastScreen);
        }
    }

    public void run() {
        try {
            removeCommand(loginCommand);
            removeCommand(backCommand);
            deleteAll();
            append(Locale.get(PASSWORD_LOGIN_STEP_INITIATING));

            // could also use /auth/fingerprint but it seems to be deprecated
            JSONObject experiments = JSON.getObject(Util.bytesToString(HTTP.request(
                "GET", "https://discord.com/api/v9/experiments?with_guild_experiments=true",
                null, null, false)));

            fingerprint = experiments.getString("fingerprint", "");
            // System.out.println("Fingerprint: '" + fingerprint + "'");

            deleteAll();
            append(Locale.get(PASSWORD_LOGIN_STEP_SENDING_REQUEST));

            JSONObject loginObject = new JSONObject();
            loginObject.put("login", email);
            loginObject.put("password", password);
            loginObject.put("undelete", false);
            loginObject.put("login_source", (Object) null);
            loginObject.put("gift_code_sku_id", (Object) null);

            // NOTE: X-Fingerprint header is sent in HTTP.java
            // Error handling (e.g. captcha or wrong password) is also handled there
            JSONObject loginResponse = JSON.getObject(Util.bytesToString(HTTP.request(
                "POST", "https://discord.com/api/v9/auth/login", loginObject.build(), null, false)));

            String token = loginResponse.getString("token", null);
            if (token != null) {
                gotToken(token);
                return;
            }

            if (!loginResponse.getBoolean("mfa")) {
                throw new Exception(Locale.get(PASSWORD_LOGIN_ERROR_NO_MFA));
            }
            if (!loginResponse.getBoolean("totp")) {
                throw new Exception(Locale.get(PASSWORD_LOGIN_ERROR_NO_TOTP));
            }

            ticket = loginResponse.getString("ticket");
            loginInstanceID = loginResponse.getString("login_instance_id");

            deleteAll();
            append(Locale.get(PASSWORD_LOGIN_STEP_ENTER_MFA));

            totpField = new TextField(Locale.get(MFA_CODE_FIELD), "", 6, TextField.NUMERIC);
            append(totpField);

            continueCommand = Locale.createCommand(CONTINUE, Command.OK, 0);
            addCommand(continueCommand);
            addCommand(backCommand);

            // wait for main thread to indicate that the totp code has been entered
            while (!haveEnteredTotp) {
                Util.sleep(100);
            }

            removeCommand(continueCommand);
            removeCommand(backCommand);

            deleteAll();
            append(Locale.get(PASSWORD_LOGIN_STEP_SENDING_MFA));

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