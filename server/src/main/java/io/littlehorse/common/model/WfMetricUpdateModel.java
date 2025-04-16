package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.WfMetricUpdate;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(
        of = {"windowStart", "windowType", "wfSpecId"},
        callSuper = false)
public class WfMetricUpdateModel extends Storeable<WfMetricUpdate> {

    @Getter
    private Date windowStart;

    @Getter
    private MetricsWindowLength windowType;

    @Getter
    private WfSpecIdModel wfSpecId;

    public long numEntries;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;

    public WfMetricUpdateModel() {}

    public WfMetricUpdateModel(Date windowStart, MetricsWindowLength type, WfSpecIdModel wfSpecId) {
        this.windowStart = windowStart;
        this.windowType = type;
        this.wfSpecId = wfSpecId;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        WfMetricUpdate p = (WfMetricUpdate) proto;
        this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        this.windowType = p.getType();
        this.numEntries = p.getNumEntries();
        this.startToCompleteMax = p.getStartToCompleteMax();
        this.startToCompleteTotal = p.getStartToCompleteTotal();
        this.totalCompleted = p.getTotalCompleted();
        this.totalErrored = p.getTotalErrored();
        this.totalStarted = p.getTotalStarted();
    }

    @Override
    public WfMetricUpdate.Builder toProto() {
        WfMetricUpdate.Builder out = WfMetricUpdate.newBuilder();
        out.setWindowStart(LHUtil.fromDate(windowStart));
        out.setType(windowType);
        out.setNumEntries(numEntries);
        out.setStartToCompleteMax(startToCompleteMax);
        out.setStartToCompleteTotal(startToCompleteTotal);
        out.setTotalCompleted(totalCompleted);
        out.setTotalErrored(totalErrored);
        out.setTotalStarted(totalStarted);
        out.setWfSpecId(wfSpecId.toProto());
        return out;
    }

    @Override
    public Class<WfMetricUpdate> getProtoBaseClass() {
        return WfMetricUpdate.class;
    }

    @Override
    public String getStoreKey() {
        return LHUtil.getCompositeId();
    }

    @Override
    public StoreableType getType() {
        return StoreableType.TASK_METRIC_UPDATE;
    }
}
