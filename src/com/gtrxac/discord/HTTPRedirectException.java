//#ifndef NO_HTTP_REDIRECT_SUPPORT
package com.gtrxac.discord;

public class HTTPRedirectException extends Exception {
	public HTTPRedirectException(String url) {
		super(url);
	}
	
	public String getUrl() {
		return getMessage();
	}
}
//#endif