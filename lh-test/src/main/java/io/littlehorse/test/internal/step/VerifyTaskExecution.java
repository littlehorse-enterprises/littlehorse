package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.test.LHClientTestWrapper;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.awaitility.Awaitility;

public class VerifyTaskExecution implements Step {

    private final int threadRunNumber;
    private final int nodeRunNumber;
    private final Consumer<TaskRun> matcher;

    public VerifyTaskExecution(int threadRunNumber, int nodeRunNumber, Consumer<TaskRun> matcher) {
        this.threadRunNumber = threadRunNumber;
        this.nodeRunNumber = nodeRunNumber;
        this.matcher = matcher;
    }

    @Override
    public void execute(Object context, LHClientTestWrapper lhClientWrapper) {
        String wfRunId = (String) context;
        Callable<NodeRun> getNodeRunExecution =
                () -> lhClientWrapper.getNodeRun(wfRunId, threadRunNumber, nodeRunNumber);
        NodeRun nodeRun = Awaitility.await().until(getNodeRunExecution, Objects::nonNull);
        TaskRunId taskRunId = nodeRun.getTask().getTaskRunId();
        Callable<TaskRun> getTaskRunExecution = () -> lhClientWrapper.getTaskRun(taskRunId);
        TaskRun taskRun = Awaitility.await().until(getTaskRunExecution, Objects::nonNull);
        matcher.accept(taskRun);
    }
}
