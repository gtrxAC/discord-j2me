package com.gtrxac.discord;

import java.io.*;
import javax.microedition.io.*;

//#ifndef NO_BLUETOOTH
import fi.gtrxac.bluewap.http.BluetoothHTTP;
import fi.gtrxac.bluewap.http.HTTP;
//#endif

//#ifdef MODERNCONNECTOR
import tech.alicesworld.ModernConnector.*;
//#endif

public class MultiConnector {
    public static Object open(String url) throws Exception {
//#ifndef NO_BLUETOOTH
        if (HTTP.CONNECTION_TYPE == HTTP.CONNECTION_TYPE_BLUETOOTH) {
            if (BluetoothHTTP.selectedConnectionUrl == null) {
                throw new Exception("Connection mode not selected");
            }
            return new BluetoothHTTPConnection(url);
        }
//#endif

        return
//#ifdef JL_TLS_VERSION
            (Settings.useModcon ? ModernConnector.open(url) : Connector.open(url));
//#else
//#ifdef MODERNCONNECTOR
            ModernConnector
//#else
            Connector
//#endif	
            .open(url);
//#endif
    }
}