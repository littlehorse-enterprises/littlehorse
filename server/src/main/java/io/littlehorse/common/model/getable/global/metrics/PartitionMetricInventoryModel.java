package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.common.proto.PartitionMetricInventory;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.proto.PartitionMetricId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionMetricInventoryModel extends Storeable<PartitionMetricInventory> {

    public static final String METRIC_INVENTORY_STORE_KEY = "metricInventory";

    private final String key;

    private final AtomicBoolean metricAdded = new AtomicBoolean(false);

    @Getter
    private Set<PartitionMetricIdModel> metrics = new HashSet<>();

    // Current design contemplates a single metric inventory per partition
    public PartitionMetricInventoryModel() {
        this.key = METRIC_INVENTORY_STORE_KEY;
    }

    @Override
    public String getStoreKey() {
        return key;
    }

    @Override
    public StoreableType getType() {
        return StoreableType.METRIC_PARTITION_INVENTORY;
    }

    @Override
    public PartitionMetricInventory.Builder toProto() {
        List<PartitionMetricId> metricIds = metrics.stream()
                .map(PartitionMetricIdModel::toProto)
                .map(PartitionMetricId.Builder::build)
                .toList();
        return PartitionMetricInventory.newBuilder().addAllMetrics(metricIds);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PartitionMetricInventory p = (PartitionMetricInventory) proto;
        this.metrics = p.getMetricsList().stream()
                .map(m -> LHSerializable.fromProto(m, PartitionMetricIdModel.class, context))
                .collect(Collectors.toSet());
    }

    @Override
    public Class<PartitionMetricInventory> getProtoBaseClass() {
        return PartitionMetricInventory.class;
    }

    /**
     * Adds new metric to this partition
     * @return true if the metrics didn't exist before
     */
    public void addMetric(PartitionMetricIdModel metric) {
        log.info("adding metric");
        metricAdded.set(metrics.add(metric));
    }

    public boolean metricAdded() {
        return metricAdded.get();
    }
}
