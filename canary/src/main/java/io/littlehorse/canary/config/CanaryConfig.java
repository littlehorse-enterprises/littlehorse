package io.littlehorse.canary.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CanaryConfig implements Config {

    private final Map<String, Object> configs;

    public CanaryConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_PREFIX))
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

    public String getTopicName() {
        return configs.get(LH_CANARY_TOPIC_NAME).toString();
    }

    public int getTopicPartitions() {
        return Integer.parseInt(configs.get(LH_CANARY_TOPIC_CREATION_PARTITIONS).toString());
    }

    public short getTopicReplicas() {
        return Short.parseShort(configs.get(LH_CANARY_TOPIC_CREATION_REPLICAS).toString());
    }

    public boolean isMetronomeEnabled() {
        return Boolean.parseBoolean(configs.get(LH_CANARY_METRONOME_ENABLE).toString());
    }

    public boolean isAggregatorEnabled() {
        return Boolean.parseBoolean(configs.get(LH_CANARY_AGGREGATOR_ENABLE).toString());
    }

    public long getMetronomeFrequency() {
        return Long.parseLong(configs.get(LH_CANARY_METRONOME_FREQUENCY_MS).toString());
    }

    public int getMetronomeThreads() {
        return Integer.parseInt(configs.get(LH_CANARY_METRONOME_THREADS).toString());
    }

    public int getMetronomeRuns() {
        return Integer.parseInt(configs.get(LH_CANARY_METRONOME_RUNS).toString());
    }
}
