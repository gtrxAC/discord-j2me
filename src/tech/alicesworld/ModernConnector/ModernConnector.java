/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.alicesworld.ModernConnector;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author NealShah
 */
public class ModernConnector {
    public static Object socketImplementation;
    
    public static Object open(String connectionURL) throws IOException {
        String protocol = Utils.split(connectionURL, ':')[0];
        if (protocol.equals("ssl") || protocol.equals("tls")) {
            String endpoint = Utils.split(connectionURL.substring(6), '/')[0];
            String[] endpointData = Utils.split(endpoint, ':');
            String host = endpointData[0];
            int port = 443;
            try {
                port = Integer.parseInt(endpointData[1]);
            } catch (Exception e) {
                // Nothing
            }

            return new ModernSecureConnection(host, port);
        }
        if (protocol.equals("ws")) {
            String endpoint = Utils.split(connectionURL.substring(5), '/')[0];
            String[] endpointData = Utils.split(endpoint, ':');
            String host = endpointData[0];
            int port = 80;
            try {
                port = Integer.parseInt(endpointData[1]);
            } catch (Exception e) {
                // Nothing
            }
            SocketConnection socket = (SocketConnection) ModernConnector.open("socket://" + host + ":" + port);
            WebSocketClient ws = new WebSocketClient(host, port);
            ws.connectWithSocket(socket);
            return ws;
        }
        if (protocol.equals("wss")) {
            String endpoint = Utils.split(connectionURL.substring(6), '/')[0];

            String[] endpointData = Utils.split(endpoint, ':');
            String host = endpointData[0];
            int port = 443;
            try {
                port = Integer.parseInt(endpointData[1]);
            } catch (Exception e) {
                // Nothing
            }
            SocketConnection socket = (SocketConnection) ModernConnector.open("tls://" + host + ":" + port);
            WebSocketClient ws = new WebSocketClient(host, port);
            ws.connectWithSocket(socket);
            return ws;
        }
        if (protocol.equals("https")) {
            String endpoint = Utils.split(connectionURL.substring(8), '/')[0];

            String[] endpointData = Utils.split(endpoint, ':');
            String host = endpointData[0];
            int port = 443;
            try {
                port = Integer.parseInt(endpointData[1]);
            } catch (Exception e) {
                // Nothing
            }
            SecureConnection socket = (SecureConnection) ModernConnector.open("tls://" + host + ":" + port);
            return new ModernHTTPSConnection(socket, connectionURL);
        }
        
        if (protocol.equals("socket") && ModernConnector.socketImplementation != null) {
            String endpoint = Utils.split(connectionURL.substring(8), '/')[0];

            String[] endpointData = Utils.split(endpoint, ':');
            String host = endpointData[0];
            int port = 443;
            try {
                port = Integer.parseInt(endpointData[1]);
            } catch (Exception e) {
                // Nothing
            }
            
            return ((ModernConnector) ModernConnector.socketImplementation).openSocket(host, port);
        }

        return Connector.open(connectionURL);
    }

    public Object openSocket(String host, int port) {
        throw new RuntimeException("Only supported when new upstream is set");
    }



}
