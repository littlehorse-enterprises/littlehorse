package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import java.util.Date;
import java.util.List;

public abstract class GetableStatusUpdate {

    private final Date creationDate;

    public GetableStatusUpdate() {
        this.creationDate = new Date();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public abstract List<MetricSpecIdModel> toMetricId();

    public abstract double getMetricIncrementValue(AggregationType aggregationType);
}
