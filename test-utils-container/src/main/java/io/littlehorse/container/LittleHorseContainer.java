package io.littlehorse.container;

import static io.littlehorse.container.LittleHorseCluster.LHC_API_HOST;
import static io.littlehorse.container.LittleHorseCluster.LHC_API_PORT;

import com.github.dockerjava.api.model.PortBinding;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * <p>
 * LH Testcontainers implementation
 * </p>
 * Example of using:
 * <blockquote><pre>
 * public LittleHorseContainer littleHorseInstance = new LittleHorseContainer(littlehorseImage)
 *             .withKafkaBootstrapServers(kafkaBootstrapServers)
 *             .withAdvertisedPort(externalPort)
 *             .withInstanceId(instanceId)
 *             .withInternalAdvertisedHost(internalHostname) // unique hostname for each instance
 *             .withNetwork(dockerNetwork)
 *             .dependsOn(kafka);
 * </pre></blockquote>
 * Example of using with LittleHorseCluster (@see {@link LittleHorseCluster#newBuilder()}):
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
    private static final String LOG_REGEX = ".*State transition from REBALANCING to RUNNING.*";
    private static final long DEFAULT_LH_MEMORY = 1024L * 1024L * 1024L;
    private static final String LHS_ADVERTISED_LISTENERS = "LHS_ADVERTISED_LISTENERS";
    private static final DockerImageName DEFAULT_IMAGE_NAME =
            DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server");
    private static final int DEFAULT_INTERNAL_PORT = 2023;
    private static final int DEFAULT_ADVERTISED_PORT = 32023;
    private static final String DEFAULT_KAFKA_BOOTSTRAP_SERVERS = "kafka:19092";

    /**
     * Create LittleHorse Testcontainers Wrapper
     *
     * @param littlehorseImage Example: "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest"
     */
    public LittleHorseContainer(final String littlehorseImage) {
        this(DockerImageName.parse(littlehorseImage));
    }

    /**
     * Create LittleHorse Testcontainers Wrapper
     *
     * @param littlehorseImage Example: {@code DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest")}
     */
    public LittleHorseContainer(final DockerImageName littlehorseImage) {
        super(littlehorseImage);
        littlehorseImage.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        this.withExposedPorts(DEFAULT_INTERNAL_PORT)
                .withKafkaBootstrapServers(DEFAULT_KAFKA_BOOTSTRAP_SERVERS)
                .withAdvertisedPort(DEFAULT_ADVERTISED_PORT)
                .withInstanceId(1)
                .withEnv(LHS_CORE_STREAM_THREADS, "2")
                .withCreateContainerCmdModifier(
                        cmd -> Objects.requireNonNull(cmd.getHostConfig()).withMemory(DEFAULT_LH_MEMORY))
                .waitingFor(Wait.forLogMessage(LOG_REGEX, 2));
    }

    /**
     * Kafka bootstrap servers. Configures the {@code LHS_KAFKA_BOOTSTRAP_SERVERS} env variable.
     *
     * @param bootstrapServers Example: kafka:19092
     * @return This testcontainer
     * @see <a href="https://www.littlehorse.io/docs/server/operations/server-configuration#lhs_kafka_bootstrap_servers">Server Documentation.</a>
     */
    public LittleHorseContainer withKafkaBootstrapServers(final String bootstrapServers) {
        return this.withEnv(LHS_KAFKA_BOOTSTRAP_SERVERS, bootstrapServers);
    }

    /**
     * External port for connection. It configures the {@code LHS_ADVERTISED_LISTENERS} env variable.
     *
     * @param port Example: 32023
     * @return This testcontainer
     * @see <a href="https://www.littlehorse.io/docs/server/operations/server-configuration#lhs_advertised_listeners">Server Documentation.</a>
     */
    public LittleHorseContainer withAdvertisedPort(final int port) {
        return this.withEnv(LHS_ADVERTISED_LISTENERS, String.format("PLAIN://localhost:%d", port))
                .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig())
                        .withPortBindings(PortBinding.parse(String.format("%d:%d", port, DEFAULT_INTERNAL_PORT))));
    }

    /**
     * Instance number
     *
     * @param id Instance id
     * @return This testcontainer
     */
    public LittleHorseContainer withInstanceId(final int id) {
        return this.withEnv(LHS_INSTANCE_ID, String.valueOf(id));
    }

    /**
     * For docker network. It configures the {@code LHS_INTERNAL_ADVERTISED_HOST} env variable.
     *
     * @param hostname Example: server1
     * @return This testcontainer
     * @see <a href="https://www.littlehorse.io/docs/server/operations/server-configuration#lhs_internal_advertised_host">Server Documentation.</a>
     */
    public LittleHorseContainer withInternalAdvertisedHost(final String hostname) {
        return this.withEnv(LHS_INTERNAL_ADVERTISED_HOST, hostname).withNetworkAliases(hostname);
    }

    /**
     * Get LH host
     *
     * @return Hostname: localhost
     */
    public String getApiHost() {
        return getHost();
    }

    /**
     * Get docker internal network hostname
     *
     * @return Internal docker hostname
     */
    public String getInternalApiHost() {
        if (getNetworkAliases().isEmpty()) {
            return getApiHost();
        }
        return getNetworkAliases().get(0);
    }

    /**
     * Get docker internal network port
     *
     * @return Internal docket network port
     */
    public int getInternalApiPort() {
        return DEFAULT_INTERNAL_PORT;
    }

    /**
     * Get external port
     *
     * @return External port: 32023
     */
    public int getApiPort() {
        return getMappedPort(DEFAULT_INTERNAL_PORT);
    }

    /**
     *  Return a properties object with the client configuration for connections.
     * <p>
     * Use: {@code new LHConfig(littlehorseContainer.getClientProperties())}
     * </p>
     *
     * @return Properties with the container configurations.
     */
    public Properties getClientProperties() {
        Properties properties = new Properties();
        properties.putAll(getClientConfig());
        return properties;
    }

    /**
     * Return a map object with the client configuration for connections.
     * <p>
     * Use: {@code LHConfig.newBuilder().loadFromMap(littlehorseContainer.getClientConfig()).build()}
     * </p>
     *
     * @return Map with the container configurations.
     */
    public Map<String, String> getClientConfig() {
        return Map.of(LHC_API_HOST, getApiHost(), LHC_API_PORT, String.valueOf(getApiPort()));
    }
}
