package fi.gtrxac.bluewap.http;

/**
 * BlueWAP HTTP over Bluetooth protocol versions for backwards compatibility purposes 
 */
public interface BluetoothHTTPProtocol {
	public static final byte PROTOCOL_BASE = 1;
	public static final byte PROTOCOL_ADDED_RESULT_URL = 2;
	public static final byte PROTOCOL_CURRENT = 2;
}