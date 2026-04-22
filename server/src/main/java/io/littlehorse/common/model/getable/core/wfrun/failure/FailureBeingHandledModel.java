package io.littlehorse.common.model.getable.core.wfrun.failure;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.FailureBeingHandled;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class FailureBeingHandledModel extends LHSerializable<FailureBeingHandled> {

    private int threadRunNumber;
    private int nodeRunPosition;
    private int failureNumber;

    public Class<FailureBeingHandled> getProtoBaseClass() {
        return FailureBeingHandled.class;
    }

    public FailureBeingHandled.Builder toProto() {
        FailureBeingHandled.Builder out = FailureBeingHandled.newBuilder()
                .setThreadRunNumber(threadRunNumber)
                .setNodeRunPosition(nodeRunPosition)
                .setFailureNumber(failureNumber);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        FailureBeingHandled p = (FailureBeingHandled) proto;
        failureNumber = p.getFailureNumber();
        nodeRunPosition = p.getNodeRunPosition();
        threadRunNumber = p.getThreadRunNumber();
    }

    public static FailureBeingHandledModel fromProto(FailureBeingHandled p, ExecutionContext context) {
        FailureBeingHandledModel out = new FailureBeingHandledModel();
        out.initFrom(p, context);
        return out;
    }

    public int getThreadRunNumber() {
        return this.threadRunNumber;
    }

    public int getNodeRunPosition() {
        return this.nodeRunPosition;
    }

    public int getFailureNumber() {
        return this.failureNumber;
    }

    public void setThreadRunNumber(final int threadRunNumber) {
        this.threadRunNumber = threadRunNumber;
    }

    public void setNodeRunPosition(final int nodeRunPosition) {
        this.nodeRunPosition = nodeRunPosition;
    }

    public void setFailureNumber(final int failureNumber) {
        this.failureNumber = failureNumber;
    }
}
