package io.littlehorse.canary.config;

import static java.util.Map.entry;

import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CanaryConfig implements Config {

    public static final String LH_CANARY_PREFIX = "lh.canary.";

    public static final String TOPIC_NAME = "topic.name";
    public static final String TOPIC_CREATION_ENABLE = "topic.creation.enable";
    public static final String TOPIC_CREATION_TIMEOUT_MS = "topic.creation.timeout.ms";
    public static final String TOPIC_PARTITIONS = "topic.partitions";
    public static final String TOPIC_REPLICAS = "topic.replicas";

    public static final String WORKFLOW_NAME = "workflow.name";
    public static final String WORKFLOW_CREATION_ENABLE = "workflow.creation.enable";
    public static final String WORKFLOW_VERSION = "workflow.version";
    public static final String WORKFLOW_REVISION = "workflow.revision";

    public static final String METRONOME_ENABLE = "metronome.enable";
    public static final String METRONOME_FREQUENCY_MS = "metronome.frequency.ms";
    public static final String METRONOME_THREADS = "metronome.threads";
    public static final String METRONOME_RUNS = "metronome.runs";
    public static final String METRONOME_WORKER_ENABLE = "metronome.worker.enable";

    public static final String AGGREGATOR_ENABLE = "aggregator.enable";
    public static final String METRICS_PORT = "metrics.port";
    public static final String METRICS_PATH = "metrics.path";
    public static final String AGGREGATOR_STORE_RETENTION_MS = "aggregator.store.retention.ms";
    public static final String METRICS_COMMON_TAGS = "metrics.common.tags";
    public static final String METRICS_COMMON_TAGS_PREFIX = "%s.".formatted(METRICS_COMMON_TAGS);

    private final Map<String, Object> configs;

    public CanaryConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_PREFIX))
                .map(entry -> entry(entry.getKey().substring(CanaryConfig.LH_CANARY_PREFIX.length()), entry.getValue()))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public Map<String, Object> toMap() {
        return configs;
    }

    @Override
    public String toString() {
        return configs.toString();
    }

    public KafkaAdminConfig toKafkaAdminConfig() {
        return new KafkaAdminConfig(configs);
    }

    public LittleHorseConfig toLittleHorseConfig() {
        return new LittleHorseConfig(configs);
    }

    public KafkaProducerConfig toKafkaProducerConfig() {
        return new KafkaProducerConfig(configs);
    }

    public KafkaConsumerConfig toKafkaConsumerConfig() {
        return new KafkaConsumerConfig(configs);
    }

    public KafkaStreamsConfig toKafkaStreamsConfig() {
        return new KafkaStreamsConfig(configs);
    }

    private String getConfig(final String configName) {
        return configs.get(configName).toString();
    }

    public String getTopicName() {
        return getConfig(TOPIC_NAME);
    }

    public Duration getTopicCreationTimeout() {
        return Duration.ofMillis(Long.parseLong(getConfig(TOPIC_CREATION_TIMEOUT_MS)));
    }

    public int getTopicPartitions() {
        return Integer.parseInt(getConfig(TOPIC_PARTITIONS));
    }

    public int getMetricsPort() {
        return Integer.parseInt(getConfig(METRICS_PORT));
    }

    public String getMetricsPath() {
        return getConfig(METRICS_PATH);
    }

    public short getTopicReplicas() {
        return Short.parseShort(getConfig(TOPIC_REPLICAS));
    }

    public boolean isMetronomeEnabled() {
        return Boolean.parseBoolean(getConfig(METRONOME_ENABLE));
    }

    public boolean isMetronomeWorkerEnabled() {
        return Boolean.parseBoolean(getConfig(METRONOME_WORKER_ENABLE));
    }

    public boolean isAggregatorEnabled() {
        return Boolean.parseBoolean(getConfig(AGGREGATOR_ENABLE));
    }

    public Duration getMetronomeFrequency() {
        return Duration.ofMillis(Long.parseLong(getConfig(METRONOME_FREQUENCY_MS)));
    }

    public Duration getAggregatorStoreRetention() {
        return Duration.ofMillis(Long.parseLong(getConfig(AGGREGATOR_STORE_RETENTION_MS)));
    }

    public int getMetronomeThreads() {
        return Integer.parseInt(getConfig(METRONOME_THREADS));
    }

    public int getMetronomeRuns() {
        return Integer.parseInt(getConfig(METRONOME_RUNS));
    }

    public Map<String, String> getCommonTags() {
        return configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(METRICS_COMMON_TAGS_PREFIX))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(METRICS_COMMON_TAGS_PREFIX.length()),
                        entry -> entry.getValue().toString()));
    }

    public boolean isTopicCreationEnabled() {
        return Boolean.parseBoolean(getConfig(TOPIC_CREATION_ENABLE));
    }

    public boolean isWorkflowCreationEnabled() {
        return Boolean.parseBoolean(getConfig(WORKFLOW_CREATION_ENABLE));
    }

    public String getWorkflowName() {
        return getConfig(WORKFLOW_NAME);
    }

    public int getWorkflowRevision() {
        return Integer.parseInt(getConfig(WORKFLOW_REVISION));
    }

    public int getWorkflowVersion() {
        return Integer.parseInt(getConfig(WORKFLOW_VERSION));
    }
}
