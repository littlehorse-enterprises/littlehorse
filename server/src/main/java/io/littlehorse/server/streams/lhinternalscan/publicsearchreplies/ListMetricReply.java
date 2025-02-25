package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.metrics.MetricModel;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListMetricReply extends PublicScanReply<MetricList, Metric, MetricModel> {
    @Override
    public Class<Metric> getResultProtoClass() {
        return Metric.class;
    }

    @Override
    public Class<MetricModel> getResultJavaClass() {
        return MetricModel.class;
    }

    @Override
    public Class<MetricList> getProtoBaseClass() {
        return MetricList.class;
    }
}
