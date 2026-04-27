/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.alicesworld.ModernConnector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import javax.microedition.io.Connector;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SecurityInfo;
import javax.microedition.io.SocketConnection;
import org.bouncycastle.crypto.tls.TlsClientProtocol;

/**
 *
 * @author NealShah
 */
public class ModernSecureConnection implements SecureConnection {
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private SocketConnection socket;
    public ModernSecureConnection(String serverHost, int port) throws IOException {            
        String connectionUrl = "socket://" + serverHost + ":" + port;
        socket = (SocketConnection) Connector.open(connectionUrl);
        InputStream plainInput = socket.openInputStream();
        OutputStream plainOutput = socket.openOutputStream();

        // Initialize BouncyCastle TLS protocol
        TlsClientProtocol tlsProtocol = new TlsClientProtocol(plainInput, plainOutput, new SecureRandom());
        CustomTlsClient tlsClient = new CustomTlsClient(serverHost);
        tlsProtocol.connect(tlsClient);
        // Wrap the plain input and output streams with TLS streams
        inputStream = new DataInputStream(tlsProtocol.getInputStream());
        outputStream = new DataOutputStream(tlsProtocol.getOutputStream());
    }
    
    public SecurityInfo getSecurityInfo() throws IOException {
        throw new RuntimeException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getLocalPort() throws IOException {
        return socket.getLocalPort();
    }

    public int getPort() throws IOException {
        return socket.getPort();
    }

    public int getSocketOption(byte option) throws IOException {
        return socket.getSocketOption(option);
    }

    public String getAddress() throws IOException {
        return socket.getAddress();
    }

    public String getLocalAddress() throws IOException {
        return socket.getLocalAddress();
    }

    public void setSocketOption(byte option, int value) throws IOException {
        socket.setSocketOption(option, value);    
    }

    public void close() throws IOException {
        socket.close();
    }
    
    public DataInputStream openDataInputStream() throws IOException {
        return inputStream;
    }

    public InputStream openInputStream() throws IOException {
        return inputStream;
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return outputStream;
    }

    public OutputStream openOutputStream() throws IOException {
        return outputStream;
    }
    
}
