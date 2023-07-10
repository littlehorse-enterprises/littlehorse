package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.metrics.WfSpecMetrics;
import io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb;
import io.littlehorse.sdk.common.proto.WfSpecMetricsPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListWfMetricsReply
    extends PublicScanReply<ListWfMetricsReplyPb, WfSpecMetricsPb, WfSpecMetrics> {

    public Class<ListWfMetricsReplyPb> getProtoBaseClass() {
        return ListWfMetricsReplyPb.class;
    }

    public Class<WfSpecMetrics> getResultJavaClass() {
        return WfSpecMetrics.class;
    }

    public Class<WfSpecMetricsPb> getResultProtoClass() {
        return WfSpecMetricsPb.class;
    }
}
