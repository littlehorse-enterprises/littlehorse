package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.awaitility.Awaitility;

public class VerifyTaskExecution extends AbstractStep {

    private final int threadRunNumber;
    private final int nodeRunNumber;
    private final Consumer<TaskRun> matcher;

    public VerifyTaskExecution(int threadRunNumber, int nodeRunNumber, Consumer<TaskRun> matcher, int id) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.nodeRunNumber = nodeRunNumber;
        this.matcher = matcher;
    }

    @Override
    public void tryExecute(Object context, LHPublicApiBlockingStub lhClient) {
        String wfRunId = (String) context;
        Callable<NodeRun> getNodeRunExecution =
                () -> this.getNodeRun(lhClient, wfRunId, threadRunNumber, nodeRunNumber);
        NodeRun nodeRun = Awaitility.await().until(getNodeRunExecution, Objects::nonNull);
        TaskRunId taskRunId = nodeRun.getTask().getTaskRunId();
        Callable<TaskRun> getTaskRunExecution = () -> lhClient.getTaskRun(taskRunId);
        TaskRun taskRun = Awaitility.await().until(getTaskRunExecution, Objects::nonNull);
        matcher.accept(taskRun);
    }

    private NodeRun getNodeRun(
            LHPublicApiBlockingStub lhClient, String wfRunId, int threadRunNumber, int nodeRunNumber) {
        NodeRunId nodeRunId = NodeRunId.newBuilder()
                .setWfRunId(WfRunId.newBuilder().setId(wfRunId))
                .setThreadRunNumber(threadRunNumber)
                .setPosition(nodeRunNumber)
                .build();
        return lhClient.getNodeRun(nodeRunId);
    }
}
