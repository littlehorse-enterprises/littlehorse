package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartMultipleThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.StartMultipleThreadsRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class StartMultipleThreadsRunModel extends SubNodeRun<StartMultipleThreadsRun> {

    @Setter
    private String threadSpecName;

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        StartMultipleThreadsRun nodeRun = (StartMultipleThreadsRun) proto;
        threadSpecName = nodeRun.getThreadSpecName();
    }

    @Override
    public StartMultipleThreadsRun.Builder toProto() {
        StartMultipleThreadsRun.Builder out = StartMultipleThreadsRun.newBuilder();
        out.setThreadSpecName(threadSpecName);
        return out;
    }

    @Override
    public Class<StartMultipleThreadsRun> getProtoBaseClass() {
        return StartMultipleThreadsRun.class;
    }

    @Override
    public boolean advanceIfPossible(Date time) {
        log.warn("Shouldn't get here");
        return false;
    }

    @Override
    public void arrive(Date time) {
        StartMultipleThreadsNodeModel nodeModel = getNode().getStartMultipleThreadsNode();
        List<Integer> createdThreads = new ArrayList<>();
        try {
            VariableValueModel iterable = nodeRun.getThreadRun()
                    .assignVariable(nodeModel.getIterable())
                    .asArr();
            for (Object threadInput : iterable.getJsonArrVal()) {
                VariableValueModel iterInput =
                        LHSerializable.fromProto(LHLibUtil.objToVarVal(threadInput), VariableValueModel.class);

                StartMultipleThreadsNodeModel node = nodeRun.getNode().getStartMultipleThreadsNode();

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

                ThreadRunModel child = nodeRun.getThreadRun()
                        .getWfRun()
                        .startThread(threadSpecName, time, parentThreadNumber, inputs, ThreadType.CHILD);
                createdThreads.add(child.getNumber());
                nodeRun.getThreadRun().getChildThreadIds().add(child.number);
            }
            VariableValueModel nodeOutput =
                    LHSerializable.fromProto(LHLibUtil.objToVarVal(createdThreads), VariableValueModel.class);
            nodeRun.complete(nodeOutput, time);
        } catch (LHVarSubError | LHSerdeError e) {
            FailureModel failure = new FailureModel();
            failure.message = "Failed constructing input variables for thread: " + e.getMessage();
            failure.failureName = LHConstants.VAR_SUB_ERROR;
            nodeRun.fail(failure, time);
        }
    }
}
