package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.metrics.WfSpecMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.sdk.common.proto.WfSpecMetricsId;
import java.util.Date;

public class WfSpecMetricsIdModel
    extends ObjectId<WfSpecMetricsId, WfSpecMetrics, WfSpecMetricsModel> {

    public Date windowStart;
    public MetricsWindowLength windowType;
    public String WfSpecName;
    public int wfSpecVersion;

    public String getPartitionKey() {
        return WfSpecName;
    }

    public Class<WfSpecMetricsId> getProtoBaseClass() {
        return WfSpecMetricsId.class;
    }

    public WfSpecMetricsIdModel() {}

    public WfSpecMetricsIdModel(
        Date windowStart,
        MetricsWindowLength type,
        String wfSpecName,
        int wfSpecVersion
    ) {
        this.windowStart = windowStart;
        this.windowType = type;
        this.WfSpecName = wfSpecName;
        this.wfSpecVersion = wfSpecVersion;
    }

    public void initFrom(Message proto) {
        WfSpecMetricsId p = (WfSpecMetricsId) proto;
        WfSpecName = p.getWfSpecName();
        windowType = p.getWindowType();
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        wfSpecVersion = p.getWfSpecVersion();
    }

    public WfSpecMetricsId.Builder toProto() {
        WfSpecMetricsId.Builder out = WfSpecMetricsId
            .newBuilder()
            .setWfSpecName(WfSpecName)
            .setWindowType(windowType)
            .setWindowStart(LHUtil.fromDate(windowStart))
            .setWfSpecVersion(wfSpecVersion);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(
            WfSpecName,
            LHUtil.toLHDbVersionFormat(wfSpecVersion),
            windowType.toString(),
            LHUtil.toLhDbFormat(windowStart)
        );
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        WfSpecName = split[0];
        wfSpecVersion = Integer.valueOf(split[1]);
        windowType = MetricsWindowLength.valueOf(split[2]);
        windowStart = new Date(Long.valueOf(split[3]));
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.WF_SPEC_METRICS;
    }
}
