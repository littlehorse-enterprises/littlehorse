package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.metrics.WfSpecMetrics;
import io.littlehorse.common.model.objectId.WfSpecMetricsId;
import io.littlehorse.common.proto.WfMetricUpdatePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.log4j.Logger;

public class WfMetricUpdate
    extends Storeable<WfMetricUpdatePb>
    implements RepartitionSubCommand {

    private static final Logger log = Logger.getLogger(WfMetricUpdate.class);

    public Date windowStart;
    public MetricsWindowLengthPb type;
    public long numEntries;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;

    public List<Integer> seenPartitions;
    public String wfSpecName;

    public WfMetricUpdate() {
        seenPartitions = new ArrayList<>();
    }

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
            .setStartToCompleteMax(startToCompleteMax)
            .setNumEntries(numEntries);

        return out;
    }

    public void initFrom(Message proto) {
        WfMetricUpdatePb p = (WfMetricUpdatePb) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        wfSpecName = p.getWfSpecName();
        totalCompleted = p.getTotalCompleted();
        totalErrored = p.getTotalErrored();
        totalStarted = p.getTotalStarted();
        startToCompleteTotal = p.getStartToCompleteTotal();
        startToCompleteMax = p.getStartToCompleteMax();
        numEntries = p.getNumEntries();
    }

    public void merge(WfMetricUpdate o) {
        if (!o.windowStart.equals(windowStart)) {
            throw new RuntimeException("Merging non-matched windows!");
        }
        if (!o.type.equals(type)) {
            throw new RuntimeException("Merging non-matched windows!");
        }

        numEntries += o.numEntries;
        if (o.startToCompleteMax > startToCompleteMax) {
            startToCompleteMax = o.startToCompleteMax;
        }
        startToCompleteTotal += o.startToCompleteTotal;

        if (o.startToCompleteMax > startToCompleteMax) {
            startToCompleteMax = o.startToCompleteMax;
        }
        startToCompleteTotal += o.startToCompleteTotal;

        totalCompleted += o.totalCompleted;
        totalErrored += o.totalErrored;
        totalStarted += o.totalStarted;

        for (Integer seenPartition : o.seenPartitions) {
            seenPartitions.add(seenPartition);
        }
    }

    public WfSpecMetrics toResponse() {
        WfSpecMetrics out = new WfSpecMetrics();
        out.startToCompleteAvg =
            totalStarted > 0 ? startToCompleteTotal / totalStarted : 0;
        out.startToCompleteMax = startToCompleteMax;
        out.wfSpecName = wfSpecName;
        out.totalCompleted = totalCompleted;
        out.totalStarted = totalStarted;
        out.totalErrored = totalErrored;
        out.windowStart = windowStart;
        out.type = type;

        return out;
    }

    public void process(LHStoreWrapper store, ProcessorContext<Void, Void> ctx) {
        WfMetricUpdate previous = store.get(getStoreKey(), getClass());
        if (previous != null) {
            merge(previous);
        }
        store.put(this);
        store.put(toResponse());
        log.debug("Put WfMetric object for key " + toResponse().getStoreKey());
    }

    public Date getCreatedAt() {
        return windowStart;
    }

    public String getPartitionKey() {
        return wfSpecName;
    }

    public static String getPrefix(MetricsWindowLengthPb type, Date time) {
        return type + "/" + LHUtil.toLhDbFormat(time) + "/";
    }

    public static String getObjectId(
        MetricsWindowLengthPb type,
        Date windowStart,
        String wfSpecName
    ) {
        return type + "/" + LHUtil.toLhDbFormat(windowStart) + "/" + wfSpecName;
    }

    public static String getStoreKey(
        MetricsWindowLengthPb type,
        Date windowStart,
        String wfSpecName
    ) {
        return new WfSpecMetricsId(windowStart, type, wfSpecName).getStoreKey();
    }

    public String getStoreKey() {
        return getObjectId(type, windowStart, wfSpecName);
    }
}
