package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.WfMetricUpdatePb;
import io.littlehorse.jlib.common.proto.WfMetricUpdatePbOrBuilder;
import java.util.Date;

public class WfMetricUpdate extends Storeable<WfMetricUpdatePb> {

    public Date windowStart;
    public MetricsWindowLengthPb type;
    public long numEntries;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;

    public int partition;
    public String wfSpecName;

    public Class<WfMetricUpdatePb> getProtoBaseClass() {
        return WfMetricUpdatePb.class;
    }

    public WfMetricUpdatePb.Builder toProto() {
        WfMetricUpdatePb.Builder out = WfMetricUpdatePb
            .newBuilder()
            .setWindowStart(LHLibUtil.fromDate(windowStart))
            .setType(type)
            .setWfSpecName(wfSpecName)
            .setTotalCompleted(totalCompleted)
            .setTotalErrored(totalErrored)
            .setTotalStarted(totalStarted)
            .setStartToCompleteTotal(startToCompleteTotal)
            .setStartToCompleteMax(startToCompleteMax);

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        WfMetricUpdatePbOrBuilder p = (WfMetricUpdatePbOrBuilder) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        wfSpecName = p.getWfSpecName();
        totalCompleted = p.getTotalCompleted();
        totalErrored = p.getTotalErrored();
        totalStarted = p.getTotalStarted();
        startToCompleteTotal = p.getStartToCompleteTotal();
        startToCompleteMax = p.getStartToCompleteMax();
    }

    public static String getObjectId(
        MetricsWindowLengthPb type,
        Date windowStart,
        String wfSpecName
    ) {
        return type + "/" + LHUtil.toLhDbFormat(windowStart) + "/" + wfSpecName;
    }

    public String getObjectId() {
        return getObjectId(type, windowStart, wfSpecName);
    }
}
