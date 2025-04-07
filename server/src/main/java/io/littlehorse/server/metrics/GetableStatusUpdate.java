package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import java.util.Date;
import java.util.List;

public abstract class GetableStatusUpdate {

    private final Date creationDate;
    private final TenantIdModel tenantId;

    public GetableStatusUpdate(TenantIdModel tenantId) {
        this.creationDate = new Date();
        this.tenantId = tenantId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public TenantIdModel getTenantId() {
        return tenantId;
    }

    public abstract List<MetricSpecIdModel> toMetricId();

    public abstract double getMetricIncrementValue(AggregationType aggregationType);
}
