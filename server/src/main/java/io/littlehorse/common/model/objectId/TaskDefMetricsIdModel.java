package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.metrics.TaskDefMetricsModel;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.sdk.common.proto.TaskDefMetricsId;
import java.util.Date;

public class TaskDefMetricsIdModel
    extends ObjectId<TaskDefMetricsId, TaskDefMetrics, TaskDefMetricsModel> {

    public Date windowStart;
    public MetricsWindowLength windowType;
    public String taskDefName;

    public TaskDefMetricsIdModel() {}

    public TaskDefMetricsIdModel(Date w, MetricsWindowLength t, String tdn) {
        windowStart = w;
        windowType = t;
        taskDefName = tdn;
    }

    public String getPartitionKey() {
        return taskDefName;
    }

    public Class<TaskDefMetricsId> getProtoBaseClass() {
        return TaskDefMetricsId.class;
    }

    public void initFrom(Message proto) {
        TaskDefMetricsId p = (TaskDefMetricsId) proto;
        taskDefName = p.getTaskDefName();
        windowType = p.getWindowType();
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
    }

    public TaskDefMetricsId.Builder toProto() {
        TaskDefMetricsId.Builder out = TaskDefMetricsId
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setWindowType(windowType)
            .setWindowStart(LHUtil.fromDate(windowStart));
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(
            taskDefName,
            windowType.toString(),
            LHUtil.toLhDbFormat(windowStart)
        );
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        taskDefName = split[0];
        windowType = MetricsWindowLength.valueOf(split[1]);
        windowStart = new Date(Long.valueOf(split[2]));
    }

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.TASK_DEF_METRICS;
    }
}
