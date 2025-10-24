package io.littlehorse.container;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.RestartPolicy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
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

    private static final String LHC_API_HOST = "LHC_API_HOST";
    private static final String LHC_API_PORT = "LHC_API_PORT";

    private static final String LH_HOSTNAME = "littlehorse";
    private static final String KAFKA_HOSTNAME = "kafka";
    private static final String KAFKA_BOOTSTRAP_SERVERS = KAFKA_HOSTNAME + ":19092";
    private static final String LHCTL_CONTAINER_COMMAND = "version";

    private static final String DEFAULT_KAFKA_IMAGE = "apache/kafka-native:latest";
    private static final String DEFAULT_LH_IMAGE = "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest";
    private static final int DEFAULT_LH_CLUSTER_SIZE = 1;
    private static final Network DEFAULT_NETWORK = Network.newNetwork();
    private static final Consumer<CreateContainerCmd> DEFAULT_KAFKA_MODIFIER = cmd -> {};
    private static final Consumer<CreateContainerCmd> DEFAULT_LH_MODIFIER = cmd -> {};

    private final KafkaContainer kafka;
    private final List<LittleHorseContainer> clusterInstances;

    /**
     * It creates a KafkaContainer and a list of LittleHorseContainer
     *
     * @param kafkaImage             Example: {@code DockerImageName.parse("apache/kafka-native:latest")}.
     * @param littlehorseImage       Example: {@code DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest")}.
     * @param instances              LH cluster size.
     * @param network                Internal Network.
     * @param kafkaContainerModifier Kafka Customization.
     * @param lhContainerModifier    Kafka Customization.
     */
    private LittleHorseCluster(
            final DockerImageName kafkaImage,
            final DockerImageName littlehorseImage,
            final int instances,
            final Network network,
            final Consumer<CreateContainerCmd> kafkaContainerModifier,
            final Consumer<CreateContainerCmd> lhContainerModifier) {
        super(DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lhctl")
                .withTag(littlehorseImage.getVersionPart()));

        if (instances <= 0) {
            throw new IllegalArgumentException("Instances should be greater than 0");
        }

        kafka = new KafkaContainer(kafkaImage)
                .withNetwork(network)
                .withNetworkAliases(KAFKA_HOSTNAME)
                .withListener(KAFKA_BOOTSTRAP_SERVERS)
                .withCreateContainerCmdModifier(kafkaContainerModifier);

        clusterInstances = IntStream.rangeClosed(DEFAULT_LH_CLUSTER_SIZE, instances)
                .mapToObj(instanceId -> new LittleHorseContainer(littlehorseImage)
                        .withKafkaBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                        .withInstanceId(instanceId)
                        .withInternalAdvertisedHost(
                                String.format("%s%d", LH_HOSTNAME, instanceId)) // unique hostname for each instance
                        .withNetwork(network)
                        .dependsOn(kafka)
                        .withCreateContainerCmdModifier(lhContainerModifier))
                .collect(Collectors.toList());

        this.withNetwork(network)
                .withCommand(LHCTL_CONTAINER_COMMAND)
                .withEnv(LHC_API_HOST, clusterInstances.getFirst().getInternalApiHost())
                .withEnv(
                        LHC_API_PORT, String.valueOf(clusterInstances.getFirst().getInternalApiPort()))
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
     * Get internal kafka container.
     *
     * @return KafkaContainer object.
     */
    public KafkaContainer getKafka() {
        return kafka;
    }

    /**
     * Get LH containers list.
     *
     * @return List of LittleHorseContainer.
     */
    public List<LittleHorseContainer> getClusterInstances() {
        return clusterInstances;
    }

    /**
     * Gets cluster size.
     *
     * @return cluster size.
     */
    public int getSize() {
        return clusterInstances.size();
    }

    /**
     * Return a properties object with the client configuration.
     * <p>
     * Use: {@code new LHConfig(littleHorseCluster.getClientProperties())}
     * </p>
     *
     * @return Properties with the container configurations.
     */
    public Properties getClientProperties() {
        return clusterInstances.getFirst().getClientProperties();
    }

    /**
     * Return a map object with the client configuration.
     * <p>
     * Use: {@code LHConfig.newBuilder().loadFromMap(littlehorseContainer.getClientConfig()).build()}
     * </p>
     *
     * @return Map with the container configurations.
     */
    public Map<String, String> getClientConfig() {
        return clusterInstances.getFirst().getClientConfig();
    }

    /**
     * Get kafka bootstrap servers for the internal docker network
     *
     * @return Kafka broker, example: "localhost:19092"
     */
    public String getKafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }

    /**
     * Cluster Builder
     */
    public static class LittleHorseClusterBuilder {
        private String kafkaImage = DEFAULT_KAFKA_IMAGE;
        private String littlehorseImage = DEFAULT_LH_IMAGE;
        private int instances = DEFAULT_LH_CLUSTER_SIZE;
        private Network network = DEFAULT_NETWORK;
        private Consumer<CreateContainerCmd> kafkaContainerModifier = DEFAULT_KAFKA_MODIFIER;
        private Consumer<CreateContainerCmd> lhContainerModifier = DEFAULT_LH_MODIFIER;

        /**
         * Build LH cluster.
         *
         * @return LittleHorseCluster.
         */
        public LittleHorseCluster build() {
            return new LittleHorseCluster(
                    DockerImageName.parse(kafkaImage),
                    DockerImageName.parse(littlehorseImage),
                    instances,
                    network,
                    kafkaContainerModifier,
                    lhContainerModifier);
        }

        /**
         * Kafka image.
         *
         * @param kafkaImage Example: "apache/kafka-native:latest".
         * @return This builder.
         */
        public LittleHorseClusterBuilder withKafkaImage(final String kafkaImage) {
            this.kafkaImage = kafkaImage;
            return this;
        }

        /**
         * LH image.
         *
         * @param littlehorseImage Example: "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest".
         * @return This builder.
         */
        public LittleHorseClusterBuilder withLittlehorseImage(final String littlehorseImage) {
            this.littlehorseImage = littlehorseImage;
            return this;
        }

        /**
         * LH Cluster size.
         *
         * @param instances Size.
         * @return This builder.
         */
        public LittleHorseClusterBuilder withInstances(final int instances) {
            if (instances <= 0) {
                throw new IllegalArgumentException("Instances should be greater than 0");
            }
            this.instances = instances;
            return this;
        }

        /**
         * Modifies the kafka container.
         *
         * @param kafkaContainerModifier Kafka modifier. More at <a href="https://java.testcontainers.org/features/advanced_options/#customizing-the-container">...</a>.
         * @return This builder.
         */
        public LittleHorseClusterBuilder withKafkaModifier(final Consumer<CreateContainerCmd> kafkaContainerModifier) {
            if (kafkaContainerModifier == null) {
                throw new NullPointerException("Modifier shouldn't be null");
            }
            this.kafkaContainerModifier = kafkaContainerModifier;
            return this;
        }

        /**
         * Modifies the LH containers.
         *
         * @param lhContainerModifier Kafka modifier. More at <a href="https://java.testcontainers.org/features/advanced_options/#customizing-the-container">...</a>.
         * @return This builder.
         */
        public LittleHorseClusterBuilder withLittleHorseModifier(
                final Consumer<CreateContainerCmd> lhContainerModifier) {
            if (lhContainerModifier == null) {
                throw new NullPointerException("Modifier shouldn't be null");
            }
            this.lhContainerModifier = lhContainerModifier;
            return this;
        }

        /**
         * Internal docker network.
         *
         * @param network Internal network to be used.
         * @return This builder.
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
