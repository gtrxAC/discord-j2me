package com.gtrxac.discord;

import javax.microedition.io.*;
import fi.gtrxac.bluewap.http.HTTP;
import java.io.*;

public class BluetoothSocketConnection implements SocketConnection {
    private static final int POLLING_RATE_MS = 5000;

    private String connectionUrl;

    public BluetoothSocketConnection() throws Exception {
        HTTP h = HTTP.createRequest("discord://connect");
        String resp = h.getResponseString();

        if (h.getResponseCode() != 200) {
            throw new Exception(resp);
        }
        connectionUrl = "discord://" + resp;
    }

    public String getAddress() {
        return null;  // unused
    }

    public String getLocalAddress() {
        return null;  // unused
    }

    public int getLocalPort() {
        return 0;  // unused
    }

    public int getPort() {
        return 0;  // unused
    }

    public int getSocketOption(byte option) {
        return 0;  // unused
    }

    public void setSocketOption(byte option, int value) {
        // no op
    }

    public DataInputStream openDataInputStream() {
        return null;  // unused
    }

    public InputStream openInputStream() {
        return new InputStream() {
            private byte[] receiveBuffer = new byte[0];
            private int receiveBufferPos = 0;

            public int read() throws IOException {
                try {
                    return readImpl();
                }
                catch (Exception e) {
                    throw new IOException(e.getMessage());
                }
            }

            private int readImpl() throws Exception {
                if (receiveBuffer.length == 0 || receiveBufferPos == receiveBuffer.length) {
                    while (true) {
                        HTTP h = HTTP.createRequest("GET", connectionUrl);

                        if (h.getResponseCode() != 200) {
                            throw new IOException();
                        }
                        receiveBuffer = h.getResponseBytes();
                        if (receiveBuffer.length != 0) {
                            receiveBufferPos = 0;
                            break;
                        }
                        Util.sleep(POLLING_RATE_MS);
                    }
                }
                return receiveBuffer[receiveBufferPos++];
            }
        };
    }

    public DataOutputStream openDataOutputStream() {
        return null;  // unused
    }

    public OutputStream openOutputStream() {
        return new OutputStream() {
            public void write(int b) throws IOException {
                // unused
                throw new IOException("not implemented");
            }

            public void write(byte[] b) throws IOException {
                try {
                    HTTP h = HTTP.createRequest("POST", connectionUrl);
                    h.setData(b);
                    h.getResponseBytes();

                    if (h.getResponseCode() != 200) {
                        throw new IOException();
                    }
                }
                catch (Exception e) {
                    throw new IOException(e.getMessage());
                }
            }
        };
    }

    public void close() {
        try {
            HTTP h = HTTP.createRequest("DELETE", connectionUrl);
            h.getResponseBytes();
        }
        catch (Exception e) {}
    }
}