package io.littlehorse.server;

import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.Cluster;
import org.slf4j.Logger;

public class LHDefaultPartitioner extends RoundRobinPartitioner {

    private final static Logger log = org.slf4j.LoggerFactory.getLogger(LHDefaultPartitioner.class);

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        log.info("Partitioning key record {}", key.getClass().getName());
        return super.partition(topic, key, keyBytes, value, valueBytes, cluster);
    }

}
