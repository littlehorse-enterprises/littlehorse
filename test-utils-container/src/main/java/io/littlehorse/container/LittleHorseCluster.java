package io.littlehorse.container;

import com.github.dockerjava.api.model.RestartPolicy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * <p>
 * LH Cluster Testcontainers implementation
 * </p>
 * Example of using:
 * <blockquote><pre>
 *     {@code @Container}
 *     public LittleHorseCluster littleHorseCluster = LittleHorseCluster.newBuilder()
 *             .withInstances(2)
 *             .withKafkaImage("apache/kafka-native:latest")
 *             .withLittlehorseImage("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:master")
 *             .build();
 * </pre></blockquote>
 */
public class LittleHorseCluster extends GenericContainer<LittleHorseCluster> {

    protected static final String LHC_API_HOST = "LHC_API_HOST";
    protected static final String LHC_API_PORT = "LHC_API_PORT";
    private static final String KAFKA_HOSTNAME = "kafka";
    private static final String LH_HOSTNAME = "littlehorse";
    private static final String KAFKA_BOOTSTRAP_SERVERS = KAFKA_HOSTNAME + ":19092";
    private static final long DEFAULT_KAFKA_MEMORY = 1024L * 1024L * 1024L;
    private static final int DEFAULT_ADVERTISED_PORT = 32023;
    private final KafkaContainer kafka;
    private final List<LittleHorseContainer> clusterInstances;

    /**
     * It creates a KafkaContainer and a list of LittleHorseContainer
     *
     * @param kafkaImage       Example: {@code DockerImageName.parse("apache/kafka-native:latest")}
     * @param littlehorseImage Example: {@code DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest")}
     * @param instances        LH cluster size
     * @param network Internal Network
     */
    private LittleHorseCluster(
            final DockerImageName kafkaImage,
            final DockerImageName littlehorseImage,
            final int instances,
            final Network network) {
        super(DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lhctl")
                .withTag(littlehorseImage.getVersionPart()));

        if (instances <= 0) {
            throw new IllegalArgumentException("Instances should be greater than 0");
        }

        kafka = new KafkaContainer(kafkaImage)
                .withNetwork(network)
                .withNetworkAliases(KAFKA_HOSTNAME)
                .withListener(KAFKA_BOOTSTRAP_SERVERS)
                .withCreateContainerCmdModifier(
                        cmd -> Objects.requireNonNull(cmd.getHostConfig()).withMemory(DEFAULT_KAFKA_MEMORY));

        clusterInstances = IntStream.range(DEFAULT_ADVERTISED_PORT, DEFAULT_ADVERTISED_PORT + instances)
                .mapToObj(port -> new LittleHorseContainer(littlehorseImage)
                        .withKafkaBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                        .withAdvertisedPort(port)
                        .withInstanceId(port)
                        .withInternalAdvertisedHost(
                                String.format("%s%d", LH_HOSTNAME, port)) // unique hostname for each instance
                        .withNetwork(network)
                        .dependsOn(kafka))
                .collect(Collectors.toList());

        this.withNetwork(network)
                .withCommand("version")
                .withEnv(LHC_API_HOST, clusterInstances.get(0).getInternalApiHost())
                .withEnv(LHC_API_PORT, String.valueOf(clusterInstances.get(0).getInternalApiPort()))
                .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig())
                        .withRestartPolicy(RestartPolicy.onFailureRestart(5))) // waiting for LH to run
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy()) // related to the RestartPolicy
                .dependsOn(kafka)
                .dependsOn(clusterInstances);
    }

    /**
     * New builder
     *
     * @return LittleHorseClusterBuilder
     */
    public static LittleHorseClusterBuilder newBuilder() {
        return new LittleHorseClusterBuilder();
    }

    /**
     * Return a properties object with the client configuration for connections.
     * <p>
     * Use: {@code new LHConfig(littleHorseCluster.getClientProperties())}
     * </p>
     *
     * @return Properties with the container configurations.
     */
    public Properties getClientProperties() {
        return clusterInstances.get(0).getClientProperties();
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
        return clusterInstances.get(0).getClientConfig();
    }

    /**
     * Get kafka bootstrap servers for the internal docker network
     *
     * @return Kafka broker, example: "kafka:19092"
     */
    public String getKafkaBootstrapServers() {
        return KAFKA_BOOTSTRAP_SERVERS;
    }

    /**
     * Cluster Builder
     */
    public static class LittleHorseClusterBuilder {
        private String kafkaImage = "apache/kafka-native:latest";
        private String littlehorseImage = "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest";
        private int instances = 1;
        private Network network = Network.newNetwork();

        /**
         * Build LH cluster
         *
         * @return LittleHorseCluster
         */
        public LittleHorseCluster build() {
            return new LittleHorseCluster(
                    DockerImageName.parse(kafkaImage), DockerImageName.parse(littlehorseImage), instances, network);
        }

        /**
         * Kafka image
         *
         * @param kafkaImage Example: "apache/kafka-native:latest"
         * @return This builder
         */
        public LittleHorseClusterBuilder withKafkaImage(final String kafkaImage) {
            this.kafkaImage = kafkaImage;
            return this;
        }

        /**
         * LH image
         *
         * @param littlehorseImage Example: "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest"
         * @return This builder
         */
        public LittleHorseClusterBuilder withLittlehorseImage(final String littlehorseImage) {
            this.littlehorseImage = littlehorseImage;
            return this;
        }

        /**
         * LH Cluster size
         *
         * @param instances Size
         * @return This builder
         */
        public LittleHorseClusterBuilder withInstances(final int instances) {
            if (instances <= 0) {
                throw new IllegalArgumentException("Instances should be greater than 0");
            }
            this.instances = instances;
            return this;
        }

        /**
         * Internal docker network
         * @param network
         * @return
         */
        public LittleHorseClusterBuilder withNetwork(final Network network) {
            if (network == null) {
                throw new NullPointerException("Network shouldn't be null");
            }
            this.network = network;
            return this;
        }
    }
}
