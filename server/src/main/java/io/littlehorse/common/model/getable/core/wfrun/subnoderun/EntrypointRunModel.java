package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.EntrypointRun;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Getter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class EntrypointRunModel extends SubNodeRun<EntrypointRun> {

    private Map<String, VariableValueModel> inputVariables = new HashMap<>();

    private ProcessorExecutionContext context;

    @Override
    public Class<EntrypointRun> getProtoBaseClass() {
        return EntrypointRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        EntrypointRun p = (EntrypointRun) proto;
        for (Map.Entry<String, VariableValue> entry : p.getInputVariablesMap().entrySet()) {
            inputVariables.put(entry.getKey(), VariableValueModel.fromProto(entry.getValue(), context));
        }

        this.context = context.castOnSupport(ProcessorExecutionContext.class);
    }

    @Override
    public EntrypointRun.Builder toProto() {
        EntrypointRun.Builder out = EntrypointRun.newBuilder();
        for (Map.Entry<String, VariableValueModel> entry : inputVariables.entrySet()) {
            out.putInputVariables(entry.getKey(), entry.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public Optional<VariableValueModel> getOutput() {
        return Optional.empty();
    }

    @Override
    public boolean checkIfProcessingCompleted() throws NodeFailureException {
        return true;
    }

    @Override
    public void arrive(Date time) throws NodeFailureException {
        // First, validate start variables.
        ThreadSpecModel threadSpec = nodeRun.getNode().getThreadSpecModel();
        try {
            threadSpec.validateStartVariables(inputVariables);
        } catch (LHValidationError exn) {
            FailureModel failure = new FailureModel(
                            "Failed validating variables on start: " + exn.getMessage(),
                            LHConstants.VAR_SUB_ERROR);
            throw new NodeFailureException(failure);
        }

        WfRunIdModel wfRunId = nodeRun.getId().getWfRunId();
        int threadRunNumber = nodeRun.getId().getThreadRunNumber();

        // Next, save them into the data store.
        for (ThreadVarDefModel threadVarDef : threadSpec.getVariableDefs()) {
            VariableDefModel varDef = threadVarDef.getVarDef();
            String varName = varDef.getName();
            VariableValueModel val;

            if (threadVarDef.getAccessLevel() == WfRunVariableAccessLevel.INHERITED_VAR) {
                // We do NOT create a variable since we want to use the one from the parent.
                continue;
            }

            if (inputVariables.containsKey(varName)) {
                val = inputVariables.get(varName);
            } else if (varDef.getDefaultValue() != null) {
                val = varDef.getDefaultValue();
            } else {
                // TODO: Will need to update this when we add the required variable feature.
                val = new VariableValueModel();
            }

            VariableModel variable = new VariableModel(varName, val, wfRunId, threadRunNumber, threadSpec.getWfSpecModel());
            context.getableManager().put(variable);
        }
    }

    public static EntrypointRunModel fromProto(EntrypointRun p, ExecutionContext context) {
        EntrypointRunModel out = new EntrypointRunModel();
        out.initFrom(p, context);
        return out;
    }
}
