package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.InlineWorkflow;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.LHTest;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = "inline-prototype-release")
public class InlineWfRunTest {
    private LittleHorseBlockingStub client;

    private InlineWorkflow inlineWorkflow() {
        return Workflow.inlineWorkflow(thread -> {
            var input = thread.declareStr("input").withDefault("hello");
            thread.waitForEvent("inline-prototype-release");
        });
    }

    private InlineWfSpec definition() {
        return inlineWorkflow().compileWorkflow().getWfSpec();
    }

    private RunInlineWfRequest request(String id) {
        return inlineWorkflow().withWfRunId(id).compileWorkflow().toBuilder().build();
    }

    @Test
    void startsInlineRunWithInputsAndPersistsCompletedEntrypoint() {
        String id = UUID.randomUUID().toString();
        RunInlineWfRequest request = Workflow.inlineWorkflow(thread -> {
                    var input = thread.declareStr("input").withDefault("default");
                    thread.complete(input);
                })
                .withWfRunId(id)
                .withRetentionPolicy(WorkflowRetentionPolicy.newBuilder()
                        .setSecondsAfterWfTermination(3600)
                        .build())
                .compileWorkflow()
                .toBuilder()
                .putVariables(
                        "input", VariableValue.newBuilder().setStr("provided").build())
                .build();

        WfRun run = client.runInlineWf(request);
        assertThat(run.getId().getId()).isEqualTo(id);
        assertThat(run.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(run.hasWfSpecId()).isFalse();
        assertThat(run.getIsInline()).isTrue();
        assertThat(run.getThreadRunsCount()).isEqualTo(1);
        assertThat(run.getThreadRuns(0).hasWfSpecId()).isFalse();
        assertThat(run.getThreadRuns(0).getOutput().getStr()).isEqualTo("provided");
        assertThat(client.getWfRun(run.getId())).isEqualTo(run);
        InlineWfSpec definition = client.getInlineWfSpec(run.getId());
        assertThat(definition)
                .isEqualTo(request.getWfSpec().toBuilder()
                        .setId(run.getId())
                        .setCreatedAt(run.getStartTime())
                        .build());
        Variable variable = client.getVariable(VariableId.newBuilder()
                .setWfRunId(run.getId())
                .setThreadRunNumber(0)
                .setName("input")
                .build());
        assertThat(variable.getValue().getStr()).isEqualTo("provided");
        assertThat(variable.hasWfSpecId()).isFalse();

        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(run.getId()).build());
        assertDefinitionDeleted(run.getId());
    }

    @Test
    void executesWithoutRegisteredMetadataAcrossEventsAndTaskRetries() {
        WfRun run = client.runInlineWf(request(UUID.randomUUID().toString()));
        assertThat(run.getIsInline()).isTrue();
        assertThat(run.hasWfSpecId()).isFalse();
        InlineWfSpec snapshot = client.getInlineWfSpec(run.getId());
        assertThat(snapshot.getId()).isEqualTo(run.getId());
        assertThat(snapshot.hasCreatedAt()).isTrue();
        assertThat(snapshot.getThreadSpecsCount()).isGreaterThan(0);
        assertThat(snapshot.getEntrypointThreadName()).isEqualTo(definition().getEntrypointThreadName());
        assertThatThrownBy(() -> client.getLatestWfSpec(GetLatestWfSpecRequest.newBuilder()
                        .setName("unregistered-inline-prototype")
                        .build()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));

        // A later command must reload the separate definition through the run's reference.
        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setWfRunId(run.getId())
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName("inline-prototype-release"))
                .build());
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(
                        client.getWfRun(run.getId()).getStatus())
                .isEqualTo(LHStatus.COMPLETED));

        WfRun completed = client.getWfRun(run.getId());
        assertThat(completed.getThreadRuns(0).getOutput().getStr()).isEqualTo("hello world");
        assertThat(completed.getIsInline()).isTrue();
        assertThat(client.getInlineWfSpec(completed.getId())).isEqualTo(snapshot);
        assertThat(completed.getThreadRuns(0).hasWfSpecId()).isFalse();
        var tasks = client.listTaskRuns(
                ListTaskRunsRequest.newBuilder().setWfRunId(run.getId()).build());
        assertThat(tasks.getResultsCount()).isEqualTo(1);
        assertThat(tasks.getResults(0).getAttemptsCount()).isEqualTo(2);
        assertThat(tasks.getResults(0).getStatus()).isEqualTo(TaskStatus.TASK_SUCCESS);
        var nodes = client.listNodeRuns(
                ListNodeRunsRequest.newBuilder().setWfRunId(run.getId()).build());
        assertThat(nodes.getResultsList())
                .allSatisfy(node -> assertThat(node.hasWfSpecId()).isFalse());
        Variable variable = client.getVariable(VariableId.newBuilder()
                .setWfRunId(run.getId())
                .setThreadRunNumber(0)
                .setName("input")
                .build());
        assertThat(variable.getValue().getStr()).isEqualTo("hello");
        assertThat(variable.hasWfSpecId()).isFalse();

        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(run.getId()).build());
        assertDefinitionDeleted(run.getId());
        assertThatThrownBy(() -> client.getWfRun(run.getId()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));
    }

    @Test
    void rejectsDuplicatesAndMigrationAndPreservesExistingDefinition() {
        RunInlineWfRequest request = request(UUID.randomUUID().toString());
        WfRun run = client.runInlineWf(request);
        InlineWfSpec snapshot = client.getInlineWfSpec(run.getId());
        assertThatThrownBy(() -> client.runInlineWf(request))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.ALREADY_EXISTS));
        assertThatThrownBy(() -> client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                        .setWfRunId(run.getId())
                        .build()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.FAILED_PRECONDITION));
        assertThat(client.getInlineWfSpec(run.getId())).isEqualTo(snapshot);
        WfRun second = client.runInlineWf(
                request.toBuilder().setId(UUID.randomUUID().toString()).build());
        InlineWfSpec secondSnapshot = client.getInlineWfSpec(second.getId());
        assertThat(secondSnapshot.getId()).isNotEqualTo(snapshot.getId());
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(run.getId()).build());
        assertDefinitionDeleted(run.getId());
        assertThat(client.getInlineWfSpec(second.getId())).isEqualTo(secondSnapshot);
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(second.getId()).build());
    }

    @Test
    void resolvesInlineDefinitionForChildThreadsSleepAndFailureHandlers() {
        RunInlineWfRequest request = Workflow.inlineWorkflow(thread -> {
                    var result = thread.declareStr("result");
                    var child = thread.spawnThread(
                            childThread -> {
                                childThread.sleepSeconds(0);
                                childThread.mutate(
                                        result,
                                        VariableMutationType.ASSIGN,
                                        childThread
                                                .execute("inline-prototype-task", "child")
                                                .withRetries(1));
                            },
                            "child",
                            null);
                    thread.waitForThreads(SpawnedThreads.of(child));
                    var failingTask = thread.execute("inline-prototype-task", "fail");
                    thread.handleError(
                            failingTask, handler -> handler.mutate(result, VariableMutationType.ASSIGN, "handled"));
                    thread.complete(result);
                })
                .compileWorkflow();
        WfRun run = client.runInlineWf(request);
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(
                        client.getWfRun(run.getId()).getStatus())
                .isEqualTo(LHStatus.COMPLETED));
        WfRun completed = client.getWfRun(run.getId());
        assertThat(completed.getThreadRuns(0).getOutput().getStr()).isEqualTo("handled");
        assertThat(completed.getThreadRunsCount()).isEqualTo(3);
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(run.getId()).build());
    }

    @Test
    void deletesInlineRunWithItsRetentionPolicy() {
        RunInlineWfRequest request = Workflow.inlineWorkflow(thread -> {})
                .withRetentionPolicy(WorkflowRetentionPolicy.newBuilder()
                        .setSecondsAfterWfTermination(0)
                        .build())
                .compileWorkflow();
        WfRun run = client.runInlineWf(request);
        assertThat(run.getStatus()).isEqualTo(LHStatus.COMPLETED);
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThatThrownBy(
                        () -> client.getWfRun(run.getId()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND)));
        assertDefinitionDeleted(run.getId());
    }

    @Test
    void rejectsInvalidDefinitionAndInputsBeforeCreatingRun() {
        String id = UUID.randomUUID().toString();
        assertThatThrownBy(() -> client.runInlineWf(request(id).toBuilder()
                        .setWfSpec(definition().toBuilder().setEntrypointThreadName("missing"))
                        .build()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
        assertThatThrownBy(() -> client.runInlineWf(request(id).toBuilder()
                        .putVariables(
                                "unknown",
                                VariableValue.newBuilder().setStr("hello").build())
                        .build()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
        assertThatThrownBy(() -> client.getWfRun(WfRunId.newBuilder().setId(id).build()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));
        assertDefinitionDeleted(WfRunId.newBuilder().setId(id).build());
    }

    private void assertDefinitionDeleted(WfRunId id) {
        assertThatThrownBy(() -> client.getInlineWfSpec(id))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));
    }

    @Test
    void rejectsServerManagedFieldsInSubmittedSpec() {
        String id = UUID.randomUUID().toString();
        for (InlineWfSpec supplied : java.util.List.of(
                definition().toBuilder().setId(WfRunId.getDefaultInstance()).build(),
                definition().toBuilder()
                        .setCreatedAt(com.google.protobuf.Timestamp.getDefaultInstance())
                        .build())) {
            assertThatThrownBy(() -> client.runInlineWf(
                            request(id).toBuilder().setWfSpec(supplied).build()))
                    .isInstanceOfSatisfying(
                            StatusRuntimeException.class,
                            ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
        }
        assertDefinitionDeleted(WfRunId.newBuilder().setId(id).build());
    }

    @Test
    void rejectsInlineDefinitionLookupForRegisteredRun() {
        WfSpec spec = client.putWfSpec(Workflow.newWorkflow("registered-" + UUID.randomUUID(), thread -> {})
                .compileWorkflow());
        RunWfRequest request = RunWfRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setWfSpecName(spec.getId().getName())
                .setMajorVersion(spec.getId().getMajorVersion())
                .setRevision(spec.getId().getRevision())
                .build();
        WfRun run = Awaitility.await()
                .ignoreExceptionsMatching(error -> error instanceof StatusRuntimeException grpcError
                        && grpcError.getStatus().getCode() == Status.Code.NOT_FOUND)
                .until(() -> client.runWf(request), result -> result != null);

        assertThat(run.getIsInline()).isFalse();
        assertDefinitionDeleted(run.getId());
        assertThat(client.getWfRun(run.getId())).isEqualTo(run);

        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(run.getId()).build());
        client.deleteWfSpec(DeleteWfSpecRequest.newBuilder().setId(spec.getId()).build());
    }

    @Test
    void rejectsLookupWithoutOwner() {
        assertThatThrownBy(() -> client.getInlineWfSpec(WfRunId.getDefaultInstance()))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @LHTaskMethod("inline-prototype-task")
    public String task(String input, WorkerContext context) {
        if (context.getAttemptNumber() == 0) throw new IllegalStateException("Retry this task");
        return input + " world";
    }
}
