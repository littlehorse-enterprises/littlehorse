package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.ListTaskRunsRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunList;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VerifyTaskRunOutputsStep extends AbstractStep {

    private List<Object> expectedOutputs;

    public VerifyTaskRunOutputsStep(int id, List<Object> expectedOutputs) {
        super(id);
        this.expectedOutputs = expectedOutputs;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        WfRunId wfRunId = context.getWfRunId();

        TaskRunList taskRuns = lhClient.listTaskRuns(
                ListTaskRunsRequest.newBuilder().setWfRunId(wfRunId).build());
        if (expectedOutputs.size() != taskRuns.getResultsList().size()) {
            throw new StepExecutionException(
                    id,
                    context.getWfRunId(),
                    "Expected %d taskRuns but got %d".formatted(expectedOutputs.size(), taskRuns.getResultsCount()));
        }

        // Currently, the result of the rpc ListTaskRuns is NOT sorted. It will
        // be sorted after LH-152. So for now we manually sort.
        List<VariableValue> expected =
                expectedOutputs.stream().map(LHLibUtil::objToVarVal).toList();

        // Make a copy because you can't modify the proto list
        List<TaskRun> sortedTasks = new ArrayList<>();
        taskRuns.getResultsList().stream().forEach(t -> sortedTasks.add(t));

        Collections.sort(sortedTasks, (task1, task2) -> {
            return task1.getSource().getTaskNode().getNodeRunId().getPosition()
                    - task2.getSource().getTaskNode().getNodeRunId().getPosition();
        });

        for (int i = 0; i < sortedTasks.size(); i++) {
            VariableValue taskOutput = sortedTasks.get(i).getAttempts(0).getOutput();

            if (!areEqual(taskOutput, expected.get(i))) {
                throw new StepExecutionException(id, context.getWfRunId(), "Task outputs didn't match!");
            }
        }
    }

    private boolean areEqual(VariableValue first, VariableValue second) {
        if (first.getValueCase() != second.getValueCase()) return false;

        switch (first.getValueCase()) {
            case INT:
                return first.getInt() == second.getInt();
            default:
        }
        throw new UnsupportedOperationException("As of now, only INT values are supported");
    }
}
