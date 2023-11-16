package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
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

public class TaskDefMetricsIdModel extends RepartitionedId<TaskDefMetricsId, TaskDefMetrics, TaskDefMetricsModel> {

    public Date windowStart;
    public MetricsWindowLength windowType;
    public String taskDefName;

    public TaskDefMetricsIdModel() {}

    public TaskDefMetricsIdModel(Date w, MetricsWindowLength t, String tdn) {
        windowStart = w;
        windowType = t;
        taskDefName = tdn;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(taskDefName);
    }

    @Override
    public Class<TaskDefMetricsId> getProtoBaseClass() {
        return TaskDefMetricsId.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskDefMetricsId p = (TaskDefMetricsId) proto;
        taskDefName = p.getTaskDefName();
        windowType = p.getWindowType();
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
    }

    @Override
    public TaskDefMetricsId.Builder toProto() {
        TaskDefMetricsId.Builder out = TaskDefMetricsId.newBuilder()
                .setTaskDefName(taskDefName)
                .setWindowType(windowType)
                .setWindowStart(LHUtil.fromDate(windowStart));
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(taskDefName, windowType.toString(), LHUtil.toLhDbFormat(windowStart));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        taskDefName = split[0];
        windowType = MetricsWindowLength.valueOf(split[1]);
        windowStart = new Date(Long.valueOf(split[2]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_DEF_METRICS;
    }
}
