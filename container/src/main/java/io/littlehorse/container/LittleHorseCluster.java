package io.littlehorse.container;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.kafka.KafkaContainer;

public class LittleHorseCluster extends GenericContainer<LittleHorseCluster> {

    public static final String LHC_API_HOST = "LHC_API_HOST";
    public static final String LHC_API_PORT = "LHC_API_PORT";
    private static final Network NETWORK = Network.newNetwork();
    private static final String KAFKA_HOSTNAME = "kafka";
    private static final String LH_HOSTNAME = "littlehorse";
    private static final String BOOTSTRAP_SERVERS = KAFKA_HOSTNAME + ":19092";
    private static final long DEFAULT_KAFKA_MEMORY = 1024L * 1024L * 1024L;
    private static final int DEFAULT_ADVERTISED_PORT = 32023;

    // TODO: DOCUMENTATION
    private LittleHorseCluster(final String kafkaImage, final String littlehorseImage, final int instances) {
        // TODO: use bash?
        super("bash");

        KafkaContainer kafka = new KafkaContainer(kafkaImage)
                .withNetwork(NETWORK)
                .withNetworkAliases(KAFKA_HOSTNAME)
                .withListener(BOOTSTRAP_SERVERS)
                .withCreateContainerCmdModifier(
                        cmd -> Objects.requireNonNull(cmd.getHostConfig()).withMemory(DEFAULT_KAFKA_MEMORY));

        List<LittleHorseContainer> cluster = IntStream.range(
                        DEFAULT_ADVERTISED_PORT, DEFAULT_ADVERTISED_PORT + instances)
                .mapToObj(port -> new LittleHorseContainer(littlehorseImage)
                        .withKafkaBootstrapServers(BOOTSTRAP_SERVERS)
                        .withAdvertisedPort(port)
                        .withInternalAdvertisedName(String.format("%s%d", LH_HOSTNAME, port))
                        .withNetwork(NETWORK)
                        .dependsOn(kafka))
                .collect(Collectors.toList());

        this.withNetwork(NETWORK)
                .withCommand("-c", "tail -f /dev/null")
                .dependsOn(kafka)
                .dependsOn(cluster);
    }

    public static LittleHorseClusterBuilder newBuilder() {
        return new LittleHorseClusterBuilder();
    }

    /**
     * Return a properties object.
     * Use: new LHConfig(littlehorseContainer.getProperties())
     *
     * @return Properties with the container configurations
     */
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(LHC_API_HOST, getHost());
        properties.put(LHC_API_PORT, DEFAULT_ADVERTISED_PORT);
        return properties;
    }

    public static class LittleHorseClusterBuilder {
        private String kafkaImage = "apache/kafka-native:latest";
        private String littlehorseImage = "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest";
        private int instances = 1;

        public LittleHorseCluster build() {
            return new LittleHorseCluster(kafkaImage, littlehorseImage, instances);
        }

        public LittleHorseClusterBuilder withKafkaImage(final String kafkaImage) {
            this.kafkaImage = kafkaImage;
            return this;
        }

        public LittleHorseClusterBuilder withLittlehorseImage(final String littlehorseImage) {
            this.littlehorseImage = littlehorseImage;
            return this;
        }

        public LittleHorseClusterBuilder withInstances(final int instances) {
            assert instances > 0;
            this.instances = instances;
            return this;
        }
    }
}
