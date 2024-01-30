package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.COMPLETED;
import static io.littlehorse.sdk.common.proto.LHStatus.RUNNING;

import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableIdList;
import io.littlehorse.sdk.common.proto.VariableMatch;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecIdList;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.SearchResultCaptor;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.List;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@LHTest(externalEventNames = "external-event")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WfRunSearchTest {

    @LHWorkflow("complex-workflow")
    private Workflow complexWorkflow;

    @LHWorkflow("searchable-variable-wf")
    private Workflow searchableVariableWf;

    private WorkflowVerifier workflowVerifier;
    private final SearchResultCaptor<WfRunIdList> wfRunIdListCaptor = SearchResultCaptor.of(WfRunIdList.class);
    private final SearchResultCaptor<WfSpecIdList> wfSpecIdListCaptor = SearchResultCaptor.of(WfSpecIdList.class);

    @Test
    @Order(0)
    public void shouldFindWfRun() {
        Function<TestExecutionContext, SearchWfRunRequest> searchByNameAndStatusRunning =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("complex-workflow")
                        .setStatus(RUNNING)
                        .build();
        Function<TestExecutionContext, SearchWfRunRequest> searchByNameAndStatusCompleted =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("complex-workflow")
                        .setStatus(COMPLETED)
                        .build();
        workflowVerifier
                .prepareRun(complexWorkflow)
                .waitForStatus(RUNNING)
                .doSearch(SearchWfRunRequest.class, wfRunIdListCaptor.capture(), searchByNameAndStatusRunning)
                .thenSendExternalEventJsonContent("external-event", "{}")
                .waitForStatus(COMPLETED)
                .doSearch(SearchWfRunRequest.class, wfRunIdListCaptor.capture(), searchByNameAndStatusCompleted)
                .start();
        WfRunIdList runningWorkflowIds = wfRunIdListCaptor.getValue().get();
        WfRunIdList completedWorkflowIds = wfRunIdListCaptor.getValue().get();
        Assertions.assertThat(completedWorkflowIds.getResultsList()).hasSize(1);
        Assertions.assertThat(runningWorkflowIds.getResultsList()).hasSize(1);
    }

    @Test
    @Order(1)
    public void shouldFindWfSpecByName() {
        Function<TestExecutionContext, SearchWfSpecRequest> searchWfSpecByName = context ->
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
    @Order(1)
    public void shouldFindAllWfSpec() {
        Function<TestExecutionContext, SearchWfSpecRequest> searchWfSpecByName =
                context -> SearchWfSpecRequest.newBuilder().build();
        workflowVerifier
                .prepareRun(complexWorkflow)
                .doSearch(SearchWfSpecRequest.class, wfSpecIdListCaptor.capture(), searchWfSpecByName)
                .start();
        WfSpecIdList wfSpecIdList = wfSpecIdListCaptor.getValue().get();
        List<WfSpecId> specIds = wfSpecIdList.getResultsList();
        Assertions.assertThat(specIds).isNotEmpty();
    }

    @Test
    void shouldFindCompletedWorkflow() {
        Function<TestExecutionContext, SearchWfRunRequest> searchWfSpecByName =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("searchable-variable-wf")
                        .setStatus(LHStatus.COMPLETED)
                        .build();

        SearchResultCaptor<WfRunIdList> captor = SearchResultCaptor.of(WfRunIdList.class);

        WfRunId wfRunId = workflowVerifier
                .prepareRun(searchableVariableWf)
                .waitForStatus(LHStatus.COMPLETED)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchWfSpecByName)
                .start();

        List<WfRunId> results = captor.getValue().get().getResultsList();
        List<WfRunId> matchingResults = results.stream()
                .filter(id -> id.getId().equals(wfRunId.getId()))
                .toList();
        Assertions.assertThat(matchingResults).hasSize(1);
    }

    @Test
    void shouldNotFindCompletedWorkflowsAsRunning() {
        Function<TestExecutionContext, SearchWfRunRequest> searchWfSpecByName =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("searchable-variable-wf")
                        .setStatus(LHStatus.RUNNING)
                        .build();

        SearchResultCaptor<WfRunIdList> captor = SearchResultCaptor.of(WfRunIdList.class);

        WfRunId wfRunId = workflowVerifier
                .prepareRun(searchableVariableWf)
                .waitForStatus(LHStatus.COMPLETED)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchWfSpecByName)
                .start();

        List<WfRunId> results = captor.getValue().get().getResultsList();
        List<WfRunId> matchingResults = results.stream()
                .filter(id -> id.getId().equals(wfRunId.getId()))
                .toList();
        Assertions.assertThat(matchingResults).hasSize(0);
    }

    @Test
    void shouldFindCompletedWorkflowsWithVariableExactlyOnce() {
        String inputVarVal = LHUtil.generateGuid();

        Function<TestExecutionContext, SearchWfRunRequest> searchWfSpecByName =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("searchable-variable-wf")
                        .addVariableFilters(VariableMatch.newBuilder()
                                .setVarName("my-var")
                                .setValue(LHLibUtil.objToVarVal(inputVarVal)))
                        .build();

        SearchResultCaptor<WfRunIdList> captor = SearchResultCaptor.of(WfRunIdList.class);

        WfRunId wfRunId = workflowVerifier
                .prepareRun(searchableVariableWf, Arg.of("my-var", inputVarVal))
                .waitForStatus(LHStatus.COMPLETED)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchWfSpecByName)
                .start();

        List<WfRunId> results = captor.getValue().get().getResultsList();
        List<WfRunId> matchingResults = results.stream()
                .filter(id -> id.getId().equals(wfRunId.getId()))
                .toList();
        Assertions.assertThat(matchingResults).hasSize(1);
    }

    @Test
    void shouldNotFindWorkflowWithWrongVariableMatch() {
        String inputVarVal = LHUtil.generateGuid();

        Function<TestExecutionContext, SearchWfRunRequest> searchWfSpecByName =
                context -> SearchWfRunRequest.newBuilder()
                        .setWfSpecName("searchable-variable-wf")
                        .setStatus(LHStatus.COMPLETED)
                        .addVariableFilters(VariableMatch.newBuilder()
                                .setVarName("my-var")
                                .setValue(LHLibUtil.objToVarVal("not-the-real-input")))
                        .build();

        SearchResultCaptor<WfRunIdList> captor = SearchResultCaptor.of(WfRunIdList.class);

        WfRunId wfRunId = workflowVerifier
                .prepareRun(searchableVariableWf, Arg.of("my-var", inputVarVal))
                .waitForStatus(LHStatus.COMPLETED)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchWfSpecByName)
                .start();

        List<WfRunId> results = captor.getValue().get().getResultsList();
        List<WfRunId> matchingResults = results.stream()
                .filter(id -> id.getId().equals(wfRunId.getId()))
                .toList();
        Assertions.assertThat(matchingResults).hasSize(0);
    }

    @Test
    void shouldFindVariableUsingSearchVariable() {
        String inputVarVal = LHUtil.generateGuid();

        Function<TestExecutionContext, SearchVariableRequest> searchWfSpecByName =
                context -> SearchVariableRequest.newBuilder()
                        .setWfSpecName("searchable-variable-wf")
                        .setVarName("my-var")
                        .setValue(LHLibUtil.objToVarVal("not-the-real-input"))
                        .build();

        SearchResultCaptor<VariableIdList> captor = SearchResultCaptor.of(VariableIdList.class);

        WfRunId wfRunId = workflowVerifier
                .prepareRun(searchableVariableWf, Arg.of("my-var", inputVarVal))
                .waitForStatus(LHStatus.COMPLETED)
                .doSearch(SearchVariableRequest.class, captor.capture(), searchWfSpecByName)
                .start();

        List<VariableId> results = captor.getValue().get().getResultsList();

        Assertions.assertThat(results.stream()
                        .filter(id -> id.getWfRunId().getId().equals(wfRunId.getId()))
                        .toList())
                .hasSize(0);
    }

    @LHWorkflow("complex-workflow")
    public Workflow getEqualsWorkflowImpl() {
        return new WorkflowImpl("complex-workflow", thread -> {
            thread.waitForEvent("external-event");
            thread.execute("my-task");
        });
    }

    @LHWorkflow("searchable-variable-wf")
    public Workflow getSearchableVariableWf() {
        return Workflow.newWorkflow("searchable-variable-wf", wf -> {
            wf.addVariable("my-var", VariableType.STR).searchable();
        });
    }

    @LHTaskMethod("my-task")
    public void myTask() {
        System.out.println("Hello from my task");
    }
}
