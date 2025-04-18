package com.gtrxac.discord;

import java.io.*;
import javax.microedition.io.*;
import cc.nnproject.json.*;
import javax.microedition.lcdui.Image;

public class HTTP implements Strings {
    public static HttpConnection openConnection(String url) throws IOException {
        String fullUrl = Settings.api + "/api/v9" + url;

        if (Settings.tokenType == Settings.TOKEN_TYPE_QUERY) {
            if (fullUrl.indexOf("?") != -1) {
                fullUrl += "&token=" + Settings.token;
            } else {
                fullUrl += "?token=" + Settings.token;
            }
        }

        HttpConnection c = (HttpConnection) Connector.open(App.getPlatformSpecificUrl(fullUrl));

        if (Settings.tokenType == Settings.TOKEN_TYPE_HEADER) {
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("Authorization", Settings.token);
        }

        return c;
    }

    public static String sendRequest(HttpConnection c) throws Exception {
        InputStream is = null;
        try {
            int respCode = c.getResponseCode();
			is = c.openDataInputStream();
            
            // Read response
            StringBuffer stringBuffer = new StringBuffer();
            int ch;
            while ((ch = is.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            String response = stringBuffer.toString().trim();

            if (respCode == HttpConnection.HTTP_OK) {
                return response;
            }
            if (respCode == HttpConnection.HTTP_UNAUTHORIZED) {
                throw new Exception(Locale.get(HTTP_ERROR_TOKEN));
            }

            try {
                String message = JSON.getObject(response).getString("message");
                throw new Exception(message);
            }
            catch (JSONException e) {
                throw new Exception(Locale.get(HTTP_ERROR_CODE) + respCode);
            }
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    private static String sendData(String method, String url, String data) throws Exception {
        HttpConnection c = null;
        OutputStream os = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(method);
            
            byte[] b = Util.stringToBytes(data);

            if (Settings.tokenType == Settings.TOKEN_TYPE_HEADER) {
                c.setRequestProperty("Content-Length", String.valueOf(b.length));
            }

            os = c.openOutputStream();
            os.write(b);

            return sendRequest(c);
        } finally {
            try { os.close(); } catch (Exception e) {}
            try { c.close(); } catch (Exception e) {}
        }
    }

    private static String sendJson(String method, String url, JSONObject data) throws Exception {
        if (Settings.tokenType == Settings.TOKEN_TYPE_JSON) data.put("token", Settings.token);
        return sendData(method, url, data.build());
    }

    public static String get(String url) throws Exception {
        if (Settings.tokenType == Settings.TOKEN_TYPE_JSON) {
            JSONObject tokenJson = new JSONObject();
            tokenJson.put("token", Settings.token);
            return get(url, tokenJson);
        }

        HttpConnection c = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(HttpConnection.GET);
            return sendRequest(c);
        } finally {
            try { c.close(); } catch (Exception e) {}
        }
    }

    public static String post(String url, String data) throws Exception {
        return sendData(HttpConnection.POST, url, data);
    }
    public static String get(String url, String data) throws Exception {
        return sendData(HttpConnection.GET, url, data);
    }
    public static String post(String url, JSONObject data) throws Exception {
        return sendJson(HttpConnection.POST, url, data);
    }
    public static String get(String url, JSONObject data) throws Exception {
        return sendJson(HttpConnection.GET, url, data);
    }

    // Image loading code by shinovon
    // https://github.com/gtrxAC/discord-j2me/pull/5/commits/193c63f6a00b8e24da7a3582e9d1a92522f9940e
    public static Image getImage(String url) throws IOException {
		byte[] b = getBytes(url);
		return Image.createImage(b, 0, b.length);
	}

	public static byte[] getBytes(String url) throws IOException {
		HttpConnection hc = null;
		InputStream in = null;
		try {
			hc = (HttpConnection) Connector.open(App.getPlatformSpecificUrl(url));
			hc.setRequestMethod("GET");
			if (Settings.tokenType == Settings.TOKEN_TYPE_HEADER) {
				hc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0");
			}
			int r;
			if((r = hc.getResponseCode()) >= 400) {
				throw new IOException(Locale.get(HTTP_ERROR_CODE) + r);
			}
			in = hc.openInputStream();
			return Util.readBytes(in, (int) hc.getLength(), 1024, 2048);
		} finally {
            try { in.close(); } catch (Exception e) {}
            try { hc.close(); } catch (Exception e) {}
		}
	}
}