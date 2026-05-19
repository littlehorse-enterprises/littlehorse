package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PartitionMetricsMemoryStore {

    private final Map<String, PartitionMetricWindowModel> metrics = new HashMap<>();
    private final Map<String, PartitionCountedTagModel> countedTags = new HashMap<>();

    public void put(PartitionMetricWindowModel windowMetric) {
        metrics.put(windowMetric.getStoreKey(), windowMetric);
    }

    public void incrementCounted(TenantIdModel tenantId, String tagAttributes) {
        countedTags
                .computeIfAbsent(tagAttributes, k -> new PartitionCountedTagModel(tenantId, tagAttributes))
                .increment();
    }

    public void decrementCounted(TenantIdModel tenantId, String tagAttributes) {
        countedTags
                .computeIfAbsent(tagAttributes, k -> new PartitionCountedTagModel(tenantId, tagAttributes))
                .decrement();
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

    public void evictCountedTag(String key) {
        countedTags.remove(key);
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
