package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class MetricWindowIdModel extends CoreObjectId<MetricWindowId, MetricWindow, MetricWindowModel> {

    private WfSpecIdModel wfSpecId;
    private Date windowStart;

    public MetricWindowIdModel() {}

    public MetricWindowIdModel(WfSpecIdModel wfSpecId, Date windowStart) {
        this.wfSpecId = wfSpecId;
        this.windowStart = windowStart;
    }

    @Override
    public Class<MetricWindowId> getProtoBaseClass() {
        return MetricWindowId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfSpecId.getName());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MetricWindowId p = (MetricWindowId) proto;
        if (p.hasWorkflow() && p.getWorkflow().hasWfSpec()) {
            wfSpecId = LHSerializable.fromProto(p.getWorkflow().getWfSpec(), WfSpecIdModel.class, context);
        }
        windowStart = LHUtil.fromProtoTs(p.getWindowStart());
    }

    @Override
    public MetricWindowId.Builder toProto() {
        MetricWindowId.Builder out = MetricWindowId.newBuilder().setWindowStart(LHUtil.fromDate(windowStart));

        if (wfSpecId != null) {
            out.setWorkflow(io.littlehorse.sdk.common.proto.WorkflowMetricId.newBuilder()
                    .setWfSpec(wfSpecId.toProto())
                    .build());
        }

        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfSpecId.toString(), LHUtil.toLhDbFormat(windowStart));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        // Expecting format: wfSpecName/majorVersion/revision/windowStartTimestamp
        wfSpecId = (WfSpecIdModel)
                ObjectIdModel.fromString(split[0] + "/" + split[1] + "/" + split[2], WfSpecIdModel.class);
        windowStart = new Date(Long.valueOf(split[3]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.METRIC_WINDOW;
    }
}
