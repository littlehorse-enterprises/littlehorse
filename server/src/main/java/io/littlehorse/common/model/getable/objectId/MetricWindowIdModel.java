package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
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

    public MetricWindowIdModel() {}

    public MetricWindowIdModel(WfSpecIdModel wfSpecId, Date windowStart) {
        this.wfSpecId = wfSpecId;
        this.windowStart = windowStart;
        this.metricType = MetricWindowType.WORKFLOW_METRIC;
    }

    @Override
    public Class<MetricWindowId> getProtoBaseClass() {
        return MetricWindowId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        String parritionKey = getMetricType().name() + "/";
        if (wfSpecId != null) {
            parritionKey += wfSpecId;
        } else if (taskDefId != null) {
            parritionKey += taskDefId;
        } else if (userTaskDefId != null) {
            parritionKey += userTaskDefId;
        }
        return Optional.of(parritionKey);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MetricWindowId p = (MetricWindowId) proto;

        switch (p.getIdCase()) {
            case WF_SPEC_ID:
                this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
                break;
            case TASK_DEF_ID:
                this.taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
                break;
            case USER_TASK_DEF_ID:
                this.userTaskDefId = LHSerializable.fromProto(p.getUserTaskDefId(), UserTaskDefIdModel.class, context);
                break;
            case ID_NOT_SET:
                break;
        }

        if (p.hasWindowStart()) {
            windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        }

        metricType = p.getMetricType();
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

        if (metricType != null) {
            out.setMetricType(metricType);
        }

        return out;
    }

    @Override
    public String toString() {
        String idPart;
        if (wfSpecId != null) {
            idPart = wfSpecId.toString();
        } else if (taskDefId != null) {
            idPart = taskDefId.toString();
        } else if (userTaskDefId != null) {
            idPart = userTaskDefId.toString();
        } else {
            idPart = "unknown";
        }
        return LHUtil.getCompositeId(this.getMetricType().name(), idPart, LHUtil.toLhDbFormat(windowStart));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        metricType = MetricWindowType.valueOf(split[0]);
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
