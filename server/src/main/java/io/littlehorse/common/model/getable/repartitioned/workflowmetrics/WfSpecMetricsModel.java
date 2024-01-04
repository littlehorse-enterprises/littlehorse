package io.littlehorse.common.model.getable.repartitioned.workflowmetrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.RepartitionedGetable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
public class WfSpecMetricsModel extends RepartitionedGetable<WfSpecMetrics> {

    public Date windowStart;
    public MetricsWindowLength type;

    @Setter
    private WfSpecIdModel wfSpecId;

    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;
    public long startToCompleteMax;
    public long startToCompleteAvg;
    private ExecutionContext executionContext;

    public WfSpecMetricsModel() {}

    public WfSpecMetricsModel(Date windowStart, MetricsWindowLength type, WfSpecIdModel wfSpecId) {
        this.windowStart = windowStart;
        this.type = type;
        this.wfSpecId = wfSpecId;
    }

    public Class<WfSpecMetrics> getProtoBaseClass() {
        return WfSpecMetrics.class;
    }

    public WfSpecMetrics.Builder toProto() {
        WfSpecMetrics.Builder out = WfSpecMetrics.newBuilder()
                .setWindowStart(LHLibUtil.fromDate(windowStart))
                .setType(type)
                .setWfSpecId(wfSpecId.toProto())
                .setTotalCompleted(totalCompleted)
                .setTotalErrored(totalErrored)
                .setTotalStarted(totalStarted)
                .setStartToCompleteAvg(startToCompleteAvg)
                .setStartToCompleteMax(startToCompleteMax);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WfSpecMetrics p = (WfSpecMetrics) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        totalCompleted = p.getTotalCompleted();
        totalErrored = p.getTotalErrored();
        totalStarted = p.getTotalStarted();
        startToCompleteAvg = p.getStartToCompleteAvg();
        startToCompleteMax = p.getStartToCompleteMax();
        this.executionContext = context;
    }

    public Date getCreatedAt() {
        return windowStart;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public static String getObjectId(MetricsWindowLength windowType, Date time, WfSpecIdModel wfSpecId) {
        return new WfSpecMetricsIdModel(time, windowType, wfSpecId).toString();
    }

    public WfSpecMetricsIdModel getObjectId() {
        return new WfSpecMetricsIdModel(windowStart, type, wfSpecId);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }
}
