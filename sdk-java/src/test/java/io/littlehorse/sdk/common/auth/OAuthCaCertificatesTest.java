package io.littlehorse.sdk.common.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class OAuthCaCertificatesTest {

    @TempDir
    Path tempDir;

    @Test
    void createsSocketFactoryFromPemFile() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        X509Certificate anyCa = ((X509TrustManager) factory.getTrustManagers()[0]).getAcceptedIssuers()[0];
        String pem = "-----BEGIN CERTIFICATE-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(anyCa.getEncoded())
                + "\n-----END CERTIFICATE-----\n";
        Path caCert = Files.writeString(tempDir.resolve("ca.crt"), pem);

        assertNotNull(OAuthCaCertificates.sslSocketFactory(caCert.toFile()));
    }

    @Test
    void failsWhenFileHasNoCertificates() throws Exception {
        Path caCert = Files.writeString(tempDir.resolve("ca.crt"), "not a certificate");

        assertThrows(LHMisconfigurationException.class, () -> OAuthCaCertificates.sslSocketFactory(caCert.toFile()));
    }

    @Test
    void failsWhenFileDoesNotExist() {
        File caCert = tempDir.resolve("missing.crt").toFile();

        assertThrows(LHMisconfigurationException.class, () -> OAuthCaCertificates.sslSocketFactory(caCert));
    }
}
