package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMaturedModel;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.SleepNodeModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SleepNodeRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class SleepNodeRunModel extends SubNodeRun<SleepNodeRun> {

    private Date maturationTime;
    private boolean matured;

    // Only contains value in Processor execution context.
    private ProcessorExecutionContext processorContext;

    public SleepNodeRunModel() {
        // used by lhdeserializer
    }

    public SleepNodeRunModel(ProcessorExecutionContext processorContext) {
        this.processorContext = processorContext;
    }

    @Override
    public Class<SleepNodeRun> getProtoBaseClass() {
        return SleepNodeRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SleepNodeRun p = (SleepNodeRun) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
        matured = p.getMatured();
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    @Override
    public SleepNodeRun.Builder toProto() {
        return SleepNodeRun.newBuilder()
                .setMaturationTime(LHUtil.fromDate(maturationTime))
                .setMatured(matured);
    }

    @Override
    public boolean checkIfProcessingCompleted(ProcessorExecutionContext processorContext) {
        return this.isMatured();
    }

    @Override
    public void arrive(Date time, ProcessorExecutionContext processorContext) throws NodeFailureException {
        // We need to schedule the timer that says "hey the node is done"
        SleepNodeModel sleepNode = getNode().sleepNode;
        if (sleepNode == null) {
            throw new RuntimeException("not possible to have non-sleep-node here.");
        }

        try {
            maturationTime = sleepNode.getMaturationTime(nodeRun.getThreadRun());
            SleepNodeMaturedModel snm = new SleepNodeMaturedModel(nodeRun.getId());
            CommandModel command = new CommandModel(snm, maturationTime);
            processorContext.getTaskManager().scheduleTimer(new LHTimer(command));

        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel(
                    "Failed calculating maturation for timer: " + exn.getMessage(), LHConstants.VAR_SUB_ERROR);
            throw new NodeFailureException(failure);
        }
    }

    @Override
    public boolean maybeHalt(ProcessorExecutionContext processorContext) {
        return true;
    }

    @Override
    public Optional<VariableValueModel> getOutput(ProcessorExecutionContext processorContext) {
        return Optional.empty();
    }

    public void processSleepNodeMatured(SleepNodeMaturedModel evt) {
        this.matured = true;
    }

    public static SleepNodeRunModel fromProto(SleepNodeRun p, ExecutionContext context) {
        SleepNodeRunModel out = new SleepNodeRunModel();
        out.initFrom(p, context);
        return out;
    }
}
