package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb;
import io.littlehorse.sdk.common.proto.TaskDefMetricsPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListTaskMetricsReply
    extends PublicScanReply<ListTaskMetricsReplyPb, TaskDefMetricsPb, TaskDefMetrics> {

    public Class<ListTaskMetricsReplyPb> getProtoBaseClass() {
        return ListTaskMetricsReplyPb.class;
    }

    public Class<TaskDefMetrics> getResultJavaClass() {
        return TaskDefMetrics.class;
    }

    public Class<TaskDefMetricsPb> getResultProtoClass() {
        return TaskDefMetricsPb.class;
    }
}
