//#ifndef NO_BLUETOOTH
package fi.gtrxac.bluewap.bt;

import javax.bluetooth.*;
import java.util.*;
import javax.microedition.io.*;

public interface BluetoothClientListener {
    public void bluetoothDeviceFound(String name, RemoteDevice device, DeviceClass cod);
    public void bluetoothSearchCompleted();
    public void bluetoothSearchError(Exception e);

    public void bluetoothConnected(String url);
    public void bluetoothConnectError(Exception e);
}
//#endif