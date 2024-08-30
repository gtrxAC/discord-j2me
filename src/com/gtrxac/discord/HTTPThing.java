package com.gtrxac.discord;

import java.io.*;
import javax.microedition.io.*;
import cc.nnproject.json.*;

public class HTTPThing {
    State s;

	public HTTPThing(State s) {
        this.s = s;
	}

    public HttpConnection openConnection(String url) throws IOException {
        String fullUrl = s.api + "/api/l" + url;

        if (s.tokenType == State.TOKEN_TYPE_QUERY) {
            char paramDelimiter = (fullUrl.indexOf("?") != -1) ? '&' : '?';
            fullUrl += paramDelimiter + "token=" + s.token;
        }

        HttpConnection c = (HttpConnection) Connector.open(fullUrl);

        if (s.tokenType == State.TOKEN_TYPE_HEADER) {
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("Authorization", s.token);
        }

        return c;
    }

    public String sendRequest(HttpConnection c) throws Exception {
        InputStream is = null;

        is = c.openDataInputStream();

        try {
            int respCode = c.getResponseCode();
            
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
                throw new Exception("Check your token");
            }

            try {
                String message = JSON.getObject(response).getString("message");
                throw new Exception(message);
            }
            catch (JSONException e) {
                throw new Exception("HTTP error " + respCode);
            }
        } finally {
            if (is != null) is.close();
        }
    }

    private String sendData(String method, String url, String data) throws Exception {
        HttpConnection c = null;
        OutputStream os = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(method);
            
            byte[] b;
            try {
                b = data.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                b = data.getBytes();
            }

            if (s.tokenType == State.TOKEN_TYPE_HEADER) {
                c.setRequestProperty("Content-Length", String.valueOf(b.length));
            }

            os = c.openOutputStream();
            os.write(b);

            return sendRequest(c);
        } finally {
            if (os != null) os.close();
            if (c != null) c.close();
        }
    }

    private String sendJson(String method, String url, JSONObject data) throws Exception {
        if (s.tokenType == State.TOKEN_TYPE_JSON) data.put("token", s.token);
        return sendData(method, url, data.build());
    }

    public String get(String url) throws Exception {
        if (s.tokenType == State.TOKEN_TYPE_JSON) {
            JSONObject tokenJson = new JSONObject();
            tokenJson.put("token", s.token);
            return get(url, tokenJson);
        }

        HttpConnection c = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(HttpConnection.GET);
            return sendRequest(c);
        } finally {
            if (c != null) c.close();
        }
    }

    public String post(String url, String data) throws Exception {
        return sendData(HttpConnection.POST, url, data);
    }
    public String get(String url, String data) throws Exception {
        return sendData(HttpConnection.GET, url, data);
    }
    public String post(String url, JSONObject data) throws Exception {
        return sendJson(HttpConnection.POST, url, data);
    }
    public String get(String url, JSONObject data) throws Exception {
        return sendJson(HttpConnection.GET, url, data);
    }
}