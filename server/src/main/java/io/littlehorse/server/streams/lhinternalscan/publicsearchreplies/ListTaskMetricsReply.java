package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.sdk.common.proto.ListTaskMetricsResponse;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListTaskMetricsReply
        extends PublicScanReply<ListTaskMetricsResponse, TaskDefMetrics, TaskDefMetricsModel> {

    public Class<ListTaskMetricsResponse> getProtoBaseClass() {
        return ListTaskMetricsResponse.class;
    }

    public Class<TaskDefMetricsModel> getResultJavaClass() {
        return TaskDefMetricsModel.class;
    }

    public Class<TaskDefMetrics> getResultProtoClass() {
        return TaskDefMetrics.class;
    }
}
