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
		url = App.getPlatformSpecificUrl(url);
		HTTPQueue queueItem = HTTPQueue.newQueueItem();

		try {
			hc = (HttpConnection)
//#ifdef JL_TLS_VERSION
				(Settings.useModcon ? ModernConnector.open(url) : Connector.open(url));
//#else
//#ifdef MODERNCONNECTOR
				ModernConnector
//#else
				Connector
//#endif	
				.open(url);
//#endif

			queueItem.hc = hc;
				
			try {
				hc.setRequestMethod(method);
			}
			catch (Exception e) {
				throw requestMethodException;
			}

//#ifdef MODERNCONNECTOR
			if (url.indexOf("remote-auth") != -1) {
				hc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:149.0) Gecko/20100101 Firefox/149.0");
			} else
//#endif
			if (!App.isLiteProxy) {
				hc.setRequestProperty("User-Agent", "Discord-Android/262205;RNA");
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
				queueItem.os = os;
				os.write(dataBytes);
			}

			return sendRequest(queueItem, hc);
		}
		finally {
			queueItem.finished();
		}
	}

	public static byte[] sendRequest(HTTPQueue queueItem, HttpConnection hc) throws Exception {
		InputStream is = null;
		try {
			int respCode = hc.getResponseCode();

			is = hc.openInputStream();
			queueItem.is = is;
			byte[] result = Util.readBytes(is, (int) hc.getLength(), 1024, 2048);

			if (respCode == HttpConnection.HTTP_OK) {
				String authID = hc.getHeaderField("X-Microcord-Auth-ID");
				if (authID != null) QRLoginScreen.authID = authID;

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
//#ifdef MODERNCONNECTOR
				if (Util.bytesToString(result).indexOf("captcha") != -1) {
					throw new Exception("A CAPTCHA was received. You may try logging in to a Discord desktop client via QR code and then trying Discord J2ME QR login again, but it is likely that you'll need to use token login instead.");
				}
//#endif
				throw new Exception(Locale.get(HTTP_ERROR_CODE) + respCode);
			}
		}
		finally {
			try { is.close(); } catch (Exception e) {}
		}
	}

	public static byte[] request(String method, String url, Object data, String contentType, boolean authorize) throws Exception {
//#ifdef MODERNCONNECTOR
//#ifndef JL_TLS_VERSION
		try {
//#endif
//#endif

//#ifdef SYMBIAN
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
//#ifdef JL_TLS_VERSION
			// J2ME Loader: if proxyless enabled, and HTTPS request (using system TLS) fails, start using java-based TLS instead
			// Android below 5 does not have TLS 1.2 by default
			try {
				return requestWrapped(method, url, data, contentType, authorize);
			}
			catch (IOException e) {
				if (e.toString().indexOf("Failure in SSL library") != -1 || e.toString().indexOf("unsupported protocol") != -1) {
					Settings.useModcon = true;
					Settings.save();
					return requestWrapped(method, url, data, contentType, authorize);
				}
				else throw e;
			}
//#else
			return requestWrapped(method, url, data, contentType, authorize);
//#endif
//#endif

//#ifdef MODERNCONNECTOR
//#ifndef JL_TLS_VERSION
		}
		catch (Exception e) {
			if (Settings.proxyless && e instanceof SecurityException) {
				// More descriptive error message for when port 443 socket connection cannot be opened because the app is not signed
				throw new SecurityException(Locale.get(PROXYLESS_ERROR_UNSIGNED));
			} else {
				throw e;
			}
		}
//#endif
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