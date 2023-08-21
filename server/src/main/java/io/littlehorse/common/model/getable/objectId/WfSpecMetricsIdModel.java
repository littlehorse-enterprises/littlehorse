package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.RepartitionedId;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.sdk.common.proto.WfSpecMetricsId;
import java.util.Date;
import java.util.Optional;

public class WfSpecMetricsIdModel extends RepartitionedId<WfSpecMetricsId, WfSpecMetrics, WfSpecMetricsModel> {

    public Date windowStart;
    public MetricsWindowLength windowType;
    public String wfSpecName;
    public int wfSpecVersion;

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfSpecName);
    }

    @Override
    public Class<WfSpecMetricsId> getProtoBaseClass() {
        return WfSpecMetricsId.class;
    }

    public WfSpecMetricsIdModel() {}

    public WfSpecMetricsIdModel(Date windowStart, MetricsWindowLength type, String wfSpecName, int wfSpecVersion) {
        this.windowStart = windowStart;
        this.windowType = type;
        this.wfSpecName = wfSpecName;
        this.wfSpecVersion = wfSpecVersion;
    }

    @Override
    public void initFrom(Message proto) {
        WfSpecMetricsId p = (WfSpecMetricsId) proto;
        wfSpecName = p.getWfSpecName();
        windowType = p.getWindowType();
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        wfSpecVersion = p.getWfSpecVersion();
    }

    @Override
    public WfSpecMetricsId.Builder toProto() {
        WfSpecMetricsId.Builder out = WfSpecMetricsId.newBuilder()
                .setWfSpecName(wfSpecName)
                .setWindowType(windowType)
                .setWindowStart(LHUtil.fromDate(windowStart))
                .setWfSpecVersion(wfSpecVersion);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(
                wfSpecName,
                LHUtil.toLHDbVersionFormat(wfSpecVersion),
                windowType.toString(),
                LHUtil.toLhDbFormat(windowStart));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfSpecName = split[0];
        wfSpecVersion = Integer.valueOf(split[1]);
        windowType = MetricsWindowLength.valueOf(split[2]);
        windowStart = new Date(Long.valueOf(split[3]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.WF_SPEC_METRICS;
    }
}
