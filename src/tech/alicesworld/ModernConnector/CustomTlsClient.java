/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.alicesworld.ModernConnector;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.crypto.tls.CertificateRequest;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.crypto.tls.DefaultTlsClient;
import org.bouncycastle.crypto.tls.NameType;
import org.bouncycastle.crypto.tls.ServerName;
import org.bouncycastle.crypto.tls.ServerNameList;
import org.bouncycastle.crypto.tls.TlsAuthentication;
import org.bouncycastle.crypto.tls.TlsCredentials;
import org.bouncycastle.crypto.tls.TlsExtensionsUtils;

/**
 *
 * @author alice
 */
public class CustomTlsClient extends DefaultTlsClient {

    String sniHost;
    public CustomTlsClient(String sniHost) {
        super();
        this.sniHost = sniHost;
    }
    public CustomTlsClient() {
        this("cloudflare.com");
    }
    
//    // Nokia JVM fix-- Wont pull from above class for some reason?
//    public int[] getCipherSuites() {
//        System.out.println("Inlined Cipher");
//        return new int[] { 49195, 49187, 49161, 49199, 49191, 49171, 162, 64, 50, 158, 103, 51, 156, 60, 47 };
//    }
    
    
    public Hashtable getClientExtensions() throws IOException {
        Hashtable ext = TlsExtensionsUtils.ensureExtensionsInitialised(super.getClientExtensions());
        Vector serverNames = new Vector();
        serverNames.addElement(new LegacyServerName(NameType.host_name, sniHost));
        TlsExtensionsUtils.addServerNameExtension(ext, new ServerNameList(serverNames));
        return ext;
    }
    public TlsAuthentication getAuthentication() throws IOException {
        return new TlsAuthentication() {
            public void notifyServerCertificate(Certificate serverCertificate) throws IOException {
                // Implement certificate validation logic here
                System.out.println("Server certificate received: " + serverCertificate);
            }

            public TlsCredentials getClientCredentials() throws IOException {
                return null; // No client credentials for simplicity
            }

            public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
                throw new IOException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
}
