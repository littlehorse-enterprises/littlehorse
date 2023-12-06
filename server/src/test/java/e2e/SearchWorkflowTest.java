package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.COMPLETED;
import static io.littlehorse.sdk.common.proto.LHStatus.RUNNING;

import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecIdList;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.SearchResultCaptor;
import io.littlehorse.test.WfRunTestContext;
import io.littlehorse.test.WorkflowVerifier;
import java.util.List;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = "external-event")
public class SearchWorkflowTest {

    @LHWorkflow("complex-workflow")
    private Workflow complexWorkflow;

    private WorkflowVerifier workflowVerifier;
    private final SearchResultCaptor<WfRunIdList> wfRunIdListCaptor = SearchResultCaptor.of(WfRunIdList.class);
    private final SearchResultCaptor<WfSpecIdList> wfSpecIdListCaptor = SearchResultCaptor.of(WfSpecIdList.class);

    @Test
    public void shouldFindRunningWorkflows() {
        Function<WfRunTestContext, SearchWfRunRequest> searchByNameAndStatusRunning =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("complex-workflow")
                        .setStatus(RUNNING)
                        .build();
        workflowVerifier
                .prepareRun(complexWorkflow)
                .waitForStatus(RUNNING)
                .doSearch(SearchWfRunRequest.class, wfRunIdListCaptor.capture(), searchByNameAndStatusRunning)
                .thenSendExternalEventJsonContent("external-event", "{}")
                .waitForStatus(COMPLETED)
                .start();
        WfRunIdList completedWorkflowIds = wfRunIdListCaptor.getValue().get();
        Assertions.assertThat(completedWorkflowIds.getResultsList()).hasSize(1);
    }

    @Test
    public void shouldFindCompletedWorkflows() {
        Function<WfRunTestContext, SearchWfRunRequest> searchByNameAndStatusCompleted =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("complex-workflow")
                        .setStatus(COMPLETED)
                        .build();
        workflowVerifier
                .prepareRun(complexWorkflow)
                .waitForStatus(RUNNING)
                .thenSendExternalEventJsonContent("external-event", "{}")
                .waitForStatus(COMPLETED)
                .doSearch(SearchWfRunRequest.class, wfRunIdListCaptor.capture(), searchByNameAndStatusCompleted)
                .start();
        WfRunIdList completedWorkflowIds = wfRunIdListCaptor.getValue().get();
        Assertions.assertThat(completedWorkflowIds.getResultsList()).hasSize(1);
    }

    @Test
    public void shouldFindWfSpecByName() {
        Function<WfRunTestContext, SearchWfSpecRequest> searchWfSpecByName = context ->
                SearchWfSpecRequest.newBuilder().setName("complex-workflow").build();
        workflowVerifier
                .prepareRun(complexWorkflow)
                .doSearch(SearchWfSpecRequest.class, wfSpecIdListCaptor.capture(), searchWfSpecByName)
                .start();
        WfSpecIdList wfSpecIdList = wfSpecIdListCaptor.getValue().get();
        List<WfSpecId> specIds = wfSpecIdList.getResultsList();
        Assertions.assertThat(specIds).hasSize(1);
        WfSpecId foundWfSpec = specIds.get(0);
        Assertions.assertThat(foundWfSpec.getName()).isEqualTo("complex-workflow");
        Assertions.assertThat(foundWfSpec.getRevision()).isEqualTo(0);
        Assertions.assertThat(foundWfSpec.getMajorVersion()).isEqualTo(0);
    }

    @Test
    public void shouldFindAllWfSpec() {
        Function<WfRunTestContext, SearchWfSpecRequest> searchWfSpecByName =
                context -> SearchWfSpecRequest.newBuilder().build();
        workflowVerifier
                .prepareRun(complexWorkflow)
                .doSearch(SearchWfSpecRequest.class, wfSpecIdListCaptor.capture(), searchWfSpecByName)
                .start();
        WfSpecIdList wfSpecIdList = wfSpecIdListCaptor.getValue().get();
        List<WfSpecId> specIds = wfSpecIdList.getResultsList();
        Assertions.assertThat(specIds).hasSize(1);
    }

    @LHWorkflow("complex-workflow")
    public Workflow getEqualsWorkflowImpl() {
        return new WorkflowImpl("complex-workflow", thread -> {
            thread.waitForEvent("external-event");
            thread.execute("my-task");
        });
    }

    @LHTaskMethod("my-task")
    public void myTask() {
        System.out.println("Hello from my task");
    }
}
