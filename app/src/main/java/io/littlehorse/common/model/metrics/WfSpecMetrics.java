package io.littlehorse.common.model.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.objectId.WfSpecMetricsId;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.WfSpecMetricsPb;
import io.littlehorse.jlib.common.proto.WfSpecMetricsQueryPb;
import java.util.Date;

public class WfSpecMetrics extends GETable<WfSpecMetricsPb> {

    public Date windowStart;
    public MetricsWindowLengthPb type;
    public String wfSpecName;
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

    public String getStoreKey() {
        return getObjectId(type, windowStart, wfSpecName);
    }

    public static String getObjectId(
        MetricsWindowLengthPb windowType,
        Date time,
        String wfSpecName
    ) {
        return (
            windowType.toString() + "/" + LHUtil.toLhDbFormat(time) + "/" + wfSpecName
        );
    }

    public static String getObjectId(WfSpecMetricsQueryPb request) {
        // Need to align the thing to the thing
        return getObjectId(
            request.getWindowType(),
            LHUtil.getWindowStart(
                LHUtil.fromProtoTs(request.getWindowStart()),
                request.getWindowType()
            ),
            request.getWfSpecName()
        );
    }

    public WfSpecMetricsId getObjectId() {
        return new WfSpecMetricsId(windowStart, type, wfSpecName);
    }
}
