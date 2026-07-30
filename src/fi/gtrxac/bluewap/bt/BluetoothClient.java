//#ifndef NO_BLUETOOTH
package fi.gtrxac.bluewap.bt;

import javax.bluetooth.*;
import java.util.*;
import javax.microedition.io.*;

public class BluetoothClient implements DiscoveryListener, Runnable {
	private UUID[] uuids;
    private String serviceName;
    private BluetoothClientListener listener;

	private DiscoveryAgent discAgent;
    private RemoteDevice selectedDevice;
    private int state;

    private static final int STATE_IDLE = 0;
    private static final int STATE_SEARCHING = 1;
    private static final int STATE_DISCOVERING_SERVICES = 2;
    private static final int STATE_WAITING_TO_CONNECT = 3;

    /**
     * Create a new Bluetooth manager instance.
     * Bluetooth applications are identified using an UUID string and a service name. These should be unique across different apps. The server and the client for the same app should share the same UUID and service name.
     * 
     * @param uuid 32-character string containing the UUID to use for identifying this application, for example generated with uuidgen (with the dashes removed)
     * @param serviceName Name to use for identifying this application's service
     * @param listener Listener to use for Bluetooth event callbacks
     */
	public BluetoothClient(String uuid, String serviceName, BluetoothClientListener listener) {
        this.uuids = new UUID[] { new UUID(uuid, false) };
        this.serviceName = serviceName;
        this.listener = listener;
        this.state = STATE_IDLE;
	}

    /**
     * Get a list of previously known devices returned by the system (e.g. paired devices).
     * Many devices do not support this feature. If there are no known devices or if the device does not keep track of known devices, an empty array is returned.
     */
    public RemoteDevice[] getKnownDevices() {
        try {
            setupDiscoveryAgent();
            RemoteDevice[] result = discAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
            if (result != null) return result;
        }
        catch (Exception e) {}

        return new RemoteDevice[]{}; 
    }

    /**
     * Automatically find and connect to the first found device that is running the server for this application.
     * This feature should be used with caution, as the 
     */
    public synchronized void autoConnect() {
        if (state != STATE_IDLE) return;

        try {
            setupDiscoveryAgent();
            new Thread(this).start();
            state = STATE_SEARCHING;
        }
        catch (Exception e) {
            handleError(e);
        }
    }

    public void run() {
        try {
            String url = discAgent.selectService(
                uuids[0], ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);

            if (url != null) {
                listener.bluetoothConnected(url);
            } else {
                listener.bluetoothSearchError(new Exception("no devices found"));
            }
        }
        catch (Exception e) {
            handleError(e);
        }
        state = STATE_IDLE;
    }

    /**
     * Start searching for other devices.
     * The bluetoothDeviceFound callback will be run for every device that was found.
     */
    public synchronized void search() {
        if (state != STATE_IDLE) return;

        try {
            setupDiscoveryAgent();
            discAgent.startInquiry(DiscoveryAgent.GIAC, this);
            state = STATE_SEARCHING;
        }
        catch (Exception e) {
            handleError(e);
        }
	}

    /**
     * Cancel a running search for other devices.
     */
	public synchronized void stopSearching() {
        if (state != STATE_SEARCHING) return;
		discAgent.cancelInquiry(this);
		state = STATE_IDLE;
	}

    /**
     * Check if a device search is currently in progress.
     */
    public synchronized boolean isSearching() {
        return state == STATE_SEARCHING;
    }

    /**
     * Check if a device connection is currently being established.
     */
    public synchronized boolean isConnecting() {
        return state == STATE_WAITING_TO_CONNECT || state == STATE_DISCOVERING_SERVICES;
    }

    /**
     * Attempt to connect to a device that was returned by getKnownDevices or the listener's bluetoothDeviceFound callback.
     * The other device must be running a BluetoothServer with the same service name and UUID.
     * When connected, the bluetoothConnected callback is called with an URL which can be used to open a StreamConnection.
     */
    public synchronized void connect(RemoteDevice device) {
        if (isConnecting()) return;

        selectedDevice = device;

        if (state == STATE_SEARCHING) {
            state = STATE_WAITING_TO_CONNECT;
            stopSearching();
            // connection happens in inquiryCompleted
        } else {
            discoverServices();
        }
    }

    private void handleError(Exception e) {
        // Symbian error code
        if (e.toString().indexOf("-44") != -1) {
            e = new Exception("Bluetooth is not enabled");
        }
        listener.bluetoothSearchError(e);
    } 

    private synchronized void setupDiscoveryAgent() throws Exception {
        if (discAgent != null) return;
        LocalDevice local = LocalDevice.getLocalDevice();

        if (local == null) {
            throw new Exception("cannot get local device, make sure Bluetooth is supported and enabled");
        }
        discAgent = local.getDiscoveryAgent();
    }

    private synchronized void discoverServices() {
        try {
            discAgent.searchServices(null, uuids, selectedDevice, this);
            state = STATE_DISCOVERING_SERVICES;
        }
        catch (Exception e) {
            listener.bluetoothConnectError(e);
        }
    }

	public synchronized void deviceDiscovered(RemoteDevice device, DeviceClass cod) {
        String name = device.getBluetoothAddress();

        String friendlyName = null;
        try {
            friendlyName = device.getFriendlyName(true);
        }
        catch (Exception e) {}

        if (friendlyName != null && friendlyName.length() != 0) {
            name = friendlyName;
        }
        listener.bluetoothDeviceFound(name, device, cod);
	}

    public synchronized void inquiryCompleted(int discType) {
        if (state == STATE_WAITING_TO_CONNECT) {
            discoverServices();
        }
        else if (state == STATE_SEARCHING) {
            if (discType == INQUIRY_ERROR) {
                listener.bluetoothSearchError(new Exception("device search failed"));
            } else {
                listener.bluetoothSearchCompleted();
            }
            state = STATE_IDLE;
        }
    }

    public synchronized void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if (state != STATE_DISCOVERING_SERVICES) return;

        if (servRecord.length == 0 || servRecord[0] == null) {
            Exception e = new Exception("cannot connect, make sure the app is running on the other device");
            listener.bluetoothConnectError(e);
            return;
        }
        String url = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
        listener.bluetoothConnected(url);
        state = STATE_IDLE;
    }

    public synchronized void serviceSearchCompleted(int transID, int respCode) {
        if (state != STATE_DISCOVERING_SERVICES) return;

        if (respCode == SERVICE_SEARCH_DEVICE_NOT_REACHABLE) {
            Exception e = new Exception("device unreachable");
            listener.bluetoothConnectError(e);
        }
        else if (respCode != SERVICE_SEARCH_COMPLETED) {
            Exception e = new Exception("cannot connect, make sure the app is running on the other device (error " + respCode + ")");
            listener.bluetoothConnectError(e);
        }
        state = STATE_IDLE;
    }
}
//#endif