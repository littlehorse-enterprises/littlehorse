package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.sdk.common.proto.MetricWindowIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchWfMetricWindowReply
        extends PublicScanReply<MetricWindowIdList, MetricWindowId, MetricWindowIdModel> {

    @Override
    public Class<MetricWindowIdList> getProtoBaseClass() {
        return MetricWindowIdList.class;
    }

    @Override
    public Class<MetricWindowIdModel> getResultJavaClass() {
        return MetricWindowIdModel.class;
    }

    @Override
    public Class<MetricWindowId> getResultProtoClass() {
        return MetricWindowId.class;
    }
}
