package tech.alicesworld.ModernConnector;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.InputConnection;
import javax.microedition.io.OutputConnection;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SecurityInfo;

/**
 * A J2ME HttpsConnection implementation that sends HTTP/1.1 over a
 * caller-provided {@link SecureConnection}. API-compatible with
 * {@link HttpsConnection}.
 *
 * Usage: SecureConnection sc = (SecureConnection)
 * Connector.open("ssl://host:443"); HttpsConnection conn = new
 * PassthroughHttpsConnection(sc, "https://host/path?query");
 * conn.setRequestMethod(HttpConnection.GET); conn.setRequestProperty("Accept",
 * "*"); InputStream in = conn.openInputStream(); // triggers send if not sent
 * int code = conn.getResponseCode(); ...
 *
 * Notes: - This class assumes the provided SecureConnection is already
 * connected to the target host:port for the given URL. - Supports response
 * bodies with Content-Length or Transfer-Encoding: chunked. - Minimal cookie
 * handling (pass-through if you set headers yourself). - No automatic
 * redirects; you can inspect 3xx and follow manually if desired.
 */
public final class ModernHTTPSConnection implements HttpsConnection {

    private final SecureConnection sc;
    private final String url;

    // Request state
    private String method = GET;
    private String httpVersion = "HTTP/1.1";
    private final Hashtable reqHeaders = new Hashtable(); // name -> value (String)
    private final Vector reqHeaderOrder = new Vector();   // preserve order
    private ByteArrayOutputStream reqBodyBuffer;          // only allocated on demand
    private boolean outputOpened = false;
    private boolean inputOpened = false;
    private boolean requestSent = false;

    // Response state
    private int responseCode = -1;
    private String responseMessage = null;
    private final Vector respHeaderNames = new Vector();  // index-aligned with respHeaderValues
    private final Vector respHeaderValues = new Vector();
    private InputStream responseBodyStream = null;
    private String reasonPhraseFromStatus = null;

    // Cached response header-derived fields
    private long contentLength = -1;
    private String contentType = null;
    private String contentEncoding = null;

    // Streams on the secure connection
    private InputStream tlsIn;
    private OutputStream tlsOut;

    // Parsed URL bits
    private String hostHeader;
    private String pathAndQuery;

    // ------------------------------------------------------------------------
    public ModernHTTPSConnection(SecureConnection sc, String url) throws IOException {
        if (sc == null) {
            throw new IllegalArgumentException("SecureConnection must not be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }
        if (!url.regionMatches(true, 0, "https://", 0, 8)) {
            throw new IllegalArgumentException("URL must start with https://");
        }
        this.sc = sc;
        this.url = url;

        parseUrl(url); // fills hostHeader & pathAndQuery

        // Open underlying streams once
        this.tlsIn = sc.openInputStream();
        this.tlsOut = sc.openOutputStream();

        // Set Host header by default (caller can override)
        if (getRequestProperty("Host") == null) {
            setRequestProperty("Host", hostHeader);
        }
        // Reasonable default User-Agent; change as you like
        if (getRequestProperty("User-Agent") == null) {
            setRequestProperty("User-Agent", "J2ME-PassthroughHttps/1.0");
        }
        // Default Connection: close (safer for simple clients)
        if (getRequestProperty("Connection") == null) {
            setRequestProperty("Connection", "close");
        }
    }

    private void parseUrl(String u) {
        // https://host[:port]/path?query
        // Extract host[:port] for Host header and path+query for request line
        int schemeEnd = u.indexOf("://");
        int hostStart = (schemeEnd >= 0) ? schemeEnd + 3 : 0;
        int pathStart = u.indexOf('/', hostStart);
        if (pathStart < 0) {
            hostHeader = u.substring(hostStart);
            pathAndQuery = "/";
        } else {
            hostHeader = u.substring(hostStart, pathStart);
            pathAndQuery = u.substring(pathStart);
            if (pathAndQuery.length() == 0) {
                pathAndQuery = "/";
            }
        }
    }

    // ------------------------------------------------------------------------
    // Sending logic
    private void ensureRequestBodyBuffer() {
        if (reqBodyBuffer == null) {
            reqBodyBuffer = new ByteArrayOutputStream(256);
        }
    }

    private void sendIfNeeded() throws IOException {
        if (requestSent) {
            return;
        }

        // If caller wrote to output, add/override Content-Length unless chunking was requested explicitly.
        boolean hasTE = hasRequestHeaderIgnoreCase("Transfer-Encoding");
        if (outputOpened && !hasTE) {
            // We will send a Content-Length with the buffered body size
            setRequestProperty("Content-Length", Integer.toString(reqBodyBuffer.size()));
        }

        writeRequestLineAndHeaders();
        writeRequestBodyIfAny();

        requestSent = true;
    }

    private boolean hasRequestHeaderIgnoreCase(String name) {
        for (int i = 0; i < reqHeaderOrder.size(); i++) {
            String k = (String) reqHeaderOrder.elementAt(i);
            // if (k.equalsIgnoreCase(name)) {
            if (k.toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void writeRequestLineAndHeaders() throws IOException {
        // Request line
        writeAscii(method);
        writeAscii(" ");
        writeAscii(pathAndQuery);
        writeAscii(" ");
        writeAscii(httpVersion);
        writeAscii("\r\n");

        // Headers (in caller-set order)
        for (int i = 0; i < reqHeaderOrder.size(); i++) {
            String name = (String) reqHeaderOrder.elementAt(i);
            String value = (String) reqHeaders.get(name);
            writeAscii(name);
            writeAscii(": ");
            writeAscii(value);
            writeAscii("\r\n");
        }
        // End of headers
        writeAscii("\r\n");
        tlsOut.flush();
    }

    private void writeRequestBodyIfAny() throws IOException {
        if (!outputOpened || reqBodyBuffer == null) {
            return;
        }
        byte[] body = reqBodyBuffer.toByteArray();
        if (body.length > 0) {
            tlsOut.write(body);
            tlsOut.flush();
        }
    }

    private void writeAscii(String s) throws IOException {
        // All request header bytes are ASCII by contract
        byte[] b = s.getBytes("ISO-8859-1");
        tlsOut.write(b);
    }

    // ------------------------------------------------------------------------
    // Response parsing
    private void readStatusAndHeadersIfNeeded() throws IOException {
        sendIfNeeded(); // Make sure request is out

        if (responseCode != -1) {
            return; // already parsed
        }
        LineReader lr = new LineReader(tlsIn);

        String statusLine = lr.readLineStrict();
        // Example: HTTP/1.1 200 OK
        parseStatusLine(statusLine);

        // Headers
        while (true) {
            String line = lr.readLineStrict();
            if (line.length() == 0) {
                break; // empty line => end of headers
            }
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue; // skip malformed
            }
            String name = trim(line.substring(0, colon));
            String value = trim(stripLeadingSpaces(line.substring(colon + 1)));
            respHeaderNames.addElement(name);
            respHeaderValues.addElement(value);
        }

        // Cache common headers
        String cl = getHeaderField("Content-Length");
        if (cl != null) {
            try {
                contentLength = Long.parseLong(cl);
            } catch (NumberFormatException nfe) {
                /* ignore */ }
        }
        contentType = getHeaderField("Content-Type");
        contentEncoding = getHeaderField("Content-Encoding");

        // Prepare body stream
        String te = getHeaderField("Transfer-Encoding");
        InputStream raw = lr.remainingStream();
        if (te != null && toLower(te).indexOf("chunked") >= 0) {
            responseBodyStream = new ChunkedInputStream(raw);
        } else if (contentLength >= 0) {
            responseBodyStream = new FixedLengthInputStream(raw, contentLength);
        } else {
            // No length and not chunked => read-until-close
            responseBodyStream = raw;
        }
    }

    private static String stripLeadingSpaces(String s) {
        int i = 0;
        while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t')) {
            i++;
        }
        return (i == 0) ? s : s.substring(i);
    }

    private static String trim(String s) {
        int a = 0, b = s.length();
        while (a < b && s.charAt(a) <= ' ') {
            a++;
        }
        while (b > a && s.charAt(b - 1) <= ' ') {
            b--;
        }
        return (a == 0 && b == s.length()) ? s : s.substring(a, b);
    }

    private void parseStatusLine(String status) throws IOException {
        // Minimal robust parsing
        // Expect "HTTP/1.x <code> <reason...>"
        int sp1 = status.indexOf(' ');
        int sp2 = (sp1 >= 0) ? status.indexOf(' ', sp1 + 1) : -1;
        if (sp1 < 0 || sp2 < 0) {
            throw new IOException("Malformed status: " + status);
        }
        // String httpVer = status.substring(0, sp1);
        String codeStr = status.substring(sp1 + 1, sp2);
        try {
            responseCode = Integer.parseInt(codeStr);
        } catch (NumberFormatException nfe) {
            throw new IOException("Bad status code: " + codeStr);
        }
        reasonPhraseFromStatus = status.substring(sp2 + 1);
        responseMessage = reasonPhraseFromStatus;
    }

    private static String toLower(String s) {
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            char ch = c[i];
            if (ch >= 'A' && ch <= 'Z') {
                c[i] = (char) (ch - 'A' + 'a');
            }
        }
        return new String(c);
    }

    // ------------------------------------------------------------------------
    // HttpsConnection
    public SecurityInfo getSecurityInfo() throws IOException {
        return sc.getSecurityInfo(); // delegate to the injected SecureConnection
    }

    // ------------------------------------------------------------------------
    // HttpConnection
    public void setRequestMethod(String method) throws IOException {
        if (requestSent || inputOpened) {
            throw new IOException("Request already sent");
        }
        if (method == null) {
            throw new IllegalArgumentException("method is null");
        }
        this.method = method;
    }

    public String getRequestMethod() {
        return method;
    }

    public void setRequestProperty(String key, String value) throws IOException {
        if (requestSent || inputOpened) {
            throw new IOException("Request already sent");
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (value == null) {
            value = "";
        }
        // preserve case & order as given
        boolean seen = false;
        for (int i = 0; i < reqHeaderOrder.size(); i++) {
            String k = (String) reqHeaderOrder.elementAt(i);
            if (k.equals(key)) {
                seen = true;
                break;
            }
        }
        reqHeaders.put(key, value);
        if (!seen) {
            reqHeaderOrder.addElement(key);
        }
    }

    public String getRequestProperty(String key) {
        return (String) reqHeaders.get(key);
    }

    public String getURL() {
        return url;
    }

    public String getProtocol() {
        return "https";
    }

    public String getHost() {
        // host[:port]
        return hostHeader;
    }

    public String getFile() {
        return pathAndQuery;
    }

    public String getRef() {
        // Not parsed from URL fragments for simplicity
        return null;
    }

    public String getQuery() {
        int q = pathAndQuery.indexOf('?');
        return (q >= 0 && q + 1 < pathAndQuery.length()) ? pathAndQuery.substring(q + 1) : null;
    }

    public int getPort() {
        // If the Host header includes :port, parse; otherwise default 443.
        int idx = hostHeader.indexOf(':');
        if (idx >= 0) {
            try {
                return Integer.parseInt(hostHeader.substring(idx + 1));
            } catch (Exception e) {
                /* ignore */ }
        }
        return 443;
    }

    public String getHeaderField(String name) {
        if (name == null) {
            return null;
        }
        for (int i = 0; i < respHeaderNames.size(); i++) {
            String n = (String) respHeaderNames.elementAt(i);
            // if (n.equalsIgnoreCase(name)) {
            if (n.toLowerCase().equals(name.toLowerCase())) {
                return (String) respHeaderValues.elementAt(i);
            }
        }
        return null;
    }

    public String getHeaderField(int n) {
        if (n < 0 || n >= respHeaderValues.size()) {
            return null;
        }
        return (String) respHeaderValues.elementAt(n);
    }

    public String getHeaderFieldKey(int n) {
        if (n < 0 || n >= respHeaderNames.size()) {
            return null;
        }
        return (String) respHeaderNames.elementAt(n);
    }

    public int getHeaderFieldInt(String name, int def) {
        String v = getHeaderField(name);
        if (v == null) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public long getHeaderFieldDate(String name, long def) {
        // J2ME doesn't have java.text.*; simple RFC1123 parsing omitted.
        // Return default unless Content-Length-like numeric timestamp is given.
        String v = getHeaderField(name);
        if (v == null) {
            return def;
        }
        // crude: many servers don't send numeric dates; keep def
        return def;
    }

    public int getResponseCode() throws IOException {
        readStatusAndHeadersIfNeeded();
        return responseCode;
    }

    public String getResponseMessage() throws IOException {
        readStatusAndHeadersIfNeeded();
        return responseMessage;
    }

    // ContentConnection
    public long getLength() {
        return contentLength;
    }

    public String getType() {
        return contentType;
    }

    public String getEncoding() {
        return contentEncoding;
    }

    // InputConnection
    public InputStream openInputStream() throws IOException {
        inputOpened = true;
        readStatusAndHeadersIfNeeded();
        return responseBodyStream;
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    // OutputConnection
    public OutputStream openOutputStream() throws IOException {
        if (requestSent) {
            throw new IOException("Request already sent");
        }
        outputOpened = true;
        ensureRequestBodyBuffer();
        return new RequestBodyOutputStream();
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    // Connection
    public void close() throws IOException {
        try {
            if (!requestSent && outputOpened) {
                // If the user forgets to read, we still try to send the request
                sendIfNeeded();
            }
        } catch (IOException ignored) {
        }
        IOException first = null;
        try {
            if (responseBodyStream != null) {
                responseBodyStream.close();
            }
        } catch (IOException e) {
            first = (first == null) ? e : first;
        }
        try {
            if (tlsIn != null) {
                tlsIn.close();
            }
        } catch (IOException e) {
            first = (first == null) ? e : first;
        }
        try {
            if (tlsOut != null) {
                tlsOut.close();
            }
        } catch (IOException e) {
            first = (first == null) ? e : first;
        }
        try {
            sc.close();
        } catch (IOException e) {
            first = (first == null) ? e : first;
        }
        if (first != null) {
            throw first;
        }
    }

    // ------------------------------------------------------------------------
    // Convenience (optional): explicitly send without opening input
    public void send() throws IOException {
        sendIfNeeded();
        // After send(), caller can still call openInputStream() to read response
    }

    public long getDate() throws IOException {
        throw new RuntimeException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long getExpiration() throws IOException {
        throw new RuntimeException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long getLastModified() throws IOException {
        throw new RuntimeException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // ------------------------------------------------------------------------
    // Inner classes/utilities
    private final class RequestBodyOutputStream extends OutputStream {

        public void write(int b) throws IOException {
            ensureRequestBodyBuffer();
            reqBodyBuffer.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            ensureRequestBodyBuffer();
            reqBodyBuffer.write(b, off, len);
        }

        public void close() throws IOException {
            // No-op: body stays buffered until send
        }

        public void flush() throws IOException {
            // No-op (buffered)
        }
    }

    /**
     * Reads CRLF-terminated lines from an InputStream and exposes a "remaining"
     * view for further body reads.
     */
    private static final class LineReader {

        private final InputStream in;
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream(128);

        LineReader(InputStream in) {
            this.in = in;
        }

        String readLineStrict() throws IOException {
            buf.reset();
            int prev = -1;
            while (true) {
                int b = in.read();
                if (b == -1) {
                    if (buf.size() == 0) {
                        throw new EOFException("Unexpected EOF reading line");
                    }
                    // return last line without CRLF
                    break;
                }
                if (b == '\n') {
                    // Trim trailing CR if present
                    int size = buf.size();
                    if (size > 0 && buf.toByteArray()[size - 1] == '\r') {
                        byte[] t = buf.toByteArray();
                        return new String(t, 0, size - 1, "ISO-8859-1");
                    }
                    byte[] t = buf.toByteArray();
                    return new String(t, 0, size, "ISO-8859-1");
                } else {
                    buf.write(b);
                }
                prev = b;
            }
            byte[] t = buf.toByteArray();
            return new String(t, 0, t.length, "ISO-8859-1");
        }

        InputStream remainingStream() {
            return in;
        }
    }

    /**
     * InputStream that reads exactly N bytes; then EOF.
     */
    private static final class FixedLengthInputStream extends InputStream {

        private final InputStream in;
        private long remaining;

        FixedLengthInputStream(InputStream in, long len) {
            this.in = in;
            this.remaining = len;
        }

        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int b = in.read();
            if (b == -1) {
                return -1;
            }
            remaining--;
            return b;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            if (len > remaining) {
                len = (int) remaining;
            }
            int n = in.read(b, off, len);
            if (n == -1) {
                return -1;
            }
            remaining -= n;
            return n;
        }

        public void close() throws IOException {
            // Best-effort drain? For J2ME we just close.
            in.close();
        }
    }

    /**
     * Minimal chunked transfer-decoding InputStream.
     */
    private static final class ChunkedInputStream extends InputStream {

        private final InputStream in;
        private long chunkRemaining = 0;
        private boolean done = false;

        ChunkedInputStream(InputStream in) {
            this.in = in;
        }

        public int read() throws IOException {
            byte[] one = new byte[1];
            int n = read(one, 0, 1);
            return (n == -1) ? -1 : (one[0] & 0xFF);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (done) {
                return -1;
            }
            if (chunkRemaining == 0) {
                // Read next chunk size line
                long size = readChunkSize();
                if (size == 0) {
                    // Consume trailing headers until empty line
                    readTrailers();
                    done = true;
                    return -1;
                }
                chunkRemaining = size;
            }
            if (len > chunkRemaining) {
                len = (int) chunkRemaining;
            }
            int n = in.read(b, off, len);
            if (n == -1) {
                throw new EOFException("EOF in chunk");
            }
            chunkRemaining -= n;
            if (chunkRemaining == 0) {
                // consume CRLF
                expectCRLF();
            }
            return n;
        }

        private long readChunkSize() throws IOException {
            // Read hex line
            String line = readAsciiLine();
            // ignore chunk extensions if any (";")
            int semi = line.indexOf(';');
            String hex = (semi >= 0) ? line.substring(0, semi) : line;
            hex = trim(hex);
            long val = 0;
            for (int i = 0; i < hex.length(); i++) {
                char c = hex.charAt(i);
                int d;
                if (c >= '0' && c <= '9') {
                    d = c - '0';
                } else if (c >= 'a' && c <= 'f') {
                    d = 10 + (c - 'a');
                } else if (c >= 'A' && c <= 'F') {
                    d = 10 + (c - 'A');
                } else {
                    throw new IOException("Bad chunk size: " + hex);
                }
                val = (val << 4) + d;
            }
            return val;
        }

        private void readTrailers() throws IOException {
            while (true) {
                String line = readAsciiLine();
                if (line.length() == 0) {
                    break; // end of trailers
                }
            }
        }

        private void expectCRLF() throws IOException {
            int c1 = in.read();
            int c2 = in.read();
            if (c1 != '\r' || c2 != '\n') {
                throw new IOException("Bad chunk CRLF");
            }
        }

        private String readAsciiLine() throws IOException {
            ByteArrayOutputStream buf = new ByteArrayOutputStream(64);
            while (true) {
                int b = in.read();
                if (b == -1) {
                    throw new EOFException("EOF in chunk line");
                }
                if (b == '\n') {
                    int size = buf.size();
                    byte[] t = buf.toByteArray();
                    if (size > 0 && t[size - 1] == '\r') {
                        size--;
                    }
                    return new String(t, 0, size, "ISO-8859-1");
                } else {
                    buf.write(b);
                }
            }
        }

        public void close() throws IOException {
            in.close();
        }
    }
}
