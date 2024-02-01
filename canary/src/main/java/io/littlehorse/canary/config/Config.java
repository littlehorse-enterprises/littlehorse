package io.littlehorse.canary.config;

import java.util.Map;

public interface Config {
    String LH_CANARY_PREFIX = "lh.canary.";
    String LH_CANARY_KAFKA_PREFIX = LH_CANARY_PREFIX + "kafka.";

    String LH_CANARY_TOPIC_NAME = LH_CANARY_PREFIX + "topic.name";
    String LH_CANARY_TOPIC_CREATION_PARTITIONS = LH_CANARY_PREFIX + "topic.creation.partitions";
    String LH_CANARY_TOPIC_CREATION_REPLICAS = LH_CANARY_PREFIX + "topic.creation.replicas";
    String LH_CANARY_METRONOME_ENABLE = LH_CANARY_PREFIX + "metronome.enable";
    String LH_CANARY_AGGREGATOR_ENABLE = LH_CANARY_PREFIX + "aggregator.enable";
    String LH_CANARY_METRONOME_FREQUENCY_MS = LH_CANARY_PREFIX + "metronome.frequency.ms";
    String LH_CANARY_METRONOME_THREADS = LH_CANARY_PREFIX + "metronome.threads";
    String LH_CANARY_METRONOME_RUNS = LH_CANARY_PREFIX + "metronome.runs";

    Map<String, Object> toMap();
}
