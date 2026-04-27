/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.alicesworld.ModernConnector;

/**
 *
 * @author aliceindisarray/Rafflesia
 */
import java.io.*;
import java.security.SecureRandom;
import java.util.Random;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import org.bouncycastle.crypto.tls.TlsClientProtocol;

public class WebSocketClient {

    private String serverUrl;
    private int port;
    private SocketConnection socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public WebSocketClient(String serverUrl, int port) {
        this.serverUrl = serverUrl;
        this.port = port;
    }

    public WebSocketClient() {
    }

    public boolean connect() {
        try {
            // Open socket connection
            String connectionUrl = "socket://" + serverUrl + ":" + port + ";deviceside=true;interface=wifi";
            socket = (SocketConnection) Connector.open(connectionUrl);
            inputStream = socket.openDataInputStream();
            outputStream = socket.openDataOutputStream();

            // Perform WebSocket handshake
            String handshakeRequest = generateHandshakeRequest();
            outputStream.write(handshakeRequest.getBytes());
            outputStream.flush();

            // Read handshake response
            String response = readResponse();
            if (response.startsWith("HTTP/1.1 101")) {
                return true; // Handshake successful
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean connectTLS() {
        try {
            // Create socket connection to the server
            String connectionUrl = "socket://" + serverUrl + ":" + port;
            socket = (SocketConnection) Connector.open(connectionUrl);
            InputStream plainInput = socket.openInputStream();
            OutputStream plainOutput = socket.openOutputStream();

            // Initialize BouncyCastle TLS protocol           
            TlsClientProtocol tlsProtocol = new TlsClientProtocol(plainInput, plainOutput, (SecureRandom) new SecureRandom());
            CustomTlsClient tlsClient = new CustomTlsClient();
            tlsProtocol.connect(tlsClient);
            // Wrap the plain input and output streams with TLS streams
            inputStream = new DataInputStream(tlsProtocol.getInputStream());
            outputStream = new DataOutputStream(tlsProtocol.getOutputStream());
            // Perform WebSocket handshake
            String handshakeRequest = generateHandshakeRequest();
            outputStream.write(handshakeRequest.getBytes());
            outputStream.flush();

            // Read handshake response
            String response = readResponse();
            if (response.startsWith("HTTP/1.1 101")) {
                return true; // Handshake successful
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean connectWithSocket(SocketConnection socket) throws IOException {
        inputStream = socket.openDataInputStream();
        outputStream = socket.openDataOutputStream();

        // Perform WebSocket handshake
        String handshakeRequest = generateHandshakeRequest();
        outputStream.write(handshakeRequest.getBytes());
        outputStream.flush();

        // Read handshake response
        String response = readResponse();
        if (response.startsWith("HTTP/1.1 101")) {
            return true; // Handshake successful
        }
        return false;
    }

    private String generateHandshakeRequest() {
        String secWebSocketKey = generateSecWebSocketKey();
        return "GET / HTTP/1.1\r\n"
                + "Host: " + serverUrl + ":" + port + "\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Key: " + secWebSocketKey + "\r\n"
                + "Sec-WebSocket-Version: 13\r\n"
                + "\r\n";
    }

    private String generateSecWebSocketKey() {
//        byte[] maskingKey = new byte[16];
//        Random random = new Random();
//        for (int i = 0; i < maskingKey.length; i++) {
//            maskingKey[i] = (byte) (random.nextInt(256)); // Generate random byte
//        }
//        return Base64.encode(maskingKey,0,maskingKey.length);
        return "CAr9RJLjmOky9oH1iXgusg==";//  No clue why the above code isn't working.
    }

    private String readResponse() throws IOException {
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            responseBuffer.write(ch);
            if (responseBuffer.size() > 4) {
                // Check for the end of headers (CRLFCRLF)
                String response = responseBuffer.toString();

                if (response.endsWith("\r\n\r\n")) {
                    break;
                }
            }
        }
        return responseBuffer.toString();
    }

    public boolean unreadData() throws IOException {
        return inputStream.available() == 0;
    }

    public void sendMessage(String message) throws IOException {
        byte[] payload = message.getBytes();
        sendMessage(payload);
    }

    public void sendMessage(byte[] payload) throws IOException {

        ByteArrayOutputStream frame = new ByteArrayOutputStream();

        // Add frame header
        frame.write(0x81); // FIN + opcode (text frame)
        if (payload.length <= 125) {
            frame.write(0x80 | payload.length); // MASK bit set to 1
        } else if (payload.length <= 65535) {
            frame.write(0x80 | 126); // MASK bit set to 1
            frame.write((payload.length >> 8) & 0xFF);
            frame.write(payload.length & 0xFF);
        } else {
            throw new IOException("Payload too large");
        }

        // Generate masking key (4 random bytes)
        byte[] maskingKey = new byte[4];
        Random random = new Random();
        for (int i = 0; i < maskingKey.length; i++) {
            // maskingKey[i] = (byte) (random.nextInt(256)); // Generate random byte
            maskingKey[i] = (byte) (random.nextInt() & 255); // Generate random byte
        }
        frame.write(maskingKey);

        // Mask the payload
        byte[] maskedPayload = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            maskedPayload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
        }

        frame.write(maskedPayload);

        // Send the frame
        outputStream.write(frame.toByteArray());
        outputStream.flush();
    }

    public byte[] receiveMessageBinary() throws IOException {
        int firstByte = inputStream.read();
        if (firstByte == -1) {
            throw new IOException("Connection closed");
        }

        int opcode = firstByte & 0x0F;
        System.out.println("Opcode: " + opcode);
        if (opcode != 0x01 && opcode != 0x02) { // Check if it's a text frame or binary frame
//            throw new IOException("Unsupported frame type");
            return new byte[0];
        }

        int secondByte = inputStream.read();
        int payloadLength = secondByte & 0x7F;

        if (payloadLength == 126) {
            payloadLength = (inputStream.read() << 8) | inputStream.read();
        } else if (payloadLength == 127) {
            throw new IOException("Unsupported payload length");
        }

        byte[] payload = new byte[payloadLength];
        inputStream.readFully(payload);

        return payload;
    }

    public String receiveMessageString() throws IOException {
        int firstByte = inputStream.read();
        if (firstByte == -1) {
            throw new IOException("Connection closed");
        }

        int opcode = firstByte & 0x0F;
        if (opcode != 0x01) { // Check if it's a text frame
            return "";
//            throw new IOException("Unsupported frame type");

        }

        int secondByte = inputStream.read();
        int payloadLength = secondByte & 0x7F;

        if (payloadLength == 126) {
            payloadLength = (inputStream.read() << 8) | inputStream.read();
        } else if (payloadLength == 127) {
            throw new IOException("Unsupported payload length");
        }

        byte[] payload = new byte[payloadLength];
        inputStream.readFully(payload);

        return new String(payload);
    }

    public void close() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
