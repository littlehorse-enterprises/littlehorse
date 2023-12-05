package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.RepartitionedId;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.sdk.common.proto.TaskDefMetricsId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class TaskDefMetricsIdModel extends RepartitionedId<TaskDefMetricsId, TaskDefMetrics, TaskDefMetricsModel> {

    private Date windowStart;
    private MetricsWindowLength windowType;
    private TaskDefIdModel taskDefId;

    public TaskDefMetricsIdModel() {}

    public TaskDefMetricsIdModel(Date w, MetricsWindowLength t, TaskDefIdModel tdid) {
        windowStart = w;
        windowType = t;
        taskDefId = tdid;
    }

    @Override
    public Optional<String> getPartitionKey() {
        // group it by task def name
        return Optional.of(taskDefId.getName());
    }

    @Override
    public Class<TaskDefMetricsId> getProtoBaseClass() {
        return TaskDefMetricsId.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskDefMetricsId p = (TaskDefMetricsId) proto;
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        windowType = p.getWindowType();
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
    }

    @Override
    public TaskDefMetricsId.Builder toProto() {
        TaskDefMetricsId.Builder out = TaskDefMetricsId.newBuilder()
                .setTaskDefId(taskDefId.toProto())
                .setWindowType(windowType)
                .setWindowStart(LHUtil.fromDate(windowStart));
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(taskDefId.toString(), windowType.toString(), LHUtil.toLhDbFormat(windowStart));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        taskDefId = (TaskDefIdModel) ObjectIdModel.fromString(split[0], TaskDefIdModel.class);
        windowType = MetricsWindowLength.valueOf(split[1]);
        windowStart = new Date(Long.valueOf(split[2]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_DEF_METRICS;
    }
}
