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

public class WfMetricUpdateModel extends Storeable<WfMetricUpdate> {
    private Date windowStart;
    private MetricsWindowLength windowType;
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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof WfMetricUpdateModel)) return false;
        final WfMetricUpdateModel other = (WfMetricUpdateModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$windowStart = this.getWindowStart();
        final Object other$windowStart = other.getWindowStart();
        if (this$windowStart == null ? other$windowStart != null : !this$windowStart.equals(other$windowStart))
            return false;
        final Object this$windowType = this.getWindowType();
        final Object other$windowType = other.getWindowType();
        if (this$windowType == null ? other$windowType != null : !this$windowType.equals(other$windowType))
            return false;
        final Object this$wfSpecId = this.getWfSpecId();
        final Object other$wfSpecId = other.getWfSpecId();
        if (this$wfSpecId == null ? other$wfSpecId != null : !this$wfSpecId.equals(other$wfSpecId)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof WfMetricUpdateModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $windowStart = this.getWindowStart();
        result = result * PRIME + ($windowStart == null ? 43 : $windowStart.hashCode());
        final Object $windowType = this.getWindowType();
        result = result * PRIME + ($windowType == null ? 43 : $windowType.hashCode());
        final Object $wfSpecId = this.getWfSpecId();
        result = result * PRIME + ($wfSpecId == null ? 43 : $wfSpecId.hashCode());
        return result;
    }

    public Date getWindowStart() {
        return this.windowStart;
    }

    public MetricsWindowLength getWindowType() {
        return this.windowType;
    }

    public WfSpecIdModel getWfSpecId() {
        return this.wfSpecId;
    }
}
