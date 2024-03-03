package io.littlehorse.server.monitoring;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


final class InstanceStore {

    private final String storeName;
    private final Set<TopicPartitionMetrics> partitions = Collections.synchronizedSet(new HashSet<>());

    public enum State {
        READY,
        NOT_READY
    }
    public InstanceStore(final String storeName) {
        this.storeName = storeName;
    }
    public State getState() {
        return null;
    }

}
