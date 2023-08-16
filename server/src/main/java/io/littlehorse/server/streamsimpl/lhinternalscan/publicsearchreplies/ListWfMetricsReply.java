package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.metrics.WfSpecMetricsModel;
import io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListWfMetricsReply
    extends PublicScanReply<ListWfMetricsReplyPb, WfSpecMetrics, WfSpecMetricsModel> {

    public Class<ListWfMetricsReplyPb> getProtoBaseClass() {
        return ListWfMetricsReplyPb.class;
    }

    public Class<WfSpecMetricsModel> getResultJavaClass() {
        return WfSpecMetricsModel.class;
    }

    public Class<WfSpecMetrics> getResultProtoClass() {
        return WfSpecMetrics.class;
    }
}
