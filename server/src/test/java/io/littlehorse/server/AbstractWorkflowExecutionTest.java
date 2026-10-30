package io.littlehorse.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Empty;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.TaskClaimEventPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/** Runs node behavior through both registered and inline workflow commands. */
@ExtendWith(AbstractWorkflowExecutionTest.WorkflowModeExtension.class)
public abstract class AbstractWorkflowExecutionTest extends AbstractCommandProcessorTest {
    private enum WorkflowMode {
        WF_SPEC,
        INLINE_SPEC
    }

    private WorkflowMode workflowMode;

    public static class WorkflowModeExtension implements TestTemplateInvocationContextProvider, BeforeEachCallback {
        @Override
        public boolean supportsTestTemplate(ExtensionContext context) {
            return true;
        }

        @Override
        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
            return Stream.of(WorkflowMode.values()).map(mode -> new TestTemplateInvocationContext() {
                @Override
                public String getDisplayName(int invocationIndex) {
                    return mode.name().toLowerCase();
                }

                @Override
                public java.util.List<Extension> getAdditionalExtensions() {
                    return java.util.List.of((BeforeEachCallback) invocation ->
                            ((AbstractWorkflowExecutionTest) invocation.getRequiredTestInstance()).workflowMode = mode);
                }
            });
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            if (!context.getRequiredTestMethod().isAnnotationPresent(TestTemplate.class)) {
                throw new IllegalStateException("Workflow execution tests must use @TestTemplate");
            }
        }
    }

    protected final WfRun startWorkflow(ThreadFunc workflow, Map<String, VariableValue> inputs) {
        String runId = UUID.randomUUID().toString();
        if (workflowMode == WorkflowMode.INLINE_SPEC) {
            RunInlineWfRequest request =
                    Workflow.inlineWorkflow(workflow).withWfRunId(runId).compileWorkflow().toBuilder()
                            .putAllVariables(inputs)
                            .build();
            WfRun run = execute(runId, command -> command.setRunInlineWf(request), WfRun.class);
            assertThat(run.getIsInline()).isTrue();
            assertThat(run.hasWfSpecId()).isFalse();
            return run;
        }
        PutWfSpecRequest definition =
                Workflow.newWorkflow("node-test-" + runId, workflow).compileWorkflow();
        WfSpec spec = executeMetadata(command -> command.setPutWfSpec(definition), WfSpec.class);
        RunWfRequest request = RunWfRequest.newBuilder()
                .setId(runId)
                .setWfSpecName(spec.getId().getName())
                .setMajorVersion(spec.getId().getMajorVersion())
                .setRevision(spec.getId().getRevision())
                .putAllVariables(inputs)
                .build();
        WfRun run = execute(runId, command -> command.setRunWf(request), WfRun.class);
        assertThat(run.hasWfSpecId()).isTrue();
        return run;
    }

    protected final NodeRunModel currentNode(WfRun run) {
        WfRunIdModel id = new WfRunIdModel(run.getId().getId());
        ThreadRun thread = readGetable(id).toProto().getThreadRuns(0);
        return readGetable(new NodeRunIdModel(id, 0, thread.getCurrentNodePosition()));
    }

    protected final TaskRunModel currentTask(WfRun run) {
        return readGetable(currentNode(run).getTaskRun().getTaskRunId());
    }

    protected final PollTaskResponse claimTask(TaskRunModel task) {
        TaskClaimEventPb claim = TaskClaimEventPb.newBuilder()
                .setTaskRunId(task.getId().toProto())
                .setTaskWorkerId("test-worker")
                .setTaskWorkerVersion("test-version")
                .setTime(LHUtil.fromDate(Date.from(commandTime)))
                .build();
        return execute(
                task.getId().getPartitionKey().orElseThrow(),
                command -> command.setTaskClaimEvent(claim),
                PollTaskResponse.class);
    }

    protected final void reportTask(TaskRunModel task, TaskStatus status, VariableValue output) {
        reportTask(task, report -> report.setStatus(status).setOutput(output));
    }

    protected final void reportTaskError(TaskRunModel task, LHTaskError error) {
        reportTask(task, report -> report.setStatus(TaskStatus.TASK_FAILED).setError(error));
    }

    protected final void reportTaskException(TaskRunModel task, LHTaskException exception) {
        reportTask(task, report -> report.setStatus(TaskStatus.TASK_EXCEPTION).setException(exception));
    }

    private void reportTask(TaskRunModel task, Consumer<ReportTaskRun.Builder> result) {
        ReportTaskRun.Builder report = ReportTaskRun.newBuilder()
                .setTaskRunId(task.getId().toProto())
                .setAttemptNumber(task.getAttempts().size() - 1)
                .setTime(LHUtil.fromDate(Date.from(commandTime)))
                .setTotalCheckpoints(task.getTotalCheckpoints());
        result.accept(report);
        execute(
                task.getId().getPartitionKey().orElseThrow(),
                command -> command.setReportTaskRun(report.build()),
                Empty.class);
    }
}
