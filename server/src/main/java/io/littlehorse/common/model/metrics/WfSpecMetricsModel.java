package io.littlehorse.common.model.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class WfSpecMetricsModel extends Getable<WfSpecMetrics> {

    public Date windowStart;
    public MetricsWindowLength type;
    public String wfSpecName;
    public int wfSpecVersion;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;
    public long startToCompleteMax;
    public long startToCompleteAvg;

    public Class<WfSpecMetrics> getProtoBaseClass() {
        return WfSpecMetrics.class;
    }

    public WfSpecMetrics.Builder toProto() {
        WfSpecMetrics.Builder out = WfSpecMetrics
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
        WfSpecMetrics p = (WfSpecMetrics) proto;
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
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public static String getObjectId(
        MetricsWindowLength windowType,
        Date time,
        String wfSpecName,
        int wfSpecVersion
    ) {
        return new WfSpecMetricsIdModel(time, windowType, wfSpecName, wfSpecVersion)
            .getStoreKey();
    }

    public static String getObjectId(WfSpecMetricsQueryRequest request) {
        return new WfSpecMetricsIdModel(
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

    public WfSpecMetricsIdModel getObjectId() {
        return new WfSpecMetricsIdModel(windowStart, type, wfSpecName, wfSpecVersion);
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageType> tagStorageType
    ) {
        return List.of();
    }
}
