package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.common.model.meta.subnode.StartThreadNode;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.StartThreadRunPb;
import io.littlehorse.sdk.common.proto.ThreadTypePb;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StartThreadRun extends SubNodeRun<StartThreadRunPb> {

    public Integer childThreadId;
    public String threadSpecName;

    public Class<StartThreadRunPb> getProtoBaseClass() {
        return StartThreadRunPb.class;
    }

    public void initFrom(Message p) {
        StartThreadRunPb proto = (StartThreadRunPb) p;
        if (proto.hasChildThreadId()) childThreadId = proto.getChildThreadId();
        threadSpecName = proto.getThreadSpecName();
    }

    public StartThreadRunPb.Builder toProto() {
        StartThreadRunPb.Builder out = StartThreadRunPb
            .newBuilder()
            .setThreadSpecName(threadSpecName);

        if (childThreadId != null) {
            out.setChildThreadId(childThreadId);
        }

        return out;
    }

    public static StartThreadRun fromProto(StartThreadRunPb p) {
        StartThreadRun out = new StartThreadRun();
        out.initFrom(p);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // nothing to do
        return false;
    }

    public void arrive(Date time) {
        StartThreadNode stn = getNode().startThreadNode;
        Map<String, VariableValueModel> variables = new HashMap<>();

        try {
            for (Map.Entry<String, VariableAssignmentModel> e : stn.variables.entrySet()) {
                variables.put(
                    e.getKey(),
                    nodeRunModel.getThreadRun().assignVariable(e.getValue())
                );
            }
        } catch (LHVarSubError exn) {
            Failure failure = new Failure();
            failure.message =
                "Failed constructing input variables for thread: " + exn.getMessage();
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
                ThreadTypePb.CHILD
            );

        nodeRunModel.getThreadRun().getChildThreadIds().add(child.number);

        if (child.status == LHStatus.ERROR) {
            Failure failure = new Failure();
            failure.message =
                "Failed launching child thread. See child for details, id: " +
                child.number;

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
