package io.littlehorse.jlib.common.config;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to configure the LHClient class.
 */
public class LHClientConfig extends ConfigBase {

    private static final Logger log = LoggerFactory.getLogger(LHClientConfig.class);

    /**
     * The bootstrap host for the LH Server.
     */
    public static final String API_HOST_KEY = "LHC_API_HOST";

    /**
     * The bootstrap port for the LH Server.
     */
    public static final String API_PORT_KEY = "LHC_API_PORT";

    /**
     * The Client Id.
     */
    public static final String CLIENT_ID_KEY = "LHC_CLIENT_ID";

    /**
     * Optional location of Client Cert file.
     */
    public static final String CLIENT_CERT_KEY = "LHC_CLIENT_CERT";

    /**
     * Optional location of Client Private Key File.
     */
    public static final String CLIENT_KEY_KEY = "LHC_CLIENT_KEY";

    /**
     * Optional location of CA Cert File.
     */
    public static final String CA_CERT_KEY = "LHC_CA_CERT";

    private static final Set<String> configNames = Collections.unmodifiableSet(
        Set.of(
            LHClientConfig.API_HOST_KEY,
            LHClientConfig.API_PORT_KEY,
            LHClientConfig.CLIENT_ID_KEY,
            LHClientConfig.CLIENT_CERT_KEY,
            LHClientConfig.CLIENT_KEY_KEY,
            LHClientConfig.CA_CERT_KEY
        )
    );

    /**
     * Returns a set of all config names.
     * @return A Set of all config names.
     */
    public static Set<String> configNames() {
        return LHClientConfig.configNames;
    }

    private Map<String, Channel> createdChannels;

    /**
     * Creates an LHClientConfig. Loads default values for config from env vars.
     */
    public LHClientConfig() {
        super();
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with provided config values.
     * @param props configuration values.
     */
    public LHClientConfig(Properties props) {
        super(props);
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with config props in a specified .properties file.
     * @param propLocation the location of the .properties file.
     */
    public LHClientConfig(String propLocation) {
        super(propLocation);
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with provided config values.
     * @param configs configuration values.
     */
    public LHClientConfig(Map<String, Object> configs) {
        super(configs);
        createdChannels = new HashMap<>();
    }

    /**
     * Returns a gRPC channel for the specified host/port combination.
     * @param host The host to connect to.
     * @param port the port to connect to.
     * @return a gRPC channel for that specified host/port combo.
     * @throws IOException if we can't connect.
     */
    public Channel getChannel(String host, int port) throws IOException {
        String hostKey = host + ":" + port;
        if (createdChannels.containsKey(hostKey)) {
            return createdChannels.get(hostKey);
        }

        Channel out;

        String caCertFile = getOrSetDefault(CA_CERT_KEY, null);
        String clientCertFile = getOrSetDefault(CLIENT_CERT_KEY, null);
        String clientKeyFile = getOrSetDefault(CLIENT_KEY_KEY, null);

        if (caCertFile == null) {
            log.warn("Using insecure channel!");
            out = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        } else {
            log.info("Using mtls!");

            File clientCert = new File(clientCertFile);
            File clientKey = new File(clientKeyFile);
            File rootCA = new File(caCertFile);

            TlsChannelCredentials.Builder tlsBuilder = TlsChannelCredentials.newBuilder();
            tlsBuilder.keyManager(clientCert, clientKey);
            tlsBuilder.trustManager(rootCA);

            out =
                Grpc
                    .newChannelBuilderForAddress(host, port, tlsBuilder.build())
                    .build();
        }

        createdChannels.put(hostKey, out);
        return out;
    }

    public String getApiBootstrapHost() {
        return getOrSetDefault(API_HOST_KEY, "localhost");
    }

    public int getApiBootstrapPort() {
        return Integer.valueOf(getOrSetDefault(API_PORT_KEY, "5000"));
    }

    public String getClientId() {
        return getOrSetDefault(
            CLIENT_ID_KEY,
            "client-" + UUID.randomUUID().toString().replaceAll("-", "")
        );
    }

    @Override
    protected String[] getEnvKeyPrefixes() {
        return new String[] { "LHC_" };
    }
}
