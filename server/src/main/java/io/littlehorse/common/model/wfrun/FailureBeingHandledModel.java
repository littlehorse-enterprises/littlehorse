package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.FailureBeingHandled;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureBeingHandledModel extends LHSerializable<FailureBeingHandled> {

    private int threadRunNumber;
    private int nodeRunPosition;
    private int failureNumber;

    public Class<FailureBeingHandled> getProtoBaseClass() {
        return FailureBeingHandled.class;
    }

    public FailureBeingHandled.Builder toProto() {
        FailureBeingHandled.Builder out =
                FailureBeingHandled.newBuilder()
                        .setThreadRunNumber(threadRunNumber)
                        .setNodeRunPosition(nodeRunPosition)
                        .setFailureNumber(failureNumber);
        return out;
    }

    public void initFrom(Message proto) {
        FailureBeingHandled p = (FailureBeingHandled) proto;
        failureNumber = p.getFailureNumber();
        nodeRunPosition = p.getNodeRunPosition();
        threadRunNumber = p.getThreadRunNumber();
    }

    public static FailureBeingHandledModel fromProto(FailureBeingHandled p) {
        FailureBeingHandledModel out = new FailureBeingHandledModel();
        out.initFrom(p);
        return out;
    }
}
