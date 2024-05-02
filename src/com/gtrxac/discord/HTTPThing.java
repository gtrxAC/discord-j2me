package com.gtrxac.discord;

import java.io.*;
import javax.microedition.io.*;

public class HTTPThing {
    String api;
    String token;

	public HTTPThing(String api, String token) {
        this.api = api;
		this.token = token;
	}

    public HttpConnection openConnection(String url) throws IOException {
        HttpConnection c = (HttpConnection)Connector.open(api + "/api/v9" + url);

        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0");
        c.setRequestProperty("Accept", "*/*");
        c.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        c.setRequestProperty("Authorization", token);
        c.setRequestProperty("X-Discord-Locale", "en-GB");
        c.setRequestProperty("X-Debug-Options", "bugReporterEnabled");
        c.setRequestProperty("Sec-Fetch-Dest", "empty");
        c.setRequestProperty("Sec-Fetch-Mode", "cors");
        c.setRequestProperty("Sec-Fetch-Site", "same-origin");

        return c;
    }

    public String sendRequest(HttpConnection c) throws Exception {
        if (api == null || api.length() == 0) throw new Exception("Please specify an API URL");
        if (token == null || token.length() == 0) throw new Exception("Token is required");

        InputStream is = null;

        // Getting the InputStream ensures that the connection
        // is opened (if it was not already handled by
        // Connector.open()) and the SSL handshake is exchanged,
        // and the HTTP response headers are read.
        // These are stored until requested.
        is = c.openDataInputStream();

        try {
            if (c.getResponseCode() == HttpConnection.HTTP_OK) {
                // Read response
                StringBuffer stringBuffer = new StringBuffer();
                int ch;
                while ((ch = is.read()) != -1) {
                    stringBuffer.append((char) ch);
                }

                return stringBuffer.toString().trim();
            }
            else if (c.getResponseCode() == HttpConnection.HTTP_UNAUTHORIZED) {
                throw new Exception("Check your token");
            }
            else {
                Integer code = new Integer(c.getResponseCode());
                throw new Exception("HTTP error " + code.toString());
            }
        } finally {
            if (is != null) is.close();
        }
    }

    public String get(String url) throws IOException, Exception {
        HttpConnection c = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(HttpConnection.GET);
            return sendRequest(c);
        } finally {
            if (c != null) c.close();
        }
    }

    public String post(String url, String data) throws IOException, Exception {
        HttpConnection c = null;
        OutputStream os = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(HttpConnection.POST);
            byte[] b = data.getBytes("UTF-8");
            c.setRequestProperty("Content-Length", String.valueOf(b.length));

            os = c.openOutputStream();
            os.write(b);

            return sendRequest(c);
        } finally {
            if (os != null) os.close();
            if (c != null) c.close();
        }
    }
}