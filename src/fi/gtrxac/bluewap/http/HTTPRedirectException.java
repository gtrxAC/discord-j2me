package fi.gtrxac.bluewap.http;

//#ifndef NO_HTTP_REDIRECT_SUPPORT
public class HTTPRedirectException extends Exception {
	public HTTPRedirectException(String url) {
		super(url);
	}
	
	public String getUrl() {
		return getMessage();
	}
}
//#endif