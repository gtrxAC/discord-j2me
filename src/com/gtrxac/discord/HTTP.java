package com.gtrxac.discord;

import java.io.*;
import javax.microedition.io.*;
import cc.nnproject.json.*;
import javax.microedition.lcdui.Image;

//#ifdef MODERNCONNECTOR
import tech.alicesworld.ModernConnector.*;
//#endif

public class HTTP implements Strings {
	public static final Exception requestMethodException = new Exception();

	private static byte[] requestWrapped(String method, String url, Object data, String contentType, boolean authorize) throws Exception {
		HttpConnection hc = null;
		OutputStream os = null;

		try {
			hc = (HttpConnection) 
//#ifdef MODERNCONNECTOR
				ModernConnector
//#else
				Connector
//#endif	
				.open(App.getPlatformSpecificUrl(url));
				
			try {
				hc.setRequestMethod(method);
			}
			catch (Exception e) {
				throw requestMethodException;
			}

			if (!App.isLiteProxy) {
				hc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:143.0) Gecko/20100101 Firefox/143.0");
			}
			if (authorize) {
				hc.setRequestProperty("Authorization", Settings.token);
			}
			
			if (data != null) {
				if (contentType == null) contentType = "application/json";
				hc.setRequestProperty("Content-Type", contentType);
					
				byte[] dataBytes = (data instanceof String) ? Util.stringToBytes((String) data) : (byte[]) data;
				hc.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));

				os = hc.openOutputStream();
				os.write(dataBytes);
			}

			return sendRequest(hc);
		}
		finally {
			try { os.close(); } catch (Exception e) {}
			try { hc.close(); } catch (Exception e) {}
		}
	}

	public static byte[] sendRequest(HttpConnection hc) throws Exception {
		InputStream is = null;
		try {
			int respCode = hc.getResponseCode();
			is = hc.openInputStream();
			byte[] result = Util.readBytes(is, (int) hc.getLength(), 1024, 2048);

			if (respCode == HttpConnection.HTTP_OK) {
				return result;
			}
			if (respCode == HttpConnection.HTTP_UNAUTHORIZED) {
				throw new Exception(Locale.get(HTTP_ERROR_TOKEN));
			}
			if (respCode == HttpConnection.HTTP_BAD_GATEWAY) {
				throw new Exception(Locale.get(HTTP_ERROR_PROXY));
			}
			if (respCode == HttpConnection.HTTP_MOVED_TEMP) {
				throw new Exception(Locale.get(HTTP_ERROR_REDIRECT));
			}

			try {
				String message = JSON.getObject(Util.bytesToString(result)).getString("message");
				throw new Exception(message);
			}
			catch (JSONException e) {
				throw new Exception(Locale.get(HTTP_ERROR_CODE) + respCode);
			}
		}
		finally {
			try { is.close(); } catch (Exception e) {}
		}
	}

	public static byte[] request(String method, String url, Object data, String contentType, boolean authorize) throws Exception {
//#ifdef MIDP2_GENERIC
		int attempts = 0;

		while (true) {
			try {
				return requestWrapped(method, url, data, contentType, authorize);
			}
			catch (IOException e) {
				// Automatic retry if we get IOException -36 which randomly occurs on Symbian
				if (e.toString().indexOf("-36") != -1) {
					attempts++;
					if (attempts == 3) throw e;
				}
				else throw e;
			}
		}
//#else
		return requestWrapped(method, url, data, contentType, authorize);
//#endif
	}

	public static String getFullUrl(String url, boolean useProxy) {
//#ifdef PROXYLESS_SUPPORT
		if (!Settings.proxyless) 
//#endif
		useProxy = true;

		String fullUrl = (useProxy ? Settings.api : "https://discord.com") + "/api/v9" + url;

		if (useProxy && Settings.tokenType == Settings.TOKEN_TYPE_QUERY) {
			if (fullUrl.indexOf("?") != -1) {
				fullUrl += "&token=" + Settings.token;
			} else {
				fullUrl += "?token=" + Settings.token;
			}
		}
		return fullUrl;
	}

	public static String apiRequest(String method, String url, Object data, boolean useProxy) throws Exception {
		String fullUrl = getFullUrl(url, useProxy);
		return Util.bytesToString(request(method, fullUrl, data, null, true));
	}

	public static byte[] getBytes(String url) throws Exception {
		return request("GET", url, null, null, false);
	}
	public static String get(String url, boolean useProxy) throws Exception {
		return apiRequest("GET", url, null, useProxy);
	}
	public static String post(String url, String data, boolean useProxy) throws Exception {
		return apiRequest("POST", url, data, useProxy);
	}
	public static String post(String url, JSONObject data, boolean useProxy) throws Exception {
		return post(url, data.build(), useProxy);
	}
	
	public static Image getImage(String url) throws Exception {
		byte[] b = getBytes(url);
		return Image.createImage(b, 0, b.length);
	}
}