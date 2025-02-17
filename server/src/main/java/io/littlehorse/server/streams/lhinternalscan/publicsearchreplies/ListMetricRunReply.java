package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.metrics.MetricRunModel;
import io.littlehorse.sdk.common.proto.MetricRun;
import io.littlehorse.sdk.common.proto.MetricRunList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListMetricRunReply extends PublicScanReply<MetricRunList, MetricRun, MetricRunModel> {
    @Override
    public Class<MetricRun> getResultProtoClass() {
        return MetricRun.class;
    }

    @Override
    public Class<MetricRunModel> getResultJavaClass() {
        return MetricRunModel.class;
    }

    @Override
    public Class<MetricRunList> getProtoBaseClass() {
        return MetricRunList.class;
    }
}
