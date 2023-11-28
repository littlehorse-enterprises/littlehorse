package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.WfMetricUpdatePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.store.ModelStore;
import java.util.Date;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Getter
public class WfMetricUpdate extends Storeable<WfMetricUpdatePb> implements RepartitionSubCommand {

    private WfSpecIdModel wfSpecId;

    public Date windowStart;
    public MetricsWindowLength type;
    public long numEntries;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;

    public WfMetricUpdate() {}

    public WfMetricUpdate(Date windowStart, MetricsWindowLength type, WfSpecIdModel wfSpecId) {
        this.windowStart = windowStart;
        this.type = type;
        this.wfSpecId = wfSpecId;
    }

    @Override
    public Class<WfMetricUpdatePb> getProtoBaseClass() {
        return WfMetricUpdatePb.class;
    }

    @Override
    public WfMetricUpdatePb.Builder toProto() {
        WfMetricUpdatePb.Builder out = WfMetricUpdatePb.newBuilder()
                .setWindowStart(LHLibUtil.fromDate(windowStart))
                .setType(type)
                .setWfSpecId(wfSpecId.toProto())
                .setTotalCompleted(totalCompleted)
                .setTotalErrored(totalErrored)
                .setTotalStarted(totalStarted)
                .setStartToCompleteTotal(startToCompleteTotal)
                .setStartToCompleteMax(startToCompleteMax)
                .setNumEntries(numEntries);

        return out;
    }

    @Override
    public void initFrom(Message proto) {
        WfMetricUpdatePb p = (WfMetricUpdatePb) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class);
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

        totalCompleted += o.totalCompleted;
        totalErrored += o.totalErrored;
        totalStarted += o.totalStarted;
    }

    public WfSpecMetricsModel toResponse() {
        WfSpecMetricsModel out = new WfSpecMetricsModel();
        out.startToCompleteAvg = totalCompleted > 0 ? startToCompleteTotal / totalCompleted : 0;
        out.startToCompleteMax = startToCompleteMax;
        out.setWfSpecId(wfSpecId);
        out.totalCompleted = totalCompleted;
        out.totalStarted = totalStarted;
        out.totalErrored = totalErrored;
        out.windowStart = windowStart;
        out.type = type;

        return out;
    }

    public String getClusterLevelWindow() {
        return new WfSpecMetricsIdModel(windowStart, type, new WfSpecIdModel(LHConstants.CLUSTER_LEVEL_METRIC, 0, 0))
                .getStoreableKey();
    }

    @Override
    public void process(ModelStore store, ProcessorContext<Void, Void> ctx) {
        throw new NotImplementedException("Need to re-enable workflow metrics");
        /*
         * // Update workflow-level metrics
         * WfMetricUpdate previousUpdate = store.get(getStoreKey(), getClass());
         * if (previousUpdate != null) {
         * merge(previousUpdate);
         * }
         * store.put(this);
         * store.put(toResponse());
         * log.debug("Put WfMetric object for key {}", toResponse().getStoreKey());
         */
    }

    @Override
    public StoreableType getType() {
        return StoreableType.WF_METRIC_UPDATE;
    }

    public Date getCreatedAt() {
        return windowStart;
    }

    public String getPartitionKey() {
        return wfSpecId.getName();
    }

    public static String getObjectId(MetricsWindowLength type, Date windowStart, WfSpecIdModel wfSpecId) {
        return new WfSpecMetricsIdModel(windowStart, type, wfSpecId).toString();
    }

    public static String getStoreKey(MetricsWindowLength type, Date windowStart, String wfSpecName, int wfSpecVersion) {
        return LHUtil.getCompositeId(
                LHUtil.toLhDbFormat(windowStart),
                type.toString(),
                wfSpecName,
                LHUtil.toLHDbVersionFormat(wfSpecVersion));
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(LHUtil.toLhDbFormat(windowStart), type.toString(), wfSpecId.toString());
    }
}
