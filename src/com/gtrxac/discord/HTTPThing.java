package com.gtrxac.discord;

import java.io.*;
import javax.microedition.io.*;
import cc.nnproject.json.*;
import javax.microedition.lcdui.Image;

public class HTTPThing implements Strings {
    State s;

	public HTTPThing(State s) {
        this.s = s;
	}

    public HttpConnection openConnection(String url) throws IOException {
        String fullUrl = s.getPlatformSpecificUrl(s.api + "/api/v9" + url);

        if (s.tokenType == State.TOKEN_TYPE_QUERY) {
            if (fullUrl.indexOf("?") != -1) {
                fullUrl += "&token=" + s.token;
            } else {
                fullUrl += "?token=" + s.token;
            }
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

    // Image loading code by shinovon
    // https://github.com/gtrxAC/discord-j2me/pull/5/commits/193c63f6a00b8e24da7a3582e9d1a92522f9940e
    public Image getImage(String url) throws IOException {
		byte[] b = getBytes(url);
		return Image.createImage(b, 0, b.length);
	}

	public byte[] getBytes(String url) throws IOException {
		HttpConnection hc = null;
		InputStream in = null;
		try {
			hc = open(url);
			int r;
			if((r = hc.getResponseCode()) >= 400) {
				throw new IOException(Locale.get(HTTP_ERROR_CODE) + r);
			}
			in = hc.openInputStream();
			return readBytes(in, (int) hc.getLength(), 1024, 2048);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
			}
			try {
				if (hc != null) hc.close();
			} catch (IOException e) {
			}
		}
	}

	private static byte[] readBytes(InputStream inputStream, int initialSize, int bufferSize, int expandSize) throws IOException {
		if (initialSize <= 0) initialSize = bufferSize;
		byte[] buf = new byte[initialSize];
		int count = 0;
		byte[] readBuf = new byte[bufferSize];
		int readLen;
		while ((readLen = inputStream.read(readBuf)) != -1) {
			if(count + readLen > buf.length) {
				byte[] newbuf = new byte[count + expandSize];
				System.arraycopy(buf, 0, newbuf, 0, count);
				buf = newbuf;
			}
			System.arraycopy(readBuf, 0, buf, count, readLen);
			count += readLen;
		}
		if(buf.length == count) {
			return buf;
		}
		byte[] res = new byte[count];
		System.arraycopy(buf, 0, res, 0, count);
		return res;
	}

	private HttpConnection open(String url) throws IOException {
		HttpConnection hc = (HttpConnection) Connector.open(s.getPlatformSpecificUrl(url));
		hc.setRequestMethod("GET");
		if (s.tokenType == State.TOKEN_TYPE_HEADER) {
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0");
        }
		return hc;
	}
}