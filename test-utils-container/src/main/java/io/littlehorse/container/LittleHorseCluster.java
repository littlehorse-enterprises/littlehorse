package io.littlehorse.container;

import com.github.dockerjava.api.model.RestartPolicy;
import java.util.List;
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
    private static final Network NETWORK = Network.newNetwork();
    private static final String KAFKA_HOSTNAME = "kafka";
    private static final String LH_HOSTNAME = "littlehorse";
    private static final String BOOTSTRAP_SERVERS = KAFKA_HOSTNAME + ":19092";
    private static final long DEFAULT_KAFKA_MEMORY = 1024L * 1024L * 1024L;
    private static final int DEFAULT_ADVERTISED_PORT = 32023;

    /**
     * It creates a KafkaContainer and a list of LittleHorseContainer
     *
     * @param kafkaImage       Example: {@code DockerImageName.parse("apache/kafka-native:latest")}
     * @param littlehorseImage Example: {@code DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest")}
     * @param instances        LH cluster size
     */
    private LittleHorseCluster(
            final DockerImageName kafkaImage, final DockerImageName littlehorseImage, final int instances) {
        super(DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lhctl")
                .withTag(littlehorseImage.getVersionPart()));

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
                        .withInstanceId(port)
                        .withInternalAdvertisedHost(
                                String.format("%s%d", LH_HOSTNAME, port)) // unique hostname for each instance
                        .withNetwork(NETWORK)
                        .dependsOn(kafka))
                .collect(Collectors.toList());

        this.withNetwork(NETWORK)
                .withCommand("version")
                .withEnv(LHC_API_HOST, cluster.get(0).getInternalApiHost())
                .withEnv(LHC_API_PORT, String.valueOf(cluster.get(0).getInternalApiPort()))
                .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig())
                        .withRestartPolicy(RestartPolicy.onFailureRestart(5))) // waiting for LH to run
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy()) // related to the RestartPolicy
                .dependsOn(kafka)
                .dependsOn(cluster);
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
     * Return a properties object with the client configuration for connections
     * <p>
     * Use: {@code new LHConfig(littleHorseCluster.getClientProperties())}
     * </p>
     * @return Properties with the container configurations
     */
    public Properties getClientProperties() {
        Properties properties = new Properties();
        properties.put(LHC_API_HOST, getHost());
        properties.put(LHC_API_PORT, DEFAULT_ADVERTISED_PORT);
        return properties;
    }

    /**
     * Cluster Builder
     */
    public static class LittleHorseClusterBuilder {
        private String kafkaImage = "apache/kafka-native:latest";
        private String littlehorseImage = "ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest";
        private int instances = 1;

        /**
         * Build LH cluster
         *
         * @return LittleHorseCluster
         */
        public LittleHorseCluster build() {
            return new LittleHorseCluster(
                    DockerImageName.parse(kafkaImage), DockerImageName.parse(littlehorseImage), instances);
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
            assert instances > 0;
            this.instances = instances;
            return this;
        }
    }
}
