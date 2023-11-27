package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.RepartitionedId;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.sdk.common.proto.WfSpecMetricsId;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class WfSpecMetricsIdModel extends RepartitionedId<WfSpecMetricsId, WfSpecMetrics, WfSpecMetricsModel> {

    private Date windowStart;
    private MetricsWindowLength windowType;
    private WfSpecIdModel wfSpecId;

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfSpecId.getName());
    }

    @Override
    public Class<WfSpecMetricsId> getProtoBaseClass() {
        return WfSpecMetricsId.class;
    }

    public WfSpecMetricsIdModel() {}

    public WfSpecMetricsIdModel(Date windowStart, MetricsWindowLength type, WfSpecIdModel wfSpecId) {
        this.windowStart = windowStart;
        this.windowType = type;
        this.wfSpecId = wfSpecId;
    }

    @Override
    public void initFrom(Message proto) {
        WfSpecMetricsId p = (WfSpecMetricsId) proto;
        windowType = p.getWindowType();
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class);
    }

    @Override
    public WfSpecMetricsId.Builder toProto() {
        WfSpecMetricsId.Builder out = WfSpecMetricsId.newBuilder()
                .setWindowType(windowType)
                .setWindowStart(LHUtil.fromDate(windowStart))
                .setWfSpecId(wfSpecId.toProto());
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfSpecId.toString(), windowType.toString(), LHUtil.toLhDbFormat(windowStart));
    }

    @Override
    public void initFromString(String storeKey) {
        // TODO: Check this when we have composite wfSpecVersions
        String[] split = storeKey.split("/");
        wfSpecId = (WfSpecIdModel)
                ObjectIdModel.fromString(split[0] + "/" + split[1] + "/" + split[2], WfSpecIdModel.class); // ouch
        windowType = MetricsWindowLength.valueOf(split[2]);
        windowStart = new Date(Long.valueOf(split[3]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.WF_SPEC_METRICS;
    }
}
