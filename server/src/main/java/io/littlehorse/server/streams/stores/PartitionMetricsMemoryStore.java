package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PartitionMetricsMemoryStore {

    public PartitionMetricsMemoryStore() {}

    private final Map<String, PartitionMetricWindowModel> metrics = new HashMap<>();
    private final Map<String, PartitionCountedTagModel> countedTags = new HashMap<>();

    public void put(PartitionMetricWindowModel windowMetric) {
        metrics.put(windowMetric.getStoreKey(), windowMetric);
    }

    public PartitionCountedTagModel incrementCounted(TenantIdModel tenantId, String tagAttributes) {
        PartitionCountedTagModel current =
                countedTags.computeIfAbsent(tagAttributes, k -> new PartitionCountedTagModel(tenantId, tagAttributes));
        current.increment();
        return current;
    }

    public PartitionCountedTagModel decrementCounted(TenantIdModel tenantId, String tagAttributes) {
        PartitionCountedTagModel current =
                countedTags.computeIfAbsent(tagAttributes, k -> new PartitionCountedTagModel(tenantId, tagAttributes));
        current.decrement();
        return current;
    }

    public PartitionMetricWindowModel get(String storeKey) {
        return metrics.get(storeKey);
    }

    public Collection<PartitionMetricWindowModel> values() {
        return metrics.values();
    }

    public Map<String, PartitionCountedTagModel> getCountedTags() {
        return Collections.unmodifiableMap(countedTags);
    }

    public PartitionCountedTagModel evictCountedTag(String key) {
        return countedTags.remove(key);
    }

    public boolean hasEntries() {
        return !metrics.isEmpty();
    }

    public void clear() {
        metrics.clear();
    }

    public void delete(String key) {
        metrics.remove(key);
    }
}
