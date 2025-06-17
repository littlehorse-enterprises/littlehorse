package io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitForThread;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitingThreadStatus;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
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
    private Integer failureHandlerThreadRunId;

    private CoreProcessorContext context;
    private NodeRunModel nodeRun;

    public WaitForThreadModel() {}

    public WaitForThreadModel(
            NodeRunModel waitForThreadNodeRunModel,
            Integer threadRunNumberToWaitFor,
            Date currentCommandTime,
            CoreProcessorContext context)
            throws NodeFailureException {
        this.context = context;

        ThreadRunModel parentThreadRunModel = waitForThreadNodeRunModel.getThreadRun();
        this.threadRunNumber = threadRunNumberToWaitFor;
        ThreadRunModel threadRun = parentThreadRunModel.getWfRun().getThreadRun(threadRunNumber);

        if (threadRun == null) {
            throw new NodeFailureException(new FailureModel(
                    "Couldn't wait for nonexistent threadRun: " + threadRunNumber,
                    LHErrorType.VAR_SUB_ERROR.toString()));
        }

        // Make sure we're not waiting for a parent thread or grandparent, etc.
        ThreadRunModel potentialParent = parentThreadRunModel;
        while (potentialParent != null) {
            if (potentialParent.number == this.threadRunNumber) {
                throw new NodeFailureException(new FailureModel(
                        "Determined threadrunnumber " + threadRunNumber + " is a parent!", LHConstants.VAR_SUB_ERROR));
            }
            potentialParent = potentialParent.getParent();
        }

        this.threadStatus = threadRun.getStatus();
        this.waitingStatus = WaitingThreadStatus.THREAD_IN_PROGRESS;
        this.nodeRun = waitForThreadNodeRunModel;
    }

    @Override
    public Class<WaitForThread> getProtoBaseClass() {
        return WaitForThread.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WaitForThread p = (WaitForThread) proto;
        if (p.hasThreadEndTime()) {
            threadEndTime = LHUtil.fromProtoTs(p.getThreadEndTime());
        }
        threadStatus = p.getThreadStatus();
        threadRunNumber = p.getThreadRunNumber();
        waitingStatus = p.getWaitingStatus();

        if (p.hasFailureHandlerThreadRunId()) {
            failureHandlerThreadRunId = p.getFailureHandlerThreadRunId();
        }

        this.context = context.castOnSupport(CoreProcessorContext.class);
    }

    @Override
    public WaitForThread.Builder toProto() {
        WaitForThread.Builder out =
                WaitForThread.newBuilder().setThreadStatus(getThreadStatus()).setThreadRunNumber(threadRunNumber);
        if (threadEndTime != null) {
            out.setThreadEndTime(LHUtil.fromDate(threadEndTime));
        }
        out.setWaitingStatus(waitingStatus);

        if (failureHandlerThreadRunId != null) out.setFailureHandlerThreadRunId(failureHandlerThreadRunId);
        return out;
    }

    public LHStatus getThreadStatus() {
        if (nodeRun == null) return threadStatus;

        LHStatus out =
                nodeRun.getThreadRun().getWfRun().getThreadRun(threadRunNumber).getStatus();
        threadStatus = out;
        return out;
    }

    public void updateStatus() {}

    public boolean isFailed() {
        return threadStatus == LHStatus.EXCEPTION || threadStatus == LHStatus.ERROR;
    }
}
