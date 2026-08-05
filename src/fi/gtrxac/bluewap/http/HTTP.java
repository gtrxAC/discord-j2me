package fi.gtrxac.bluewap.http;

import fi.gtrxac.bluewap.*;
import java.io.*;
import java.util.*;
import javax.microedition.lcdui.Image;

public abstract class HTTP {
//#ifndef NO_HTTP_BLUETOOTH_SUPPORT
	public static final int CONNECTION_TYPE_STANDARD = 0;
	public static final int CONNECTION_TYPE_BLUETOOTH = 1;
	public static int CONNECTION_TYPE = CONNECTION_TYPE_STANDARD;
//#endif

	protected String method;
	protected String url;
	protected byte[] data;
	protected Hashtable requestHeaders;
	protected Hashtable responseHeaders;
	protected int responseCode;
	protected byte[] responseBytes;
	protected InputStream is;
	private boolean requestMade;

	protected HTTP(String method, String url) {
		this.method = method;
		this.url = url;
		this.requestHeaders = new Hashtable();
		this.responseHeaders = new Hashtable();
		requestHeaders.put("Accept", HTTPConfig.DEFAULT_ACCEPT);
		requestHeaders.put("User-Agent", HTTPConfig.DEFAULT_USER_AGENT);
	}

	public static HTTP createRequest(String method, String url) throws Exception {
		String proto = new URL(url).protocol;

		if (proto.equals("http") || proto.equals("https") || proto.equals("discord")) {
			return createFetchRequest(method, url);
		}
		if (proto.equals("file") || proto.equals("jar")) {
			return new LocalHTTP(url);
		}
		throw new Exception("unsupported protocol '" + proto + "'");
	}

	public static HTTP createRequest(String url) throws Exception {
		return createRequest("GET", url);
	}

	private static HTTP createFetchRequest(String method, String url) {
//#ifndef NO_HTTP_BLUETOOTH_SUPPORT
		if (CONNECTION_TYPE == CONNECTION_TYPE_BLUETOOTH) {
			return new BluetoothHTTP(method, url);
		}
//#endif
		return new StandardHTTP(method, url);
	}

//#ifndef NO_HTTP_BLUETOOTH_SUPPORT
	public static void setConnectionType(int connectionType) {
		if (connectionType == CONNECTION_TYPE_STANDARD || connectionType == CONNECTION_TYPE_BLUETOOTH) {
			CONNECTION_TYPE = connectionType;
		}
	}
//#endif

	protected abstract InputStream makeRequest() throws Exception;

	protected abstract void closeTransport();

	protected void checkMakeRequest() throws Exception {
		if (!requestMade) {
			is = makeRequest();
			requestMade = true;
		}
	}

	/**
	 * Public API
	 */

	/**
	 * Set a HTTP request header that will be sent to the server with this request.
	 */
	public HTTP setHeader(String key, String value) {
		requestHeaders.put(key, value);
		return this;
	}

	/**
	 * Set the data that will be sent to the server with this request.
	 */
	public HTTP setData(byte[] data) {
		this.data = data;

		if (data != null) {
			requestHeaders.put("Content-Length", String.valueOf(data.length));
		} else {
			requestHeaders.remove("Content-Length");
		}

		if (!requestHeaders.containsKey("Content-Type")) {
			requestHeaders.put("Content-Type", HTTPConfig.DEFAULT_CONTENT_TYPE);
		}
		return this;
	}

	/**
	 * Set the data that will be sent to the server with this request.
	 */
	public HTTP setData(String data) {
		return setData(Util.stringToBytes(data));
	}
	
	/**
	 * Get the resulting URL that was requested, which may have changed in the case of a redirect.
	 */
	public String getUrl() throws Exception {
		checkMakeRequest();
		return url;
	}

	/**
	 * Get the HTTP response code returned by the server.
	 */
	public int getResponseCode() throws Exception {
		checkMakeRequest();
		return responseCode;
	}

	/**
	 * Get the value of a HTTP response header sent by the server.
	 * Must be called before the other `getResponse*()` methods.
	 * If the other `getResponse*()` methods are not called for this request, then `close()` must be used.
	 */
	public String getResponseHeader(String name) throws Exception {
		checkMakeRequest();
		return (String) responseHeaders.get(name);
	}

	/**
	 * Get server response as an input stream that can be used to read streamed data.
	 * Stream must be closed with `close()` after use.
	 */
	public InputStream getResponseStream() throws Exception {
		checkMakeRequest();
		return is;
	}

	/**
	 * Get server response as a byte array.
	 */
	public byte[] getResponseBytes() throws Exception {
		checkMakeRequest();
		if (responseBytes != null) {
			close();
			return responseBytes;
		}
		byte[] result = Util.readBytes(is);
		close();
		return result;
	}

	/**
	 * Get server response as a string.
	 */
	public String getResponseString() throws Exception {
		String charset = Util.getCharsetFromContentType(getResponseHeader("Content-Type"));
		return Util.bytesToString(getResponseBytes(), charset);
	}

	/**
	 * Get server response as an image.
	 */
	public Image getResponseImage() throws Exception {
		Image result = Image.createImage(getResponseStream());
		close();
		return result;
	}
	
	/**
	 * Close all streams related to this request.
	 */
	public void close() {
		closeTransport();
		try { is.close(); } catch (Exception e) {}
	}
}