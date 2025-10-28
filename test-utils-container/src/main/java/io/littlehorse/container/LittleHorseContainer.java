package io.littlehorse.container;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

/**
 * <p>
 * LH Testcontainers implementation
 * </p>
 *
 * Example of using:
 * <blockquote><pre>
 * public LittleHorseContainer littleHorseInstance = new LittleHorseContainer(littlehorseImage)
 *             .withKafkaBootstrapServers(kafkaBootstrapServers)
 *             .withInstanceId(instanceId)
 *             .withInternalAdvertisedHost(internalHostname) // unique hostname for each instance
 *             .withNetwork(dockerNetwork)
 *             .dependsOn(kafka);
 * </pre></blockquote>
 *
 * Example of using through LittleHorseCluster (@see {@link LittleHorseCluster#newBuilder()}):
 * <blockquote><pre>
 * {@code @Container}
 * public LittleHorseCluster littleHorseCluster = LittleHorseCluster.newBuilder()
 *             .withInstances(2)
 *             .withKafkaImage("apache/kafka-native:latest")
 *             .withLittlehorseImage("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:master")
 *             .build();
 * </pre></blockquote>
 */
public class LittleHorseContainer extends GenericContainer<LittleHorseContainer> {

    private static final String LHS_INTERNAL_ADVERTISED_HOST = "LHS_INTERNAL_ADVERTISED_HOST";
    private static final String LHS_INSTANCE_ID = "LHS_INSTANCE_ID";
    private static final String LHS_CORE_STREAM_THREADS = "LHS_CORE_STREAM_THREADS";
    private static final String LHS_KAFKA_BOOTSTRAP_SERVERS = "LHS_KAFKA_BOOTSTRAP_SERVERS";
    private static final String LHS_ADVERTISED_LISTENERS = "LHS_ADVERTISED_LISTENERS";

    private static final String LHC_API_HOST = "LHC_API_HOST";
    private static final String LHC_API_PORT = "LHC_API_PORT";

    private static final String CONTAINER_COMMAND = "server";
    private static final String CONTAINER_CONFIG_PATH = "/lh/config.properties";

    private static final int DEFAULT_INTERNAL_PORT = 2023;
    private static final int DEFAULT_HEALTHCHECK_PORT = 1822;
    private static final DockerImageName DEFAULT_IMAGE_NAME =
            DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server");
    private static final String DEFAULT_KAFKA_BOOTSTRAP_SERVERS = "kafka:19092";
    private static final String DEFAULT_CORE_STREAM_THREADS = "2";
    private static final String DEFAULT_INTERNAL_ADVERTISED_HOST = "littlehorse";
    private static final int DEFAULT_INSTANCE_ID = 1;
    private static final String DEFAULT_READINESS_CHECK_PATH = "/readiness";

    private int instanceId;
    private String kafkaBootstrapServers;
    private String internalAdvertisedHost;

    /**
     * Create LittleHorse Testcontainers Wrapper.
     *
     * @param littlehorseImage Example: "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest"
     */
    public LittleHorseContainer(final String littlehorseImage) {
        this(DockerImageName.parse(littlehorseImage));
    }

    /**
     * Create LittleHorse Testcontainers Wrapper.
     *
     * @param littlehorseImage Example: {@code DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest")}
     */
    public LittleHorseContainer(final DockerImageName littlehorseImage) {
        super(littlehorseImage);
        littlehorseImage.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        this.withCommand(CONTAINER_COMMAND, CONTAINER_CONFIG_PATH)
                .withExposedPorts(DEFAULT_INTERNAL_PORT, DEFAULT_HEALTHCHECK_PORT)
                .withInstanceId(DEFAULT_INSTANCE_ID)
                .withKafkaBootstrapServers(DEFAULT_KAFKA_BOOTSTRAP_SERVERS)
                .withInternalAdvertisedHost(DEFAULT_INTERNAL_ADVERTISED_HOST)
                .waitingFor(Wait.forHttp(DEFAULT_READINESS_CHECK_PATH).forPort(DEFAULT_HEALTHCHECK_PORT));
    }

    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        StringWriter writer = new StringWriter();
        try {
            getServerProperties().store(writer, null);
        } catch (IOException e) {
            throw new ContainerLaunchException("Error generating LH configuration file", e);
        }
        this.copyFileToContainer(Transferable.of(writer.toString(), 777), CONTAINER_CONFIG_PATH);
    }

    /**
     * Kafka bootstrap servers.
     *
     * @param bootstrapServers Example: kafka:19092
     * @return This testcontainer.
     * @see <a href="https://www.littlehorse.io/docs/server/operations/server-configuration#lhs_kafka_bootstrap_servers">Server Documentation.</a>
     */
    public LittleHorseContainer withKafkaBootstrapServers(final String bootstrapServers) {
        this.kafkaBootstrapServers = bootstrapServers;
        return this;
    }

    /**
     * Instance number.
     *
     * @param id Instance id.
     * @return This testcontainer.
     */
    public LittleHorseContainer withInstanceId(final int id) {
        this.instanceId = id;
        return this;
    }

    /**
     * For docker network.
     *
     * @param hostname Example: server1.
     * @return This testcontainer.
     * @see <a href="https://www.littlehorse.io/docs/server/operations/server-configuration#lhs_internal_advertised_host">Server Documentation.</a>
     */
    public LittleHorseContainer withInternalAdvertisedHost(final String hostname) {
        this.internalAdvertisedHost = hostname;
        return this.withNetworkAliases(hostname);
    }

    /**
     * Get docker internal network hostname.
     *
     * @return Internal docker hostname.
     */
    public String getInternalApiHost() {
        return internalAdvertisedHost;
    }

    /**
     * Get docker internal network port.
     *
     * @return Internal docket network port. Example: 2023.
     */
    public int getInternalApiPort() {
        return DEFAULT_INTERNAL_PORT;
    }

    /**
     * Get LH host.
     *
     * @return Hostname: localhost.
     */
    public String getApiHost() {
        return getHost();
    }

    /**
     * Get external port.
     *
     * @return External port. Example: 32023.
     */
    public int getApiPort() {
        return getMappedPort(DEFAULT_INTERNAL_PORT);
    }

    /**
     * Get LH host.
     *
     * @return Hostname: localhost.
     */
    public String getHealthCheckHost() {
        return getHost();
    }

    /**
     * Get external port for healthcheck.
     *
     * @return External port.
     */
    public int getHealthCheckPort() {
        return getMappedPort(DEFAULT_HEALTHCHECK_PORT);
    }

    /**
     * Return a map object with the client configuration.
     * <p>
     * Use: {@code LHConfig.newBuilder().loadFromMap(littlehorseContainer.getClientConfig()).build()}
     * </p>
     *
     * @return Map with the client configurations.
     */
    public Map<String, String> getClientConfig() {
        return Map.of(
                LHC_API_HOST, getApiHost(),
                LHC_API_PORT, String.valueOf(getApiPort()));
    }

    /**
     * Return a properties object with the client configuration.
     * <p>
     * Use: {@code new LHConfig(littlehorseContainer.getClientProperties())}
     * </p>
     *
     * @return Properties with the client configurations.
     */
    public Properties getClientProperties() {
        Properties properties = new Properties();
        properties.putAll(getClientConfig());
        return properties;
    }

    /**
     * Return a map object with the container configuration.
     *
     * @return Map with the container configurations.
     */
    public Map<String, String> getServerConfig() {
        return Map.of(
                LHS_CORE_STREAM_THREADS, DEFAULT_CORE_STREAM_THREADS,
                LHS_INSTANCE_ID, String.valueOf(instanceId),
                LHS_KAFKA_BOOTSTRAP_SERVERS, kafkaBootstrapServers,
                LHS_ADVERTISED_LISTENERS, "PLAIN://%s:%d".formatted(getApiHost(), getApiPort()),
                LHS_INTERNAL_ADVERTISED_HOST, internalAdvertisedHost);
    }

    /**
     * Return the properties that the LH container is using to run.
     *
     * @return Properties with the container configurations.
     */
    public Properties getServerProperties() {
        Properties properties = new Properties();
        properties.putAll(getServerConfig());
        return properties;
    }
}
