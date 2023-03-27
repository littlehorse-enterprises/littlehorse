package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.TaskDefMetricsIdPb;
import io.littlehorse.jlib.common.proto.TaskDefMetricsPb;
import java.util.Date;

public class TaskDefMetricsId
    extends ObjectId<TaskDefMetricsIdPb, TaskDefMetricsPb, TaskDefMetrics> {

    public Date windowStart;
    public MetricsWindowLengthPb windowType;
    public String taskDefName;

    public TaskDefMetricsId() {}

    public TaskDefMetricsId(Date w, MetricsWindowLengthPb t, String tdn) {
        windowStart = w;
        windowType = t;
        taskDefName = tdn;
    }

    public String getPartitionKey() {
        return taskDefName;
    }

    public Class<TaskDefMetricsIdPb> getProtoBaseClass() {
        return TaskDefMetricsIdPb.class;
    }

    public void initFrom(Message proto) {
        TaskDefMetricsIdPb p = (TaskDefMetricsIdPb) proto;
        taskDefName = p.getTaskDefName();
        windowType = p.getWindowType();
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
    }

    public TaskDefMetricsIdPb.Builder toProto() {
        TaskDefMetricsIdPb.Builder out = TaskDefMetricsIdPb
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setWindowType(windowType)
            .setWindowStart(LHUtil.fromDate(windowStart));
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(
            windowType.toString(),
            LHUtil.toLhDbFormat(windowStart),
            taskDefName
        );
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        windowType = MetricsWindowLengthPb.valueOf(split[0]);
        windowStart = new Date(Long.valueOf(split[1]));
        taskDefName = split[2];
    }

    public GETableClassEnumPb getType() {
        return GETableClassEnumPb.TASK_DEF_METRICS;
    }
}
