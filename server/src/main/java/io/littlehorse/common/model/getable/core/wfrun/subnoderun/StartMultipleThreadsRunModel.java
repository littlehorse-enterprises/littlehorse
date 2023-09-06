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
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.StartMultipleThreadsRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import java.util.ArrayList;
import java.util.Date;
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
            VariableValueModel iterable = nodeRunModel
                    .getThreadRun()
                    .assignVariable(nodeModel.getIterable())
                    .asArr();
            for (Object threadInput : iterable.getJsonArrVal()) {
                VariableValueModel iterInput =
                        LHSerializable.fromProto(LHLibUtil.objToVarVal(threadInput), VariableValueModel.class);
                ThreadRunModel child = nodeRunModel
                        .getThreadRun()
                        .getWfRunModel()
                        .startThread(
                                nodeRunModel
                                        .getNode()
                                        .getStartMultipleThreadsNode()
                                        .getThreadSpecName(),
                                time,
                                nodeRunModel.threadRunNumber,
                                Map.of(ThreadBuilder.HANDLER_INPUT_VAR, iterInput),
                                ThreadType.CHILD);
                createdThreads.add(child.getNumber());
            }
            VariableValueModel nodeOutput =
                    LHSerializable.fromProto(LHLibUtil.objToVarVal(createdThreads), VariableValueModel.class);
            nodeRunModel.complete(nodeOutput, time);
        } catch (LHVarSubError | LHSerdeError e) {
            FailureModel failure = new FailureModel();
            failure.message = "Failed constructing input variables for thread: " + e.getMessage();
            failure.failureName = LHConstants.VAR_SUB_ERROR;
            nodeRunModel.fail(failure, time);
        }
    }
}
