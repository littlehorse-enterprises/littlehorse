package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class WfRunStatusUpdate extends GetableStatusUpdate {

    private final LHStatus previousStatus;
    private final LHStatus newStatus;
    private final WfSpecIdModel wfSpecId;

    public WfRunStatusUpdate(
            WfSpecIdModel wfSpecId, TenantIdModel tenantId, LHStatus previousStatus, LHStatus newStatus) {
        this.previousStatus = previousStatus;
        this.wfSpecId = wfSpecId;
        this.newStatus = Objects.requireNonNull(newStatus);
    }

    @Override
    public List<MetricSpecIdModel> toMetricId() {
        return List.of(new MetricSpecIdModel(wfSpecId), new MetricSpecIdModel(MeasurableObject.WORKFLOW));
    }

    @Override
    public double getMetricIncrementValue(AggregationType aggregationType) {
        return switch (aggregationType) {
            case COUNT -> 1;
            case AVG, RATIO, LATENCY -> 0;
            case UNRECOGNIZED -> throw new IllegalArgumentException(
                    "Unrecognized aggregation type: " + aggregationType);
        };
    }
}
