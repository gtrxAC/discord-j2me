package fi.gtrxac.bluewap.http;

import com.gtrxac.discord.HTTPQueue;
import fi.gtrxac.bluewap.*;
import java.io.*;
import java.util.*;
import javax.microedition.io.*;

public class StandardHTTP extends HTTP {
	public static final Exception requestMethodException = new Exception();
	public static final Exception redirectException = new Exception();

//#ifndef NO_HTTP_BLACKBERRY_WIFI
	private static final boolean isBlackBerry;
	static {
		String plat = System.getProperty("microedition.platform");
		isBlackBerry = (plat != null && plat.toLowerCase().startsWith("blackberry"));
	}
//#endif

	private HTTPQueue queueItem;
	private HttpConnection hc;
	private OutputStream os;

	public StandardHTTP(String method, String url) {
		super(method, url);
	}

	private InputStream makeRequestFinal(String finalUrl) throws Exception {
		queueItem = HTTPQueue.newQueueItem();
		hc = (HttpConnection) Connector.open(finalUrl);
		queueItem.hc = hc;

		try {
			hc.setRequestMethod(method);
		}
		catch (Exception e) {
			throw requestMethodException;
		}

		for (Enumeration e = requestHeaders.keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String value = (String) requestHeaders.get(key);
			hc.setRequestProperty(key, value);
		}

		if (data != null) {
			os = hc.openOutputStream();
			queueItem.os = os;
			os.write(data);
		}

		responseCode = hc.getResponseCode();
		InputStream result = hc.openInputStream();
		queueItem.is = result;

//#ifndef NO_HTTP_REDIRECT_SUPPORT
		if (responseCode >= 300 && responseCode < 400) {
			String redir = hc.getHeaderField("Location");
			if (redir == null) throw new Exception("received redirect with no location");

			url = new URL(redir, new URL(url)).toString(false);
			throw redirectException;
		}
//#endif
		return result;
	}

	private InputStream makeRequestWithCloseOnError(String finalUrl) throws Exception {
		try {
			return makeRequestFinal(finalUrl);
		}
		catch (Exception e) {
			close();
			throw e;
		}
	}

	private InputStream makeRequestWithBlackBerryWifi() throws Exception {
//#ifndef NO_HTTP_BLACKBERRY_WIFI
		if (isBlackBerry) {
			String bbWifiUrl = url + ";deviceside=true;interface=wifi";
			long time = System.currentTimeMillis();

			try {
				return makeRequestWithCloseOnError(bbWifiUrl);
			}
			catch (Exception e) {
				time = System.currentTimeMillis() - time;
				if (time > 10) throw e;
			}
		}
//#endif
		return makeRequestWithCloseOnError(url);
	}

	private InputStream makeRequestWithSymbianErrorHandler() throws Exception {
//#ifndef NO_HTTP_SYMBIAN_ERROR_HANDLER
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				return makeRequestWithBlackBerryWifi();
			}
			catch (Exception e) {
				if (e.toString().indexOf("-36") == -1) throw e;
			}
		}
		throw new Exception("too many connection attempts");
//#else
		return makeRequestWithBlackBerryWifi();
//#endif
	}

	protected InputStream makeRequest() throws Exception {
//#ifdef BLUEWAP_SERVER
		String clientUa = (String) requestHeaders.get("User-Agent");
		if (clientUa == null) {
			clientUa = "";
		}
		else if (clientUa.length() != 0 && !clientUa.endsWith(" ")) {
			clientUa += " ";
		}
		clientUa += "BlueWAPServer/" + AppBase.instance.getAppProperty("MIDlet-Version");
		setHeader("User-Agent", clientUa);
//#endif

		for (int redirs = 0; redirs < 5; redirs++) {
			try {
				return makeRequestWithSymbianErrorHandler();
			}
			catch (Exception e) {
				if (e != redirectException) throw e;
			}
		}
		throw new Exception("too many redirects");
	}

	protected void closeTransport() {
		queueItem.finished();
	}

	/**
	 * Get the value of a HTTP response header sent by the server.
	 */
	public String getResponseHeader(String name) throws Exception {
		checkMakeRequest();
		return hc.getHeaderField(name);
	}
}
