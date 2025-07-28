package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils.WaitForThreadModel;
import io.littlehorse.sdk.common.proto.HandlingFailureHaltReason;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandlingFailureHaltReasonModel extends LHSerializable<HandlingFailureHaltReason> implements SubHaltReason {

    @Getter
    private int handlerThreadId;

    public HandlingFailureHaltReasonModel() {}

    public HandlingFailureHaltReasonModel(int handlerThreadId) {
        this.handlerThreadId = handlerThreadId;
    }

    @Override
    public Class<HandlingFailureHaltReason> getProtoBaseClass() {
        return HandlingFailureHaltReason.class;
    }

    @Override
    public HandlingFailureHaltReason.Builder toProto() {
        HandlingFailureHaltReason.Builder out = HandlingFailureHaltReason.newBuilder();
        out.setHandlerThreadId(handlerThreadId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        HandlingFailureHaltReason p = (HandlingFailureHaltReason) proto;
        handlerThreadId = p.getHandlerThreadId();
    }

    @Override
    public boolean isResolved(ThreadRunModel haltedThread) {
        WfRunModel wfRunModel = haltedThread.getWfRun();
        ThreadRunModel handlerThread = wfRunModel.getThreadRun(handlerThreadId);
        log.debug(
                "HandlingFailureHaltReason for failed thread {}: handler thread " + "status is {}",
                handlerThread.getFailureBeingHandled().getThreadRunNumber(),
                handlerThread.getStatus());

        ThreadRunModel originalThatFailed =
                wfRunModel.getThreadRun(handlerThread.failureBeingHandled.getThreadRunNumber());
        NodeRunModel handledNode =
                originalThatFailed.getNodeRun(handlerThread.failureBeingHandled.getNodeRunPosition());

        if (handlerThread.status == LHStatus.COMPLETED) {
            handledNode.getLatestFailure().get().setProperlyHandled(true);

            if (handledNode.getType() == NodeTypeCase.WAIT_FOR_THREADS) {
                // The current implementation of failure handlers for wait_thread nodes
                // is an all-or-nothing handler that catches all failed children.
                //
                // Therefore, what we must do here is add each of the failed children.
                for (WaitForThreadModel wft : handledNode.getWaitForThreadsRun().getThreads()) {
                    if (wft.getThreadStatus() == LHStatus.ERROR || wft.getThreadStatus() == LHStatus.EXCEPTION) {
                        originalThatFailed.handledFailedChildren.add(wft.getThreadRunNumber());
                    } else if (wft.getThreadStatus() != LHStatus.COMPLETED
                            && wft.getThreadStatus() != LHStatus.HALTED) {
                        log.warn("Impossible: handling failure for a WaitThreadNode "
                                + "and found a non-terminated child");
                    }
                }
            }
            return true;
        } else if (handlerThread.status == LHStatus.EXCEPTION || handlerThread.status == LHStatus.ERROR) {
            // Shouldn't need to do anything here since the new ThreadRunModel#advance()
            // will detect this and fail
            // anyways. Just remove the halt reason so that the threadRun properly fails.
            return true;
        } else {
            return false;
        }
    }

    public static HandlingFailureHaltReasonModel fromProto(HandlingFailureHaltReason proto, ExecutionContext context) {
        HandlingFailureHaltReasonModel out = new HandlingFailureHaltReasonModel();
        out.initFrom(proto, context);
        return out;
    }
}
