//#ifndef NO_BLUETOOTH
package com.gtrxac.discord;

import javax.bluetooth.*;
import java.util.*;
import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.bt.*;
import fi.gtrxac.bluewap.http.*;
import javax.microedition.lcdui.*;

public class ConnectionScreen extends ListScreen implements BluetoothClientListener, CommandListener, Strings {
    private Vector devices = new Vector();
    private BluetoothClient client;
    private Command quitCommand;

    public ConnectionScreen() {
        super(Locale.get(CONNECTION_SCREEN_TITLE), false, false, false);
        setCommandListener(this);

        App.ic = null;
        App.ic = new Icons(Icons.TYPE_CONNECTION_SCREEN);

        initClient();
        clearAndRefresh();

        quitCommand = Locale.createCommand(QUIT, Command.EXIT, 0);
        addCommand(quitCommand);
    }

    private void initClient() {
        if (client != null) return;
        client = new BluetoothClient(Config.BLUETOOTH_UUID, Config.BLUETOOTH_SERVICE, this);
    }

    private void clearAndRefresh() {
        int lastSel = getSelectedIndex();

        devices.removeAllElements();
        deleteAll();
        append(Locale.get(CONNECTION_CELL_WIFI), App.ic.connectionCell);

        int searchText = client.isSearching() ? CONNECTION_SEARCHING : CONNECTION_BT_SEARCH;

        append(Locale.get(searchText), App.ic.connectionBt);
        append(Locale.get(CONNECTION_HELP), App.ic.help);

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
            append(Locale.get(NO_DEVICES_FOUND), null);
            App.error(Locale.get(NO_DEVICES_FOUND_DESCRIPTION));
        } else {
            append(Locale.get(SEARCH_COMPLETED), null);
        }
        set(1, Locale.get(CONNECTION_BT_SEARCH), App.ic.connectionBt);
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
        if (c == quitCommand) {
            App.instance.notifyDestroyed();
            return;
        }

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
                String help = "- " + Locale.get(CONNECTION_HELP_BT_SEARCH)
//#ifdef PROXYLESS_SUPPORT
                + "\n- " + Locale.get(CONNECTION_HELP_PROXYLESS)
//#endif
                + "\n- " + Locale.get(CONNECTION_HELP_BW_SERVER);

                App.disp.setCurrent(new Dialog(Locale.get(CONNECTION_HELP_TITLE), help));
                break;
            }
            default: {
                // select device
                int devIndex = getSelectedIndex() - 3;
                if (devIndex < 0 || devIndex >= devices.size()) {
                    // selected "search completed" or "no devices found" item - do nothing
                    break;
                }

                set(getSelectedIndex(), Locale.get(CONNECTION_CONNECTING), App.ic.connectionDevice);
                
                RemoteDevice dev = (RemoteDevice) devices.elementAt(devIndex);
                initClient();
                client.connect(dev);
            }
        }
    }
}
//#endif