/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.alicesworld.ModernConnector;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.crypto.tls.AlertDescription;
import org.bouncycastle.crypto.tls.NameType;
import org.bouncycastle.crypto.tls.TlsFatalAlert;
import org.bouncycastle.crypto.tls.TlsUtils;

/**
 *
 * @author NealShah
 */
public class LegacyServerName extends org.bouncycastle.crypto.tls.ServerName {

    public LegacyServerName(short s, Object o) {
        super(s, o);
    }
    
    public void encode(OutputStream output) throws IOException
    {
        TlsUtils.writeUint8(nameType, output);

        switch (nameType)
        {
        case NameType.host_name:
            
            byte[] asciiEncoding;
            try{
                // Real bouncy castle uses ASCII here which breaks some devices
                asciiEncoding = ((String)name).getBytes("US_ASCII");
            } catch(Exception e) {
                asciiEncoding = ((String)name).getBytes("ASCII");
            }
            
            if (asciiEncoding.length < 1)
            {
                throw new TlsFatalAlert(AlertDescription.internal_error);
            }
            TlsUtils.writeOpaque16(asciiEncoding, output);
            break;
        default:
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }

}
