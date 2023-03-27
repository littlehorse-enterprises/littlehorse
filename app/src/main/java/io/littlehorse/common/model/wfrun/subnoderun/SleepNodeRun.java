package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.SleepNodeMatured;
import io.littlehorse.common.model.meta.subnode.SleepNode;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.SleepNodeRunPb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import java.util.Date;

public class SleepNodeRun extends SubNodeRun<SleepNodeRunPb> {

    public Date maturationTime;

    public SleepNodeRun() {}

    public Class<SleepNodeRunPb> getProtoBaseClass() {
        return SleepNodeRunPb.class;
    }

    public void initFrom(Message proto) {
        SleepNodeRunPb p = (SleepNodeRunPb) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
    }

    public SleepNodeRunPb.Builder toProto() {
        return SleepNodeRunPb
            .newBuilder()
            .setMaturationTime(LHUtil.fromDate(maturationTime));
    }

    public static SleepNodeRun fromProto(SleepNodeRunPb p) {
        SleepNodeRun out = new SleepNodeRun();
        out.initFrom(p);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // nothing to do, we just wait for the event to come in.
        return false;
    }

    public void arrive(Date time) {
        // We need to schedule the timer that says "hey the node is done"

        SleepNode sn = getNode().sleepNode;
        if (sn == null) {
            throw new RuntimeException("not possible to have non-sleep-node here.");
        }

        try {
            maturationTime = sn.getMaturationTime(nodeRun.threadRun);
            Command cmd = new Command();
            cmd.time = maturationTime;
            SleepNodeMatured snm = new SleepNodeMatured();
            snm.wfRunId = nodeRun.wfRunId;
            snm.threadRunNumber = nodeRun.threadRunNumber;
            snm.nodeRunPosition = nodeRun.position;

            cmd.setSubCommand(snm);

            LHTimer timer = new LHTimer();
            timer.maturationTime = maturationTime;
            timer.key = nodeRun.wfRunId;
            timer.topic = nodeRun.threadRun.wfRun.cmdDao.getWfRunEventQueue();
            timer.payload = cmd.toProto().build().toByteArray();

            nodeRun.threadRun.wfRun.cmdDao.scheduleTimer(timer);
        } catch (LHVarSubError exn) {
            Failure failure = new Failure(
                TaskResultCodePb.VAR_SUB_ERROR,
                "Failed calculating maturation for timer: " + exn.getMessage(),
                LHConstants.VAR_SUB_ERROR
            );
            nodeRun.fail(failure, time);
        }
    }

    public void processSleepNodeMatured(SleepNodeMatured evt) {
        VariableValue nullOutput = new VariableValue();
        nullOutput.type = VariableTypePb.NULL;

        // mark when we actually processed the completion, not when it was "supposed"
        // to come in. In cases where there's a large backlog of scheduler events,
        // this would be useful to help debug what's going on.
        nodeRun.complete(nullOutput, new Date());
    }

    @Override
    public boolean canBeInterrupted() {
        return true;
    }
}
