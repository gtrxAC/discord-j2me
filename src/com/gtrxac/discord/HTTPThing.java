package com.gtrxac.discord;

import java.io.*;
import javax.microedition.io.*;
import cc.nnproject.json.*;
import javax.microedition.lcdui.Image;

public class HTTPThing {
    State s;
    String api;
    String token;

	public HTTPThing(State s, String api, String token) {
        this.s = s;
        this.api = api;
		this.token = token;
	}

    public HttpConnection openConnection(String url) throws IOException {
        String fullUrl = s.getPlatformSpecificUrl(api + "/api/v9" + url);

        HttpConnection c = (HttpConnection)Connector.open(fullUrl);

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
        InputStream is = null;

        // Getting the InputStream ensures that the connection
        // is opened (if it was not already handled by
        // Connector.open()) and the SSL handshake is exchanged,
        // and the HTTP response headers are read.
        // These are stored until requested.
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
				throw new IOException("HTTP " + r);
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
		hc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0");
		return hc;
	}
}