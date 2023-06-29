package io.littlehorse.common.model.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.objectId.WfSpecMetricsId;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.WfSpecMetricsPb;
import io.littlehorse.jlib.common.proto.WfSpecMetricsQueryPb;
import io.littlehorse.server.streamsimpl.storeinternals.GETableIndex;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WfSpecMetrics extends GETable<WfSpecMetricsPb> {

    public Date windowStart;
    public MetricsWindowLengthPb type;
    public String wfSpecName;
    public int wfSpecVersion;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;
    public long startToCompleteMax;
    public long startToCompleteAvg;

    public Class<WfSpecMetricsPb> getProtoBaseClass() {
        return WfSpecMetricsPb.class;
    }

    public WfSpecMetricsPb.Builder toProto() {
        WfSpecMetricsPb.Builder out = WfSpecMetricsPb
            .newBuilder()
            .setWindowStart(LHLibUtil.fromDate(windowStart))
            .setType(type)
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion)
            .setTotalCompleted(totalCompleted)
            .setTotalErrored(totalErrored)
            .setTotalStarted(totalStarted)
            .setStartToCompleteAvg(startToCompleteAvg)
            .setStartToCompleteMax(startToCompleteMax);

        return out;
    }

    public void initFrom(Message proto) {
        WfSpecMetricsPb p = (WfSpecMetricsPb) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        wfSpecName = p.getWfSpecName();
        wfSpecVersion = p.getWfSpecVersion();
        totalCompleted = p.getTotalCompleted();
        totalErrored = p.getTotalErrored();
        totalStarted = p.getTotalStarted();
        startToCompleteAvg = p.getStartToCompleteAvg();
        startToCompleteMax = p.getStartToCompleteMax();
    }

    public Date getCreatedAt() {
        return windowStart;
    }

    public String getPartitionKey() {
        return wfSpecName;
    }

    @Override
    public List<GETableIndex> getIndexes() {
        return new ArrayList<>();
    }

    public static String getObjectId(
        MetricsWindowLengthPb windowType,
        Date time,
        String wfSpecName,
        int wfSpecVersion
    ) {
        return new WfSpecMetricsId(time, windowType, wfSpecName, wfSpecVersion)
            .getStoreKey();
    }

    public static String getObjectId(WfSpecMetricsQueryPb request) {
        return new WfSpecMetricsId(
            LHUtil.getWindowStart(
                LHUtil.fromProtoTs(request.getWindowStart()),
                request.getWindowType()
            ),
            request.getWindowType(),
            request.getWfSpecName(),
            request.getWfSpecVersion()
        )
            .getStoreKey();
    }

    public WfSpecMetricsId getObjectId() {
        return new WfSpecMetricsId(windowStart, type, wfSpecName, wfSpecVersion);
    }
}
