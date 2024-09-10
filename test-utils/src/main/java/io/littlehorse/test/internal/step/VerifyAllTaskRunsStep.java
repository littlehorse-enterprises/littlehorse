package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.ListTaskRunsRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.List;
import java.util.function.Consumer;

public class VerifyAllTaskRunsStep extends AbstractStep {

    private final Consumer<List<TaskRun>> matcher;

    public VerifyAllTaskRunsStep(Consumer<List<TaskRun>> consumer, int id) {
        super(id);
        this.matcher = consumer;
    }

    @Override
    public void tryExecute(TestExecutionContext ctx, LittleHorseBlockingStub client) {
        WfRunId wfRunId = ctx.getWfRunId();
        matcher.accept(client.listTaskRuns(
                        ListTaskRunsRequest.newBuilder().setWfRunId(wfRunId).build())
                .getResultsList());
    }
}
