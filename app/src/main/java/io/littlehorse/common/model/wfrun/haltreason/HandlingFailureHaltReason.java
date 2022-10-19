package io.littlehorse.common.model.wfrun.haltreason;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.HandlingFailureHaltReasonPb;
import io.littlehorse.common.proto.HandlingFailureHaltReasonPbOrBuilder;
import io.littlehorse.common.proto.LHStatusPb;

public class HandlingFailureHaltReason
    extends LHSerializable<HandlingFailureHaltReasonPb>
    implements SubHaltReason {

    public int handlerThreadId;

    @JsonIgnore
    public boolean isResolved(WfRun wfRun) {
        ThreadRun hThread = wfRun.threadRuns.get(handlerThreadId);
        return (hThread.status == LHStatusPb.COMPLETED);
    }

    public Class<HandlingFailureHaltReasonPb> getProtoBaseClass() {
        return HandlingFailureHaltReasonPb.class;
    }

    public HandlingFailureHaltReasonPb.Builder toProto() {
        HandlingFailureHaltReasonPb.Builder out = HandlingFailureHaltReasonPb.newBuilder();
        out.setHandlerThreadId(handlerThreadId);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        HandlingFailureHaltReasonPbOrBuilder p = (HandlingFailureHaltReasonPbOrBuilder) proto;
        handlerThreadId = p.getHandlerThreadId();
    }

    public static HandlingFailureHaltReason fromProto(
        HandlingFailureHaltReasonPbOrBuilder proto
    ) {
        HandlingFailureHaltReason out = new HandlingFailureHaltReason();
        out.initFrom(proto);
        return out;
    }
}
