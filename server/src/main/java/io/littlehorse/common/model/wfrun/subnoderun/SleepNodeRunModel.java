package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.SleepNodeMatured;
import io.littlehorse.common.model.meta.subnode.SleepNodeModel;
import io.littlehorse.common.model.wfrun.FailureModel;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SleepNodeRun;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Date;

public class SleepNodeRunModel extends SubNodeRun<SleepNodeRun> {

    public Date maturationTime;

    public SleepNodeRunModel() {}

    public Class<SleepNodeRun> getProtoBaseClass() {
        return SleepNodeRun.class;
    }

    public void initFrom(Message proto) {
        SleepNodeRun p = (SleepNodeRun) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
    }

    public SleepNodeRun.Builder toProto() {
        return SleepNodeRun
            .newBuilder()
            .setMaturationTime(LHUtil.fromDate(maturationTime));
    }

    public static SleepNodeRunModel fromProto(SleepNodeRun p) {
        SleepNodeRunModel out = new SleepNodeRunModel();
        out.initFrom(p);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // nothing to do, we just wait for the event to come in.
        return false;
    }

    public void arrive(Date time) {
        // We need to schedule the timer that says "hey the node is done"

        SleepNodeModel sn = getNode().sleepNode;
        if (sn == null) {
            throw new RuntimeException("not possible to have non-sleep-node here.");
        }

        try {
            maturationTime = sn.getMaturationTime(nodeRunModel.getThreadRun());
            Command cmd = new Command();
            cmd.time = maturationTime;
            SleepNodeMatured snm = new SleepNodeMatured();
            snm.wfRunId = nodeRunModel.wfRunId;
            snm.threadRunNumber = nodeRunModel.threadRunNumber;
            snm.nodeRunPosition = nodeRunModel.position;

            cmd.setSubCommand(snm);

            LHTimer timer = new LHTimer();
            timer.maturationTime = maturationTime;
            timer.key = nodeRunModel.wfRunId;
            timer.topic =
                nodeRunModel
                    .getThreadRun()
                    .getWfRunModel()
                    .getDao()
                    .getCoreCmdTopic();
            timer.payload = cmd.toProto().build().toByteArray();

            nodeRunModel.getThreadRun().getWfRunModel().getDao().scheduleTimer(timer);
        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel(
                "Failed calculating maturation for timer: " + exn.getMessage(),
                LHConstants.VAR_SUB_ERROR
            );
            nodeRunModel.fail(failure, time);
        }
    }

    public void processSleepNodeMatured(SleepNodeMatured evt) {
        VariableValueModel nullOutput = new VariableValueModel();
        nullOutput.type = VariableType.NULL;

        // mark when we actually processed the completion, not when it was "supposed"
        // to come in. In cases where there's a large backlog of scheduler events,
        // this would be useful to help debug what's going on.
        nodeRunModel.complete(nullOutput, new Date());
    }

    @Override
    public boolean canBeInterrupted() {
        return true;
    }
}
