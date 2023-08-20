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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandlingFailureHaltReasonModel extends LHSerializable<HandlingFailureHaltReason> implements SubHaltReason {

    public int handlerThreadId;

    public boolean isResolved(WfRunModel wfRunModel) {
        ThreadRunModel handlerThread = wfRunModel.threadRunModels.get(handlerThreadId);
        log.debug(
                "HandlingFailureHaltReason for failed thread {}: handler thread " + "status is {}",
                handlerThread.getFailureBeingHandled().getThreadRunNumber(),
                handlerThread.getStatus());
        if (handlerThread.status == LHStatus.COMPLETED) {
            // Need to figure out if the handler thread was handling another
            // failed thread.
            ThreadRunModel originalThatFailed = wfRunModel.threadRunModels
                    .get(handlerThread.failureBeingHandled.getThreadRunNumber());
            NodeRunModel handledNode = originalThatFailed
                    .getNodeRun(handlerThread.failureBeingHandled.getNodeRunPosition());

            if (handledNode.type == NodeTypeCase.WAIT_THREADS) {
                // The current implementation of failure handlers for wait_thread nodes
                // is an all-or-nothing handler that catches all failed children.
                //
                // Therefore, what we must do here is add each of the failed children.
                for (WaitForThreadModel wft : handledNode.getWaitThreadsRun().getThreads()) {
                    if (wft.getThreadStatus() == LHStatus.ERROR) {
                        originalThatFailed.handledFailedChildren.add(wft.getThreadRunNumber());
                    } else if (wft.getThreadStatus() != LHStatus.COMPLETED) {
                        log.warn("Impossible: handling failure for a WaitThreadNode "
                                + "and found a non-terminated child");
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Class<HandlingFailureHaltReason> getProtoBaseClass() {
        return HandlingFailureHaltReason.class;
    }

    public HandlingFailureHaltReason.Builder toProto() {
        HandlingFailureHaltReason.Builder out = HandlingFailureHaltReason.newBuilder();
        out.setHandlerThreadId(handlerThreadId);
        return out;
    }

    public void initFrom(Message proto) {
        HandlingFailureHaltReason p = (HandlingFailureHaltReason) proto;
        handlerThreadId = p.getHandlerThreadId();
    }

    public static HandlingFailureHaltReasonModel fromProto(HandlingFailureHaltReason proto) {
        HandlingFailureHaltReasonModel out = new HandlingFailureHaltReasonModel();
        out.initFrom(proto);
        return out;
    }
}
