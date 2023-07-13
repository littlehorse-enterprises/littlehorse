package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.FailureBeingHandledPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureBeingHandled extends LHSerializable<FailureBeingHandledPb> {

    private int threadRunNumber;
    private int nodeRunPosition;
    private int failureNumber;

    public Class<FailureBeingHandledPb> getProtoBaseClass() {
        return FailureBeingHandledPb.class;
    }

    public FailureBeingHandledPb.Builder toProto() {
        FailureBeingHandledPb.Builder out = FailureBeingHandledPb
            .newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setNodeRunPosition(nodeRunPosition)
            .setFailureNumber(failureNumber);
        return out;
    }

    public void initFrom(Message proto) {
        FailureBeingHandledPb p = (FailureBeingHandledPb) proto;
        failureNumber = p.getFailureNumber();
        nodeRunPosition = p.getNodeRunPosition();
        threadRunNumber = p.getThreadRunNumber();
    }

    public static FailureBeingHandled fromProto(FailureBeingHandledPb p) {
        FailureBeingHandled out = new FailureBeingHandled();
        out.initFrom(p);
        return out;
    }
}
