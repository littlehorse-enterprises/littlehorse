package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.StartThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StartThreadRunModel extends SubNodeRun<StartThreadRun> {

    public Integer childThreadId;
    public String threadSpecName;

    @Override
    public Class<StartThreadRun> getProtoBaseClass() {
        return StartThreadRun.class;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        StartThreadRun proto = (StartThreadRun) p;
        if (proto.hasChildThreadId()) childThreadId = proto.getChildThreadId();
        threadSpecName = proto.getThreadSpecName();
    }

    @Override
    public StartThreadRun.Builder toProto() {
        StartThreadRun.Builder out = StartThreadRun.newBuilder().setThreadSpecName(threadSpecName);

        if (childThreadId != null) {
            out.setChildThreadId(childThreadId);
        }

        return out;
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) {
        return true;
    }

    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {
        StartThreadNodeModel stn = getNode().startThreadNode;
        Map<String, VariableValueModel> variables = new HashMap<>();

        // First make sure we can construct the input variables.
        try {
            for (Map.Entry<String, VariableAssignmentModel> e : stn.variables.entrySet()) {
                variables.put(e.getKey(), nodeRun.getThreadRun().assignVariable(e.getValue()));
            }
        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel();
            failure.message = "Failed constructing input variables for thread: " + exn.getMessage();
            failure.failureName = LHConstants.VAR_SUB_ERROR;

            throw new NodeFailureException(failure);
        }

        // Next make sure all of the variables are valid for the child.
        String threadSpecName = nodeRun.getNode().getStartThreadNode().getThreadSpecName();
        ThreadSpecModel threadSpec = getWfSpec().getThreadSpecs().get(threadSpecName);
        try {
            threadSpec.validateStartVariables(variables);
        } catch (LHValidationException exn) {
            throw new NodeFailureException(new FailureModel(
                    "Invalid input variables for child thread: %s".formatted(exn.getMessage()),
                    LHErrorType.VAR_SUB_ERROR.toString()));
        }

        ThreadRunModel child = nodeRun.getThreadRun()
                .getWfRun()
                .startThread(threadSpecName, time, nodeRun.getThreadRunNumber(), variables, ThreadType.CHILD);

        this.childThreadId = child.getNumber();
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        return Optional.of(new VariableValueModel(childThreadId));
    }

    public static StartThreadRunModel fromProto(StartThreadRun p, ExecutionContext context) {
        StartThreadRunModel out = new StartThreadRunModel();
        out.initFrom(p, context);
        return out;
    }
}
