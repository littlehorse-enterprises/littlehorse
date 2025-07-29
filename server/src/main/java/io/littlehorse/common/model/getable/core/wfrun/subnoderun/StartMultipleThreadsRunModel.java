package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartMultipleThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StartMultipleThreadsRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
public class StartMultipleThreadsRunModel extends SubNodeRun<StartMultipleThreadsRun> {

    @Setter
    private String threadSpecName;

    private List<Integer> childThreadIds = new ArrayList<>();

    private ExecutionContext context;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        StartMultipleThreadsRun nodeRun = (StartMultipleThreadsRun) proto;
        threadSpecName = nodeRun.getThreadSpecName();
        childThreadIds.addAll(nodeRun.getChildThreadIdsList());
        this.context = context;
    }

    @Override
    public StartMultipleThreadsRun.Builder toProto() {
        StartMultipleThreadsRun.Builder out = StartMultipleThreadsRun.newBuilder()
                .setThreadSpecName(threadSpecName)
                .addAllChildThreadIds(childThreadIds);
        return out;
    }

    @Override
    public Class<StartMultipleThreadsRun> getProtoBaseClass() {
        return StartMultipleThreadsRun.class;
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) {
        return true;
    }

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {
        StartMultipleThreadsNodeModel node = getNode().getStartMultipleThreadsNode();
        try {
            VariableValueModel iterable =
                    nodeRun.getThreadRun().assignVariable(node.getIterable()).asArr();
            for (Object threadInput : iterable.getJsonArrVal()) {
                VariableValueModel iterInput =
                        LHSerializable.fromProto(LHLibUtil.objToVarVal(threadInput), VariableValueModel.class, context);

                // Construct input variables
                Map<String, VariableValueModel> inputs = new HashMap<>();

                for (Map.Entry<String, VariableAssignmentModel> inputVar :
                        node.getVariables().entrySet()) {
                    inputs.put(inputVar.getKey(), nodeRun.getThreadRun().assignVariable(inputVar.getValue()));
                }

                String threadSpecName = node.getThreadSpecName();
                int parentThreadNumber = nodeRun.getId().getThreadRunNumber();
                ThreadSpecModel threadSpec = getWfSpec().getThreadSpecs().get(threadSpecName);
                if (threadSpec.getInputVariableDefs().containsKey(WorkflowThread.HANDLER_INPUT_VAR)) {
                    inputs.put(WorkflowThread.HANDLER_INPUT_VAR, iterInput);
                }

                // Throws LHValidationError if variables not valid
                threadSpec.validateStartVariables(inputs);

                ThreadRunModel child = nodeRun.getThreadRun()
                        .getWfRun()
                        .startThread(threadSpecName, time, parentThreadNumber, inputs, ThreadType.CHILD);
                childThreadIds.add(child.getNumber());
            }
        } catch (LHVarSubError | LHSerdeException | LHValidationException e) {
            FailureModel failure = new FailureModel();
            failure.message = "Failed constructing input variables for thread: " + e.getMessage();
            failure.failureName = LHConstants.VAR_SUB_ERROR;
            throw new NodeFailureException(failure);
        }
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        VariableValue val = LHLibUtil.objToVarVal(childThreadIds);
        VariableValueModel valModel = VariableValueModel.fromProto(val, context);
        return Optional.of(valModel);
    }
}
