package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMaturedModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.SleepNodeModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SleepNodeRun;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;

public class SleepNodeRunModel extends SubNodeRun<SleepNodeRun> {

    public Date maturationTime;
    private ExecutionContext executionContext;
    // Only contains value in Processor execution context.
    private ProcessorExecutionContext processorContext;

    public SleepNodeRunModel() {
        // used by lhdeserializer
    }

    public SleepNodeRunModel(ProcessorExecutionContext processorContext) {
        this.processorContext = processorContext;
    }

    public Class<SleepNodeRun> getProtoBaseClass() {
        return SleepNodeRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SleepNodeRun p = (SleepNodeRun) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
        this.executionContext = context;
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    public SleepNodeRun.Builder toProto() {
        return SleepNodeRun.newBuilder().setMaturationTime(LHUtil.fromDate(maturationTime));
    }

    public static SleepNodeRunModel fromProto(SleepNodeRun p, ExecutionContext context) {
        SleepNodeRunModel out = new SleepNodeRunModel();
        out.initFrom(p, context);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // nothing to do, we just wait for the event to come in.
        return false;
    }

    public void arrive(Date time) {
        // We need to schedule the timer that says "hey the node is done"

        SleepNodeModel sleepNode = getNode().sleepNode;
        if (sleepNode == null) {
            throw new RuntimeException("not possible to have non-sleep-node here.");
        }

        try {
            maturationTime = sleepNode.getMaturationTime(nodeRun.getThreadRun());
            SleepNodeMaturedModel snm = new SleepNodeMaturedModel(nodeRun.getId());
            CommandModel command = new CommandModel(snm, maturationTime);
            CoreProcessorDAO dao = nodeRun.getThreadRun().getWfRun().getDao();
            dao.scheduleTimer(new LHTimer(command, dao));

        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel(
                    "Failed calculating maturation for timer: " + exn.getMessage(), LHConstants.VAR_SUB_ERROR);
            nodeRun.fail(failure, time);
        }
    }

    public void processSleepNodeMatured(SleepNodeMaturedModel evt) {
        VariableValueModel nullOutput = new VariableValueModel();
        nullOutput.type = VariableType.NULL;

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
