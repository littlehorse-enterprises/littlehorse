package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.WfMetricUpdate;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.Date;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class WfMetricUpdateModel extends Storeable<WfMetricUpdate> implements RepartitionSubCommand {

    public Date windowStart;
    public MetricsWindowLength type;
    public long numEntries;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;

    public String wfSpecName;
    public int wfSpecVersion;

    public WfMetricUpdateModel() {}

    public WfMetricUpdateModel(Date windowStart, MetricsWindowLength type, String wfSpecName, int wfSpecVersion) {
        this.windowStart = windowStart;
        this.type = type;
        this.wfSpecName = wfSpecName;
        this.wfSpecVersion = wfSpecVersion;
    }

    public Class<WfMetricUpdate> getProtoBaseClass() {
        return WfMetricUpdate.class;
    }

    public WfMetricUpdate.Builder toProto() {
        WfMetricUpdate.Builder out = WfMetricUpdate.newBuilder()
                .setWindowStart(LHLibUtil.fromDate(windowStart))
                .setType(type)
                .setWfSpecName(wfSpecName)
                .setWfSpecVersion(wfSpecVersion)
                .setTotalCompleted(totalCompleted)
                .setTotalErrored(totalErrored)
                .setTotalStarted(totalStarted)
                .setStartToCompleteTotal(startToCompleteTotal)
                .setStartToCompleteMax(startToCompleteMax)
                .setNumEntries(numEntries);

        return out;
    }

    public void initFrom(Message proto) {
        WfMetricUpdate p = (WfMetricUpdate) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        wfSpecName = p.getWfSpecName();
        wfSpecVersion = p.getWfSpecVersion();
        totalCompleted = p.getTotalCompleted();
        totalErrored = p.getTotalErrored();
        totalStarted = p.getTotalStarted();
        startToCompleteTotal = p.getStartToCompleteTotal();
        startToCompleteMax = p.getStartToCompleteMax();
        numEntries = p.getNumEntries();
    }

    public void merge(WfMetricUpdateModel o) {
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
        out.wfSpecName = wfSpecName;
        out.wfSpecVersion = wfSpecVersion;
        out.totalCompleted = totalCompleted;
        out.totalStarted = totalStarted;
        out.totalErrored = totalErrored;
        out.windowStart = windowStart;
        out.type = type;

        return out;
    }

    public String getClusterLevelWindow() {
        return new WfSpecMetricsIdModel(windowStart, type, LHConstants.CLUSTER_LEVEL_METRIC, 0).getStoreableKey();
    }

    @Override
    public void process(RocksDBWrapper store, ProcessorContext<Void, Void> ctx) {
        WfMetricUpdateModel previousUpdate = store.get(getStoreKey(), getClass());
        if (previousUpdate != null) {
            merge(previousUpdate);
        }
        store.put(this);

        // This is really hacky
        store.put(new StoredGetable<>(toResponse()));
    }

    @Override
    public StoreableType getType() {
        return StoreableType.WF_METRIC_UPDATE;
    }

    public Date getCreatedAt() {
        return windowStart;
    }

    public String getPartitionKey() {
        return wfSpecName;
    }

    public static String getObjectId(MetricsWindowLength type, Date windowStart, String wfSpecName, int wfSpecVersion) {
        return WfSpecMetricsModel.getObjectId(type, windowStart, wfSpecName, wfSpecVersion);
    }

    public static String getStoreKey(MetricsWindowLength type, Date windowStart, String wfSpecName, int wfSpecVersion) {
        return LHUtil.getCompositeId(
                LHUtil.toLhDbFormat(windowStart),
                type.toString(),
                wfSpecName,
                LHUtil.toLHDbVersionFormat(wfSpecVersion));
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(
                LHUtil.toLhDbFormat(windowStart),
                type.toString(),
                wfSpecName,
                LHUtil.toLHDbVersionFormat(wfSpecVersion));
    }
}
