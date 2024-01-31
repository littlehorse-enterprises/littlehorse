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

    Map<String, Object> toMap();
}
