package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.metrics.TaskDefMetricsModel;
import io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListTaskMetricsReply
    extends PublicScanReply<ListTaskMetricsReplyPb, TaskDefMetrics, TaskDefMetricsModel> {

    public Class<ListTaskMetricsReplyPb> getProtoBaseClass() {
        return ListTaskMetricsReplyPb.class;
    }

    public Class<TaskDefMetricsModel> getResultJavaClass() {
        return TaskDefMetricsModel.class;
    }

    public Class<TaskDefMetrics> getResultProtoClass() {
        return TaskDefMetrics.class;
    }
}
