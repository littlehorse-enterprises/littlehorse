package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.Node;
import java.time.Duration;
import java.util.List;
import lombok.Getter;

@Getter
public class NodeRunCompleteUpdate extends GetableStatusUpdate {
    private final Duration latency;
    private final Node.NodeCase nodeType;

    public NodeRunCompleteUpdate(TenantIdModel tenantId, Node.NodeCase nodeType, Duration latency) {
        this.latency = latency;
        this.nodeType = nodeType;
    }

    @Override
    public List<MetricSpecIdModel> toMetricId() {
        return List.of();
    }

    @Override
    public double getMetricIncrementValue(AggregationType aggregationType) {
        return 0;
    }
}
