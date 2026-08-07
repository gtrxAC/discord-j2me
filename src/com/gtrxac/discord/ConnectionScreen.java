//#ifndef NO_BLUETOOTH
package com.gtrxac.discord;

import javax.bluetooth.*;
import java.util.*;
import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.bt.*;
import fi.gtrxac.bluewap.http.*;
import javax.microedition.lcdui.*;

public class ConnectionScreen extends ListScreen implements BluetoothClientListener, CommandListener {
    private Vector devices = new Vector();
    private BluetoothClient client;

    public ConnectionScreen() {
        super("Connection", false, false, false);
        setCommandListener(this);

        App.ic = null;
        App.ic = new Icons(Icons.TYPE_CONNECTION_SCREEN);

        initClient();
        clearAndRefresh();
    }

    private void initClient() {
        if (client != null) return;
        client = new BluetoothClient(Config.BLUETOOTH_UUID, Config.BLUETOOTH_SERVICE, this);
    }

    private void clearAndRefresh() {
        int lastSel = getSelectedIndex();

        devices.removeAllElements();
        deleteAll();
        append("Cellular/Wi-Fi", App.ic.connectionCell);
        append(client.isSearching() ? "Searching..." : "Bluetooth search", App.ic.connectionBt);
        append("Help", App.ic.help);

        int maxItem = 3 + devices.size() - 1;
        setSelectedIndex(Math.min(lastSel, maxItem), true);
    }

    private void searchDevices(boolean autoConnect) {
        initClient();
        if (client.isSearching()) return;
        client.search();
        clearAndRefresh();
    }

    private void addDeviceItem(String name, RemoteDevice device) {
        devices.addElement(device);
        append(name, App.ic.connectionDevice);
    }

    public void bluetoothDeviceFound(String name, RemoteDevice device, DeviceClass cod) {
        if (devices.size() == 0) {
            clearAndRefresh();
        }
        addDeviceItem(name, device);
    }

    public void bluetoothSearchCompleted() {
        if (devices.size() == 0) {
            clearAndRefresh();
            append("No devices found", null);
            App.error("No devices found. Make sure the server device is set to visible, then try again.");
        } else {
            append("Search completed", null);
        }
        set(1, "Bluetooth search", App.ic.connectionBt);
    }

    public void bluetoothSearchError(Exception e) {
        e.printStackTrace();
        App.error(e);
    }

    public void bluetoothConnected(String url) {
        fi.gtrxac.bluewap.http.HTTP.CONNECTION_TYPE =
            fi.gtrxac.bluewap.http.HTTP.CONNECTION_TYPE_BLUETOOTH;
            
        BluetoothHTTP.selectedConnectionUrl = url;
        App.continueStartApp();
    }

    public void bluetoothConnectError(Exception e) {
        e.printStackTrace();
        App.error(e);
    }

    public void commandAction(Command c, Displayable d) {
        switch (getSelectedIndex()) {
            case 0: {
                // cell/wifi
                App.continueStartApp();
                break;
            }
            case 1: {
                // bt search
                searchDevices(false);
                break;
            }
            case 2: {
                // help
                String help = "- Select 'Bluetooth search' to tether to a device running BlueWAP Server.";

//#ifdef PROXYLESS_SUPPORT
                help += "\n- Use Direct connection if your server device supports TLS 1.2 (e.g. Android 5+).";
//#endif
                help += "\n- Get BlueWAP Server: github.com/gtrxAC/BlueWAP";

                App.disp.setCurrent(new Dialog("Help", help));
                break;
            }
            default: {
                // select device
                int devIndex = getSelectedIndex() - 3;
                if (devIndex < 0 || devIndex >= devices.size()) {
                    // selected "search completed" or "no devices found" item - do nothing
                    break;
                }

                set(getSelectedIndex(), "Connecting...", App.ic.connectionDevice);
                
                RemoteDevice dev = (RemoteDevice) devices.elementAt(devIndex);
                initClient();
                client.connect(dev);
            }
        }
    }
}
//#endif