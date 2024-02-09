package io.littlehorse.canary.config;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CanaryConfig implements Config {

    public static final String LH_CANARY_PREFIX = "lh.canary.";
    public static final String TOPIC_NAME = "topic.name";
    public static final String TOPIC_CREATION_PARTITIONS = "topic.creation.partitions";
    public static final String TOPIC_CREATION_REPLICAS = "topic.creation.replicas";
    public static final String METRONOME_ENABLE = "metronome.enable";
    public static final String AGGREGATOR_ENABLE = "aggregator.enable";
    public static final String METRONOME_FREQUENCY_MS = "metronome.frequency.ms";
    public static final String METRONOME_THREADS = "metronome.threads";
    public static final String METRONOME_RUNS = "metronome.runs";
    public static final String API_PORT = "api.port";
    public static final String ID = "id";
    public static final String METRICS_PORT = "metrics.port";
    public static final String METRICS_PATH = "metrics.path";
    public static final String METRICS_FILTER_ENABLE = "metrics.filter.enable";
    public static final String METRONOME_ACTIVE_MODE_ENABLE = "metronome.active.mode.enable";
    public static final String AGGREGATOR_STORE_RETENTION_MS = "aggregator.store.retention.ms";
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

    public KafkaStreamsConfig toKafkaStreamsConfig() {
        return new KafkaStreamsConfig(configs);
    }

    private String getConfig(final String configName) {
        return configs.get(configName).toString();
    }

    public String getTopicName() {
        return getConfig(TOPIC_NAME);
    }

    public int getTopicPartitions() {
        return Integer.parseInt(getConfig(TOPIC_CREATION_PARTITIONS));
    }

    public int getApiPort() {
        return Integer.parseInt(getConfig(API_PORT));
    }

    public int getMetricsPort() {
        return Integer.parseInt(getConfig(METRICS_PORT));
    }

    public String getMetricsPath() {
        return getConfig(METRICS_PATH);
    }

    public boolean isMetricsFilterEnabled() {
        return Boolean.parseBoolean(getConfig(METRICS_FILTER_ENABLE));
    }

    public short getTopicReplicas() {
        return Short.parseShort(getConfig(TOPIC_CREATION_REPLICAS));
    }

    public boolean isMetronomeEnabled() {
        return Boolean.parseBoolean(getConfig(METRONOME_ENABLE));
    }

    public boolean isMetronomeActiveModeEnabled() {
        return Boolean.parseBoolean(getConfig(METRONOME_ACTIVE_MODE_ENABLE));
    }

    public boolean isAggregatorEnabled() {
        return Boolean.parseBoolean(getConfig(AGGREGATOR_ENABLE));
    }

    public long getMetronomeFrequencyMs() {
        return Long.parseLong(getConfig(METRONOME_FREQUENCY_MS));
    }

    public long getAggregatorStoreRetentionMs() {
        return Long.parseLong(getConfig(AGGREGATOR_STORE_RETENTION_MS));
    }

    public int getMetronomeThreads() {
        return Integer.parseInt(getConfig(METRONOME_THREADS));
    }

    public int getMetronomeRuns() {
        return Integer.parseInt(getConfig(METRONOME_RUNS));
    }

    public String getId() {
        return getConfig(ID);
    }

    public List<String> getEnabledMetrics() {
        return configs.entrySet().stream()
                .filter(entry -> entry.getKey().matches("%s\\[\\d+\\]".formatted(METRICS_FILTER_ENABLE)))
                .map(Entry::getValue)
                .map(Object::toString)
                .toList();
    }
}
