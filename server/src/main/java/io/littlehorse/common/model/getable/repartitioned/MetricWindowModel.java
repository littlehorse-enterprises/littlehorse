package io.littlehorse.common.model.getable.repartitioned;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.proto.MetricWindow;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MetricWindowModel extends Storeable<MetricWindow> {

    private String tenantId;
    public Date lastWindowStart;
    private MetricsWindowLength type;
    private WfSpecIdModel wfSPecId;
    private TaskDefIdModel taskDefId;

    public MetricWindowModel() {}

    public MetricWindowModel(MetricsWindowLength type, WfSpecIdModel wfSPecId, String tenantId) {
        this.tenantId = tenantId;
        this.type = type;
        this.wfSPecId = wfSPecId;
        this.lastWindowStart = new Date();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricWindow p = (MetricWindow) proto;
        this.tenantId = p.getTenantId();
        this.lastWindowStart = LHUtil.fromProtoTs(p.getLastWindowStart());
        this.type = p.getType();
        if (p.getMetricIdCase().equals(MetricWindow.MetricIdCase.WF_SPEC_ID)) {
            this.wfSPecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        } else if (p.getMetricIdCase().equals(MetricWindow.MetricIdCase.TASK_DEF_ID)) {
            this.taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        } else {
            throw new IllegalStateException("Unrecognized MetricIdCase");
        }
    }

    @Override
    public MetricWindow.Builder toProto() {
        MetricWindow.Builder out = MetricWindow.newBuilder();
        out.setTenantId(tenantId);
        out.setLastWindowStart(LHUtil.fromDate(lastWindowStart));
        out.setType(type);
        if (wfSPecId != null) {
            out.setWfSpecId(wfSPecId.toProto());
        }
        if (taskDefId != null) {
            out.setTaskDefId(taskDefId.toProto());
        }
        return out;
    }

    @Override
    public Class<MetricWindow> getProtoBaseClass() {
        return MetricWindow.class;
    }

    @Override
    public String getStoreKey() {
        if (wfSPecId != null) {
            return LHUtil.getCompositeId(type.name(), wfSPecId.toString(), tenantId);
        } else {
            return LHUtil.getCompositeId(type.name(), taskDefId.toString(), tenantId);
        }
    }

    @Override
    public StoreableType getType() {
        return StoreableType.METRIC_WINDOW;
    }

    public String currentAggregationId() {
        long windowLengthMillis = LHUtil.getWindowLengthMillis(type);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWindow =
                lastWindowStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime plus = lastWindow.plus(windowLengthMillis, TimeUnit.MILLISECONDS.toChronoUnit());
        if (now.isEqual(plus) || now.isAfter(plus)) {
            lastWindowStart = new Date();
        }
        return new WfSpecMetricsIdModel(lastWindowStart, type, wfSPecId).toString();
    }
}
