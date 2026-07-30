package fi.gtrxac.bluewap.http;

import fi.gtrxac.bluewap.*;
import java.io.*;
import java.util.*;
import javax.microedition.io.*;

public class LocalHTTP extends HTTP {
	public LocalHTTP(String url) {
		super("GET", url);
	}

	protected InputStream makeRequest() throws Exception {
		if (method.equals("POST")) {
			throw new Exception("POST method not supported for local files");
		}

		String path = "/" + new URL(url).getPath();
		InputStream is = new Object().getClass().getResourceAsStream(path);
		if (is == null) throw new Exception("file not found: '" + path + "'");

		responseHeaders.put("Content-Type", getContentType());

		return is;
	}

	private String getContentType() {
		int extensionIndex = url.lastIndexOf('.');
		if (extensionIndex == -1) return "application/octet-stream";

		String extension = url.substring(extensionIndex + 1);

		if (extension.equals("wml")) return "text/vnd.wap.wml";
		if (extension.equals("htm")) return "text/html";
		if (extension.equals("html")) return "text/html";
		if (extension.equals("png")) return "image/png";
		if (extension.equals("jpg")) return "image/jpeg";
		if (extension.equals("gif")) return "image/gif";
		if (extension.equals("wbmp")) return "image/vnd.wap.wbmp";

		return "application/octet-stream";
	}

	protected void closeTransport() {
	}
}