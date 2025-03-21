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
    public static final String WORKFLOW_RETENTION_MS = "workflow.retention.ms";

    public static final String METRONOME_ENABLE = "metronome.enable";
    public static final String METRONOME_RUN_FREQUENCY_MS = "metronome.run.frequency.ms";
    public static final String METRONOME_RUN_THREADS = "metronome.run.threads";
    public static final String METRONOME_RUN_REQUESTS = "metronome.run.requests";
    public static final String METRONOME_RUN_SAMPLE_PERCENTAGE = "metronome.run.sample.percentage";
    public static final String METRONOME_GET_FREQUENCY_MS = "metronome.get.frequency.ms";
    public static final String METRONOME_GET_THREADS = "metronome.get.threads";
    public static final String METRONOME_GET_RETRIES = "metronome.get.retries";
    public static final String METRONOME_WORKER_ENABLE = "metronome.worker.enable";
    public static final String METRONOME_DATA_PATH = "metronome.data.path";
    public static final String METRONOME_BEAT_EXTRA_TAGS = "metronome.beat.extra.tags";
    public static final String METRONOME_BEAT_EXTRA_TAGS_PREFIX = "%s.".formatted(METRONOME_BEAT_EXTRA_TAGS);

    public static final String AGGREGATOR_ENABLE = "aggregator.enable";
    public static final String AGGREGATOR_STORE_RETENTION_MS = "aggregator.store.retention.ms";
    public static final String AGGREGATOR_EXPORT_FREQUENCY_MS = "aggregator.export.frequency.ms";

    public static final String METRICS_PORT = "metrics.port";
    public static final String METRICS_PATH = "metrics.path";
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

    public LittleHorseConfig toLittleHorseConfig() {
        return new LittleHorseConfig(configs);
    }

    public KafkaAdminConfig toKafkaAdminConfig() {
        return new KafkaAdminConfig(configs);
    }

    public KafkaStreamsConfig toKafkaStreamsConfig() {
        return new KafkaStreamsConfig(configs);
    }

    public KafkaProducerConfig toKafkaProducerConfig() {
        return new KafkaProducerConfig(configs);
    }

    public String getConfig(final String configName) {
        final Object value = configs.get(configName);
        if (value == null) {
            throw new IllegalArgumentException("Configuration 'lh.canary." + configName + "' not found");
        }
        return value.toString();
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

    public Duration getAggregatorStoreRetention() {
        return Duration.ofMillis(Long.parseLong(getConfig(AGGREGATOR_STORE_RETENTION_MS)));
    }

    public Duration getAggregatorExportFrequency() {
        return Duration.ofMillis(Long.parseLong(getConfig(AGGREGATOR_EXPORT_FREQUENCY_MS)));
    }

    public Duration getWorkflowRetention() {
        return Duration.ofMillis(Long.parseLong(getConfig(WORKFLOW_RETENTION_MS)));
    }

    public Duration getMetronomeRunFrequency() {
        return Duration.ofMillis(Long.parseLong(getConfig(METRONOME_RUN_FREQUENCY_MS)));
    }

    public int getMetronomeRunThreads() {
        return Integer.parseInt(getConfig(METRONOME_RUN_THREADS));
    }

    public int getMetronomeRunRequests() {
        return Integer.parseInt(getConfig(METRONOME_RUN_REQUESTS));
    }

    public int getMetronomeSamplePercentage() {
        return Integer.parseInt(getConfig(METRONOME_RUN_SAMPLE_PERCENTAGE));
    }

    public Duration getMetronomeGetFrequency() {
        return Duration.ofMillis(Long.parseLong(getConfig(METRONOME_GET_FREQUENCY_MS)));
    }

    public int getMetronomeGetThreads() {
        return Integer.parseInt(getConfig(METRONOME_GET_THREADS));
    }

    public int getMetronomeGetRetries() {
        return Integer.parseInt(getConfig(METRONOME_GET_RETRIES));
    }

    public Map<String, String> getCommonTags() {
        return configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(METRICS_COMMON_TAGS_PREFIX))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(METRICS_COMMON_TAGS_PREFIX.length()),
                        entry -> entry.getValue().toString()));
    }

    public Map<String, String> getMetronomeBeatExtraTags() {
        return configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(METRONOME_BEAT_EXTRA_TAGS_PREFIX))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(METRONOME_BEAT_EXTRA_TAGS_PREFIX.length()),
                        entry -> entry.getValue().toString()));
    }

    public boolean isTopicCreationEnabled() {
        return Boolean.parseBoolean(getConfig(TOPIC_CREATION_ENABLE));
    }

    public boolean isWorkflowCreationEnabled() {
        return isMetronomeEnabled() && Boolean.parseBoolean(getConfig(WORKFLOW_CREATION_ENABLE));
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

    public String getMetronomeDataPath() {
        return getConfig(METRONOME_DATA_PATH);
    }
}
