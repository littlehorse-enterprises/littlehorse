package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PartitionMetricsMemoryStore {

    public PartitionMetricsMemoryStore() {}

    private final Map<String, PartitionMetricWindowModel> metrics = new HashMap<>();
    private final Map<String, PartitionCountedTagModel> countedTags = new HashMap<>();

    public void put(PartitionMetricWindowModel windowMetric) {
        metrics.put(windowMetric.getStoreKey(), windowMetric);
    }

    public PartitionMetricWindowModel get(String storeKey) {
        return metrics.get(storeKey);
    }

    public Collection<PartitionMetricWindowModel> values() {
        return metrics.values();
    }

    public void clear() {
        metrics.clear();
    }

    public void delete(String key) {
        metrics.remove(key);
    }
}
