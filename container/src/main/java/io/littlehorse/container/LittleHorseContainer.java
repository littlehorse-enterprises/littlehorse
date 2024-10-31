package io.littlehorse.container;

import com.github.dockerjava.api.model.PortBinding;
import java.util.Objects;
import java.util.Properties;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class LittleHorseContainer extends GenericContainer<LittleHorseContainer> {
    private static final String LHS_KAFKA_BOOTSTRAP_SERVERS = "LHS_KAFKA_BOOTSTRAP_SERVERS";
    private static final String KAFKA_HOSTNAME = "kafka";
    private static final String BOOTSTRAP_SERVERS = KAFKA_HOSTNAME + ":19092";
    private static final String LOG_REGEX = ".*New state for (core|timer) topology: RUNNING.*";
    private static final String LHC_API_HOST = "LHC_API_HOST";
    private static final String LHC_API_PORT = "LHC_API_PORT";
    private static final long DEFAULT_MEMORY = 1024L * 1024L * 1024L;
    private static final String LHS_ADVERTISED_LISTENERS = "LHS_ADVERTISED_LISTENERS";
    private static final DockerImageName DEFAULT_IMAGE_NAME =
            DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server");
    private static final int DEFAULT_INTERNAL_PORT = 2023;
    private static final int DEFAULT_ADVERTISED_PORT = 32023;
    private static final String ADVERTISED_LISTENER = String.format("PLAIN://localhost:%d", DEFAULT_ADVERTISED_PORT);
    private final KafkaContainer kafka;

    /**
     * Create LittleHorse Testcontainers Wrapper
     *
     * @param kafkaImage       Example: DockerImageName.parse("apache/kafka-native:latest")
     * @param littlehorseImage Example: DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest")
     */
    public LittleHorseContainer(DockerImageName kafkaImage, DockerImageName littlehorseImage) {
        super(littlehorseImage);
        littlehorseImage.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        Network network = Network.newNetwork();

        kafka = new KafkaContainer(kafkaImage);
        kafka.withNetwork(network);
        kafka.withNetworkAliases(KAFKA_HOSTNAME);
        kafka.withListener(BOOTSTRAP_SERVERS);
        kafka.withCreateContainerCmdModifier(
                cmd -> Objects.requireNonNull(cmd.getHostConfig()).withMemory(DEFAULT_MEMORY));

        this.withNetwork(network);
        this.withExposedPorts(DEFAULT_INTERNAL_PORT);
        this.waitingFor(Wait.forLogMessage(LOG_REGEX, 2));
        this.withEnv(LHS_KAFKA_BOOTSTRAP_SERVERS, BOOTSTRAP_SERVERS);
        this.withEnv(LHS_ADVERTISED_LISTENERS, ADVERTISED_LISTENER);
        this.withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig())
                .withPortBindings(
                        PortBinding.parse(String.format("%d:%d", DEFAULT_ADVERTISED_PORT, DEFAULT_INTERNAL_PORT)))
                .withMemory(DEFAULT_MEMORY));
    }

    /**
     * Get LH host
     * @return hostname: localhost
     */
    public String getApiHost() {
        return getHost();
    }

    /**
     * Get port
     * @return port: 32023
     */
    public int getApiPort() {
        return getMappedPort(DEFAULT_INTERNAL_PORT);
    }

    /**
     * Return a properties object.
     * Use: new LHConfig(littlehorseContainer.getProperties())
     * @return Properties with the container configurations
     */
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(LHC_API_HOST, getApiHost());
        properties.put(LHC_API_PORT, getApiPort());
        return properties;
    }

    @Override
    public void start() {
        kafka.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        kafka.stop();
    }
}
