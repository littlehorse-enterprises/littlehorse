package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.sdk.common.proto.MetricWindowType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricWindowIdModel extends CoreObjectId<MetricWindowId, MetricWindow, MetricWindowModel> {

    private WfSpecIdModel wfSpecId;
    private TaskDefIdModel taskDefId;
    private UserTaskDefIdModel userTaskDefId;

    private Date windowStart;
    private MetricWindowType metricType;
    private TenantIdModel tenantId;

    public MetricWindowIdModel() {}

    public MetricWindowIdModel(TenantIdModel tenantId, WfSpecIdModel wfSpecId, Date windowStart) {
        this.tenantId = tenantId;
        this.wfSpecId = wfSpecId;
        this.windowStart = windowStart;
        this.metricType = MetricWindowType.WORKFLOW_METRIC;
    }

    public MetricWindowIdModel(TenantIdModel tenantId, TaskDefIdModel taskDefId, Date windowStart) {
        this.tenantId = tenantId;
        this.taskDefId = taskDefId;
        this.windowStart = windowStart;
        this.metricType = MetricWindowType.TASK_METRIC;
    }

    @Override
    public Class<MetricWindowId> getProtoBaseClass() {
        return MetricWindowId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        String parritionKey = getMetricType().name();
        if (wfSpecId != null) {
            parritionKey += "/" + wfSpecId;
        } else if (taskDefId != null) {
            parritionKey += "/" + taskDefId;
        } else if (userTaskDefId != null) {
            parritionKey += "/" + userTaskDefId;
        }
        return Optional.of(parritionKey);
    }

    public void markAsTenantMetricId() {
        clearIds();
    }

    private void clearIds() {
        this.wfSpecId = null;
        this.taskDefId = null;
        this.userTaskDefId = null;
    }

    public String getPartitionMetricStoreKey() {
        String idPart = "";
        if (wfSpecId != null) {
            idPart = wfSpecId.toString();
        } else if (taskDefId != null) {
            idPart = taskDefId.toString();
        } else if (userTaskDefId != null) {
            idPart = userTaskDefId.toString();
        }
        return String.format(
                "%s/%s/%s/%s/%s",
                LHConstants.PARTITION_METRICS_KEY,
                LHUtil.toLhDbFormat(windowStart),
                getMetricType().name(),
                tenantId,
                idPart);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MetricWindowId p = (MetricWindowId) proto;

        switch (p.getIdCase()) {
            case WF_SPEC_ID:
                this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
                this.metricType = MetricWindowType.WORKFLOW_METRIC;
                break;
            case TASK_DEF_ID:
                this.taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
                this.metricType = MetricWindowType.TASK_METRIC;
                break;
            case USER_TASK_DEF_ID:
                this.userTaskDefId = LHSerializable.fromProto(p.getUserTaskDefId(), UserTaskDefIdModel.class, context);
                this.metricType = MetricWindowType.USER_TASK_METRIC;
                break;
            case ID_NOT_SET:
                if (p.hasMetricType()) {
                    this.metricType = p.getMetricType();
                } else {
                    this.metricType = MetricWindowType.UNRECOGNIZED; // default to workflow metric if not set
                }
                break;
        }

        if (p.hasWindowStart()) {
            windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        }
        if (p.hasTenantId()) {
            tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        }
    }

    @Override
    public MetricWindowId.Builder toProto() {
        MetricWindowId.Builder out = MetricWindowId.newBuilder();
        if (windowStart != null) {
            out.setWindowStart(LHUtil.fromDate(windowStart));
        }
        if (wfSpecId != null) {
            out.setWfSpecId(wfSpecId.toProto());
        } else if (taskDefId != null) {
            out.setTaskDefId(taskDefId.toProto());
        } else if (userTaskDefId != null) {
            out.setUserTaskDefId(userTaskDefId.toProto());
        }
        if (tenantId != null) {
            out.setTenantId(tenantId.toProto());
        }
        if (metricType != null) {
            out.setMetricType(metricType);
        }
        return out;
    }

    @Override
    public String toString() {
        String idPart = "";
        if (wfSpecId != null) {
            idPart = wfSpecId.toString();
        } else if (taskDefId != null) {
            idPart = taskDefId.toString();
        } else if (userTaskDefId != null) {
            idPart = userTaskDefId.toString();
        }
        String key = idPart != ""
                ? LHUtil.getCompositeId(this.getMetricType().name(), idPart, LHUtil.toLhDbFormat(windowStart))
                : LHUtil.getCompositeId(this.getMetricType().name(), LHUtil.toLhDbFormat(windowStart));
        return key;
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        metricType = MetricWindowType.valueOf(split[0]);
        System.out.println("Parsing MetricWindowId from store key: " + storeKey);
        switch (metricType) {
            case WORKFLOW_METRIC:
                wfSpecId = (WfSpecIdModel)
                        ObjectIdModel.fromString(split[1] + "/" + split[2] + "/" + split[3], WfSpecIdModel.class);
                break;
            case TASK_METRIC:
                taskDefId = (TaskDefIdModel) ObjectIdModel.fromString(split[1], TaskDefIdModel.class);
                break;
            case USER_TASK_METRIC:
                userTaskDefId = (UserTaskDefIdModel) ObjectIdModel.fromString(split[1], UserTaskDefIdModel.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown metric type: " + metricType);
        }

        windowStart = new Date(Long.valueOf(split[split.length - 1]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.METRIC_WINDOW;
    }
}
