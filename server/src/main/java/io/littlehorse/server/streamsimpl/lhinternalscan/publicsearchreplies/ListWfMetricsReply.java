package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.sdk.common.proto.ListWfMetricsResponse;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListWfMetricsReply extends PublicScanReply<ListWfMetricsResponse, WfSpecMetrics, WfSpecMetricsModel> {

    public Class<ListWfMetricsResponse> getProtoBaseClass() {
        return ListWfMetricsResponse.class;
    }

    public Class<WfSpecMetricsModel> getResultJavaClass() {
        return WfSpecMetricsModel.class;
    }

    public Class<WfSpecMetrics> getResultProtoClass() {
        return WfSpecMetrics.class;
    }
}
