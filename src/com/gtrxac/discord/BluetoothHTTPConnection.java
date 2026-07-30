//#ifndef NO_BLUETOOTH
package com.gtrxac.discord;

import javax.microedition.io.*;
import java.io.*;
import fi.gtrxac.bluewap.http.HTTP;

public class BluetoothHTTPConnection implements HttpConnection {
    private HTTP h;
    private String url;
    private String method;
    private ByteArrayOutputStream baos;

    public BluetoothHTTPConnection(String url) {
        this.url = url;
    }

    private void init() throws IOException {
        if (h != null) return;
        if (method == null) method = "GET";

        try {
            h = HTTP.createRequest(method, url);
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private void setData() {
        if (baos == null) return;
        h.setData(baos.toByteArray());
    }
     
    public long getDate() {
        return 0;  // unused
    }

    public long getExpiration() {
        return 0;  // unused
    }

    public String getFile() {
        return null;  // unused
    }

    public String getHeaderField(int n) {
        return null;  // unused
    }

    public String getHeaderField(String name) throws IOException {
        setData();

        try {
            return h.getResponseHeader(name);
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public long getHeaderFieldDate(String name, long def) {
        return 0;  // unused
    }

    public int getHeaderFieldInt(String name, int def) {
        return 0;  // unused
    }

    public String getHeaderFieldKey(int n) {
        return null;  // unused
    }

    public String getHost() {
        return null;  // unused
    }

    public long getLastModified() {
        return 0;  // unused
    }

    public int getPort() {
        return 0;  // unused
    }

    public String getProtocol() {
        return null;  // unused
    }

    public String getQuery() {
        return null;  // unused
    }

    public String getRef() {
        return null;  // unused
    }

    public String getRequestMethod() {
        return null;  // unused
    }

    public String getRequestProperty(String key) {
        return null;  // unused
    }

    public int getResponseCode() throws IOException {
        init();
        setData();

        try {
            return h.getResponseCode();
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public String getResponseMessage() {
        return null;  // unused
    }

    public String getURL() {
        return null;  // unused
    }

    public void setRequestMethod(String method) {
        this.method = method;
    }

    public void setRequestProperty(String key, String value) throws IOException {
        init();
        h.setHeader(key, value);
    }

    public void close() {
        if (h != null) h.close();
    }

    public String getEncoding() {
        return null;  // unused
    }

    public long getLength() {
        return 0;  // may return 0 even with normal HTTP
    }

    public String getType() {
        return null;  // unused
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public InputStream openInputStream() throws IOException {
        init();
        setData();

        try {
            return h.getResponseStream();
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public DataOutputStream openDataOutputStream() {
        return new DataOutputStream(openOutputStream());
    }

    public OutputStream openOutputStream() {
        baos = new ByteArrayOutputStream();
        return baos;
    }
}
//#endif