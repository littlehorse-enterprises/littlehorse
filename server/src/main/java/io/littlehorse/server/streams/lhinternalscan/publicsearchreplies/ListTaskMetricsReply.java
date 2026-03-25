package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricsList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListTaskMetricsReply extends PublicScanReply<MetricsList, MetricWindow, MetricWindowModel> {

    @Override
    public Class<MetricsList> getProtoBaseClass() {
        return MetricsList.class;
    }

    @Override
    public Class<MetricWindowModel> getResultJavaClass() {
        return MetricWindowModel.class;
    }

    @Override
    public Class<MetricWindow> getResultProtoClass() {
        return MetricWindow.class;
    }
}
