package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.WfMetricUpdate;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

import java.util.Date;

public class WfMetricUpdateModel extends Storeable<WfMetricUpdate> {

    private Date windowStart;
    private MetricsWindowLength type;
    private WfSpecIdModel wfSpecId;
    public long numEntries;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;

    public WfMetricUpdateModel() {

    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        WfMetricUpdate p = (WfMetricUpdate) proto;
        this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        this.type = p.getType();
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
        out.setType(type);
        out.setNumEntries(numEntries);
        out.setStartToCompleteMax(startToCompleteMax);
        out.setStartToCompleteTotal(startToCompleteTotal);
        out.setTotalCompleted(totalCompleted);
        out.setTotalErrored(totalErrored);
        out.setTotalStarted(totalStarted);
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
