package io.littlehorse.container;

import static io.littlehorse.container.LittleHorseCluster.LHC_API_HOST;
import static io.littlehorse.container.LittleHorseCluster.LHC_API_PORT;

import com.github.dockerjava.api.model.PortBinding;
import java.util.Objects;
import java.util.Properties;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class LittleHorseContainer extends GenericContainer<LittleHorseContainer> {

    private static final String LHS_INTERNAL_ADVERTISED_HOST = "LHS_INTERNAL_ADVERTISED_HOST";
    private static final String LHS_INSTANCE_ID = "LHS_INSTANCE_ID";
    private static final String LHS_CORE_STREAM_THREADS = "LHS_CORE_STREAM_THREADS";
    private static final String LHS_KAFKA_BOOTSTRAP_SERVERS = "LHS_KAFKA_BOOTSTRAP_SERVERS";
    private static final String LOG_REGEX = ".*New state for (core|timer) topology: RUNNING.*";
    private static final long DEFAULT_MEMORY = 1024L * 1024L * 1024L;
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
     * @param littlehorseImage Example: DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest")
     */
    public LittleHorseContainer(final DockerImageName littlehorseImage) {
        super(littlehorseImage);
        littlehorseImage.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        this.waitingFor(Wait.forLogMessage(LOG_REGEX, 2))
                .withExposedPorts(DEFAULT_INTERNAL_PORT)
                .withKafkaBootstrapServers(DEFAULT_KAFKA_BOOTSTRAP_SERVERS)
                .withAdvertisedPort(DEFAULT_ADVERTISED_PORT)
                .withInstanceId(1)
                .withEnv(LHS_CORE_STREAM_THREADS, "2")
                .withCreateContainerCmdModifier(
                        cmd -> Objects.requireNonNull(cmd.getHostConfig()).withMemory(DEFAULT_MEMORY));
    }

    public LittleHorseContainer withKafkaBootstrapServers(final String bootstrapServers) {
        return this.withEnv(LHS_KAFKA_BOOTSTRAP_SERVERS, bootstrapServers);
    }

    public LittleHorseContainer withAdvertisedPort(final int port) {
        return this.withEnv(LHS_ADVERTISED_LISTENERS, String.format("PLAIN://localhost:%d", port))
                .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig())
                        .withPortBindings(PortBinding.parse(String.format("%d:%d", port, DEFAULT_INTERNAL_PORT))));
    }

    public LittleHorseContainer withInstanceId(final int id) {
        return this.withEnv(LHS_INSTANCE_ID, String.valueOf(id));
    }

    public LittleHorseContainer withInternalAdvertisedHost(final String hostname) {
        return this.withEnv(LHS_INTERNAL_ADVERTISED_HOST, hostname).withNetworkAliases(hostname);
    }

    /**
     * Get LH host
     *
     * @return hostname: localhost
     */
    public String getApiHost() {
        return getHost();
    }

    public String getInternalApiHost() {
        if (getNetworkAliases().isEmpty()) {
            return getApiHost();
        }
        return getNetworkAliases().get(0);
    }

    public int getInternalApiPort() {
        return DEFAULT_INTERNAL_PORT;
    }

    /**
     * Get port
     *
     * @return port: 32023
     */
    public int getApiPort() {
        return getMappedPort(DEFAULT_INTERNAL_PORT);
    }

    /**
     * Return a properties object.
     * Use: new LHConfig(littlehorseContainer.getProperties())
     *
     * @return Properties with the container configurations
     */
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(LHC_API_HOST, getApiHost());
        properties.put(LHC_API_PORT, getApiPort());
        return properties;
    }
}
