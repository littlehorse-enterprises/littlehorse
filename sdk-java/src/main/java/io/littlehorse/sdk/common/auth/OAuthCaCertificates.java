package io.littlehorse.sdk.common.auth;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Builds TLS settings for HTTPS calls to an OAuth authorization server signed by a private CA.
 */
public final class OAuthCaCertificates {

    private OAuthCaCertificates() {}

    /**
     * Creates a socket factory that trusts the JVM default CAs plus the CAs in a PEM file.
     *
     * @param caCert PEM file with one or more CA certificates
     * @return socket factory for HTTPS connections
     * @throws LHMisconfigurationException if the file can't be read or has no certificates
     */
    public static SSLSocketFactory sslSocketFactory(File caCert) {
        try (InputStream in = new FileInputStream(caCert)) {
            Collection<? extends Certificate> caCertificates =
                    CertificateFactory.getInstance("X.509").generateCertificates(in);
            if (caCertificates.isEmpty()) {
                throw new LHMisconfigurationException("No certificates found in " + caCert);
            }

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            int index = 0;
            for (X509Certificate systemCa : defaultTrustManager().getAcceptedIssuers()) {
                trustStore.setCertificateEntry("system-" + index++, systemCa);
            }
            for (Certificate ca : caCertificates) {
                trustStore.setCertificateEntry("oauth-ca-" + index++, ca);
            }

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagerFactory.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (IOException | GeneralSecurityException e) {
            throw new LHMisconfigurationException("Invalid CA certificate file " + caCert, e);
        }
    }

    private static X509TrustManager defaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        for (TrustManager trustManager : factory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager x509TrustManager) {
                return x509TrustManager;
            }
        }
        throw new KeyStoreException("No default X509TrustManager found");
    }
}
