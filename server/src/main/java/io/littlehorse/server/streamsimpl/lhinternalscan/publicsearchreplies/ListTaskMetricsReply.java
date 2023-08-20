package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.sdk.common.proto.ListTaskMetricsResponse;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

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
