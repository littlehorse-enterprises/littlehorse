package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.RunChildWfNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.RunChildWfNodeRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RunChildWfNodeRunModel extends SubNodeRun<RunChildWfNodeRun> {

    private WfRunIdModel childWfRunId;
    private Map<String, VariableValueModel> inputs = new HashMap<>();

    @Override
    public Class<RunChildWfNodeRun> getProtoBaseClass() {
        return RunChildWfNodeRun.class;
    }

    @Override
    public RunChildWfNodeRun.Builder toProto() {
        RunChildWfNodeRun.Builder out = RunChildWfNodeRun.newBuilder();
        if (childWfRunId != null) out.setChildWfRunId(childWfRunId.toProto());

        for (Map.Entry<String, VariableValueModel> entry : inputs.entrySet()) {
            out.putInputs(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        RunChildWfNodeRun p = (RunChildWfNodeRun) proto;
        if (p.hasChildWfRunId()) {
            this.childWfRunId = LHSerializable.fromProto(p.getChildWfRunId(), WfRunIdModel.class, ctx);
        }
        for (Map.Entry<String, VariableValue> entry : p.getInputsMap().entrySet()) {
            this.inputs.put(entry.getKey(), LHSerializable.fromProto(entry.getValue(), VariableValueModel.class, ctx));
        }
    }

    @Override
    public void arrive(Date time, CoreProcessorContext ctx) throws NodeFailureException {
        RunChildWfNodeModel runWfNode = getNode().getRunChildWfNode();

        // First make sure we can construct the input variables.
        try {
            for (Map.Entry<String, VariableAssignmentModel> e :
                    runWfNode.getInputs().entrySet()) {
                inputs.put(e.getKey(), nodeRun.getThreadRun().assignVariable(e.getValue()));
            }
        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel();
            failure.message = "Failed constructing input variables for child workflow: " + exn.getMessage();
            failure.failureName = LHConstants.VAR_SUB_ERROR;

            throw new NodeFailureException(failure);
        }

        WfSpecModel childSpec = runWfNode.getWfSpecToRun(nodeRun, ctx.metadataManager());

        this.childWfRunId =
                new WfRunIdModel(LHUtil.generateGuid(), nodeRun.getId().getWfRunId());

        WfRunModel out = new WfRunModel(ctx);
        out.setId(childWfRunId);

        out.setWfSpec(childSpec);
        out.setWfSpecId(childSpec.getId());
        out.startTime = ctx.currentCommand().getTime();
        out.transitionTo(LHStatus.RUNNING);

        out.startThread(
                childSpec.getEntrypointThreadName(),
                ctx.currentCommand().getTime(),
                null,
                inputs,
                ThreadType.ENTRYPOINT);
        ctx.getableManager().put(out);
        out.advance(time);
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        return true;
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        if (childWfRunId != null) {
            return Optional.of(new VariableValueModel(childWfRunId));
        } else {
            return Optional.empty();
        }
    }
}
