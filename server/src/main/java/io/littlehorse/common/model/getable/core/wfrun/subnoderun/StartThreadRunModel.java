package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.StartThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

public class StartThreadRunModel extends SubNodeRun<StartThreadRun> {

    public Integer childThreadId;
    public String threadSpecName;

    public Class<StartThreadRun> getProtoBaseClass() {
        return StartThreadRun.class;
    }

    public void initFrom(Message p) {
        StartThreadRun proto = (StartThreadRun) p;
        if (proto.hasChildThreadId()) childThreadId = proto.getChildThreadId();
        threadSpecName = proto.getThreadSpecName();
    }

    public StartThreadRun.Builder toProto() {
        StartThreadRun.Builder out = StartThreadRun.newBuilder().setThreadSpecName(threadSpecName);

        if (childThreadId != null) {
            out.setChildThreadId(childThreadId);
        }

        return out;
    }

    public static StartThreadRunModel fromProto(StartThreadRun p) {
        StartThreadRunModel out = new StartThreadRunModel();
        out.initFrom(p);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        return false;
    }

    public void arrive(Date time) {
        StartThreadNodeModel stn = getNode().startThreadNode;
        Map<String, VariableValueModel> variables = new HashMap<>();

        try {
            for (Map.Entry<String, VariableAssignmentModel> e : stn.variables.entrySet()) {
                variables.put(e.getKey(), nodeRunModel.getThreadRun().assignVariable(e.getValue()));
            }
        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel();
            failure.message = "Failed constructing input variables for thread: " + exn.getMessage();
            failure.failureName = LHConstants.VAR_SUB_ERROR;

            nodeRunModel.fail(failure, time);
        }

        ThreadRunModel child = nodeRunModel
                .getThreadRun()
                .getWfRunModel()
                .startThread(
                        nodeRunModel.getNode().startThreadNode.threadSpecName,
                        time,
                        nodeRunModel.threadRunNumber,
                        variables,
                        ThreadType.CHILD);

        nodeRunModel.getThreadRun().getChildThreadIds().add(child.number);

        if (child.status == LHStatus.ERROR) {
            FailureModel failure = new FailureModel();
            failure.message = "Failed launching child thread. See child for details, id: " + child.number;

            failure.failureName = LHConstants.CHILD_FAILURE;
            nodeRunModel.fail(failure, time);
        } else {
            // Then the variable output of this node is just the int thread id.
            VariableValueModel output = new VariableValueModel();
            output.type = VariableType.INT;
            output.intVal = Long.valueOf(child.number);

            nodeRunModel.complete(output, time);
        }
    }
}
