/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.alicesworld.ModernConnector;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author NealShah
 */
public class WebSocketConnection implements SocketConnection {
    SocketConnection socket;
    WebSocketClient ws;
    /**
     *
     * @param socket
     */
    public WebSocketConnection(SocketConnection socket) throws IOException {
        this.socket = socket;
        ws = new WebSocketClient();
        ws.connectWithSocket(socket);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // start size optional
        DataOutputStream dos = new DataOutputStream(baos);
//        DataInputStream inp = new DataInputStream(dos)
        
        
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
        throw new RuntimeException("WebSocket's aren't really streams."); //To change body of generated methods, choose Tools | Templates.
    }

    public InputStream openInputStream() throws IOException {
        throw new RuntimeException("WebSocket's aren't really streams."); //To change body of generated methods, choose Tools | Templates.
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        throw new RuntimeException("WebSocket's aren't really streams."); //To change body of generated methods, choose Tools | Templates.
    }

    public OutputStream openOutputStream() throws IOException {
        throw new RuntimeException("WebSocket's aren't really streams."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void sendMessage(String data) throws IOException {
        ws.sendMessage(data);
    }
    public void sendMessage(byte[] data) throws IOException {
        ws.sendMessage(data);
    }
    public byte[] receiveMessageBinary() throws IOException {
        return ws.receiveMessageBinary();
    }
    public String recieveMessageString() throws IOException {
        return ws.receiveMessageString();
    }
    
}
