package io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitForThread;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitingThreadStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WaitForThreadModel extends LHSerializable<WaitForThread> {

    private Date threadEndTime;
    private LHStatus threadStatus;
    private int threadRunNumber;
    private WaitingThreadStatus waitingStatus;
    private ExecutionContext executionContext;

    public Class<WaitForThread> getProtoBaseClass() {
        return WaitForThread.class;
    }

    public WaitForThreadModel() {}

    public WaitForThreadModel(
            NodeRunModel waitForThreadNodeRunModel, Integer threadRunNumberToWaitFor, Date currentCommandTime)
            throws LHVarSubError {
        ThreadRunModel parentThreadRunModel = waitForThreadNodeRunModel.getThreadRun();
        this.threadRunNumber = threadRunNumberToWaitFor;
        ThreadRunModel threadRunModel = parentThreadRunModel.getWfRun().getThreadRun(threadRunNumber);

        if (threadRunModel == null) {
            throw new LHVarSubError(null, "Couldn't wait for nonexistent threadRun: " + threadRunNumber);
        }

        // Make sure we're not waiting for a parent thread or grandparent, etc.
        ThreadRunModel potentialParent = parentThreadRunModel;
        while (potentialParent != null) {
            if (potentialParent.number == this.threadRunNumber) {
                waitForThreadNodeRunModel.fail(
                        new FailureModel(
                                "Determined threadrunnumber " + threadRunNumber + " is a parent!",
                                LHConstants.VAR_SUB_ERROR),
                        currentCommandTime);
            }
            potentialParent = potentialParent.getParent();
        }

        this.threadStatus = threadRunModel.getStatus();
        this.waitingStatus = WaitingThreadStatus.THREAD_IN_PROGRESS;
    }

    public void initFrom(Message proto, ExecutionContext context) {
        WaitForThread p = (WaitForThread) proto;
        if (p.hasThreadEndTime()) {
            threadEndTime = LHUtil.fromProtoTs(p.getThreadEndTime());
        }
        threadStatus = p.getThreadStatus();
        threadRunNumber = p.getThreadRunNumber();
        waitingStatus = p.getWaitingStatus();
    }

    public WaitForThread.Builder toProto() {
        WaitForThread.Builder out =
                WaitForThread.newBuilder().setThreadStatus(threadStatus).setThreadRunNumber(threadRunNumber);
        if (threadEndTime != null) {
            out.setThreadEndTime(LHUtil.fromDate(threadEndTime));
        }
        out.setWaitingStatus(waitingStatus);
        return out;
    }

    public boolean isFailed() {
        return threadStatus == LHStatus.EXCEPTION || threadStatus == LHStatus.ERROR;
    }
}
