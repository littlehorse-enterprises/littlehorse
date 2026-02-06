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
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.internal.TestExecutionContext;
import io.littlehorse.test.internal.step.SearchResultCaptor;
import java.util.List;
import java.util.UUID;
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
        WfRunId id = workflowVerifier
                .prepareRun(complexWorkflow)
                .waitForStatus(RUNNING)
                .doSearch(SearchWfRunRequest.class, wfRunIdListCaptor.capture(), searchByNameAndStatusRunning)
                .thenSendExternalEventWithContent("external-event", "{}")
                .waitForStatus(COMPLETED)
                .doSearch(SearchWfRunRequest.class, wfRunIdListCaptor.capture(), searchByNameAndStatusCompleted)
                .start();
        WfRunIdList runningWorkflowIds = wfRunIdListCaptor.getValue().get();
        WfRunIdList completedWorkflowIds = wfRunIdListCaptor.getValue().get();
        Assertions.assertThat(completedWorkflowIds.getResultsList().stream()
                        .filter(foundId -> foundId.getId().equals(id.getId()))
                        .toList())
                .hasSize(1);
        Assertions.assertThat(runningWorkflowIds.getResultsList().stream()
                        .filter(foundId -> foundId.getId().equals(id.getId()))
                        .toList())
                .hasSize(1);
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

    @Test
    @Order(2)
    void shouldSearchChildWorkflowByParentId() {

        WfRunId parentId = workflowVerifier
                .prepareRun(getSearchableVariableWf())
                .waitForStatus(COMPLETED)
                .start();

        SearchResultCaptor<WfRunIdList> captor = SearchResultCaptor.of(WfRunIdList.class);
        WfRunId childId = WfRunId.newBuilder()
                .setId("child-id")
                .setParentWfRunId(parentId)
                .build();
        Function<TestExecutionContext, SearchWfRunRequest> searchByParentId = context ->
                SearchWfRunRequest.newBuilder().setParentWfRunId(parentId).build();
        Function<TestExecutionContext, SearchWfRunRequest> searchByParentIdAndWfName =
                context -> SearchWfRunRequest.newBuilder()
                        .setParentWfRunId(parentId)
                        .setWfSpecName("child-workflow")
                        .build();
        Function<TestExecutionContext, SearchWfRunRequest> searchByParentIdWfNameAndStatus =
                context -> SearchWfRunRequest.newBuilder()
                        .setParentWfRunId(parentId)
                        .setWfSpecName("child-workflow")
                        .setStatus(COMPLETED)
                        .build();
        Function<TestExecutionContext, SearchWfRunRequest> searchByParentIdAndStatus =
                context -> SearchWfRunRequest.newBuilder()
                        .setParentWfRunId(parentId)
                        .setStatus(COMPLETED)
                        .build();

        workflowVerifier
                .prepareRun(getChildWorkflow())
                .waitForStatus(COMPLETED)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchByParentId)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchByParentIdAndWfName)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchByParentIdWfNameAndStatus)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchByParentIdAndStatus)
                .start(childId);

        WfRunIdList byIdResult = captor.getValue().get();
        WfRunIdList byIdAndNameResult = captor.getValue().get();
        WfRunIdList byIdAndNameAndStatusResult = captor.getValue().get();
        WfRunIdList byIdAndStatusResult = captor.getValue().get();
        Assertions.assertThat(byIdResult.getResultsList())
                .hasSize(1)
                .allSatisfy(foundId -> Assertions.assertThat(foundId.getId()).isEqualTo(childId.getId()));
        Assertions.assertThat(byIdAndNameResult.getResultsList()).hasSize(1).allSatisfy(foundId -> {
            Assertions.assertThat(foundId.getId()).isEqualTo(childId.getId());
        });
        Assertions.assertThat(byIdAndNameAndStatusResult.getResultsList())
                .hasSize(1)
                .allSatisfy(foundId -> {
                    Assertions.assertThat(foundId.getId()).isEqualTo(childId.getId());
                    Assertions.assertThat(foundId.getParentWfRunId().getId()).isEqualTo(parentId.getId());
                });
        Assertions.assertThat(byIdAndStatusResult.getResultsList()).hasSize(1).allSatisfy(foundId -> {
            Assertions.assertThat(foundId.getId()).isEqualTo(childId.getId());
            Assertions.assertThat(foundId.getParentWfRunId().getId()).isEqualTo(parentId.getId());
        });
    }

    @Test
    @Order(2)
    void shouldSearchGrandChildWorkflowWithLongIDs() {
        WfRunId parentId =
                WfRunId.newBuilder().setId(UUID.randomUUID().toString()).build();
        workflowVerifier
                .prepareRun(getSearchableVariableWf())
                .waitForStatus(COMPLETED)
                .start(parentId);
        WfRunId childId = WfRunId.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setParentWfRunId(parentId)
                .build();
        workflowVerifier.prepareRun(getChildWorkflow()).waitForStatus(COMPLETED).start(childId);

        WfRunId grandChildId = WfRunId.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setParentWfRunId(childId)
                .build();
        SearchResultCaptor<WfRunIdList> captor = SearchResultCaptor.of(WfRunIdList.class);
        Function<TestExecutionContext, SearchWfRunRequest> searchByChildId = context ->
                SearchWfRunRequest.newBuilder().setParentWfRunId(childId).build();

        workflowVerifier
                .prepareRun(getGrandChildWorkflow())
                .waitForStatus(COMPLETED)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchByChildId)
                .start(grandChildId);

        WfRunIdList result = captor.getValue().get();
        Assertions.assertThat(result.getResultsList())
                .hasSize(1)
                .allSatisfy(foundId -> Assertions.assertThat(foundId.getId()).isEqualTo(grandChildId.getId()));
    }

    @Test
    @Order(2)
    void searchByParentIdShouldOnlyReturnChildNoGrandChild() {
        WfRunId parentId = workflowVerifier
                .prepareRun(getSearchableVariableWf())
                .waitForStatus(COMPLETED)
                .start();
        WfRunId childId = WfRunId.newBuilder()
                .setId("child-id")
                .setParentWfRunId(parentId)
                .build();
        workflowVerifier.prepareRun(getChildWorkflow()).waitForStatus(COMPLETED).start(childId);

        SearchResultCaptor<WfRunIdList> captor = SearchResultCaptor.of(WfRunIdList.class);
        WfRunId grandChildId = WfRunId.newBuilder()
                .setId("grand-child-id")
                .setParentWfRunId(childId)
                .build();
        Function<TestExecutionContext, SearchWfRunRequest> searchByParentId = context ->
                SearchWfRunRequest.newBuilder().setParentWfRunId(parentId).build();
        workflowVerifier
                .prepareRun(getGrandChildWorkflow())
                .waitForStatus(COMPLETED)
                .doSearch(SearchWfRunRequest.class, captor.capture(), searchByParentId)
                .start(grandChildId);

        WfRunIdList result = captor.getValue().get();
        Assertions.assertThat(result.getResultsList())
                .hasSize(1)
                .allSatisfy(foundId -> Assertions.assertThat(foundId.getId()).isEqualTo(childId.getId()));
    }

    @LHWorkflow("complex-workflow")
    public Workflow getEqualsWorkflowImpl() {
        return new WorkflowImpl("complex-workflow", thread -> {
            thread.waitForEvent("external-event");
            thread.execute("my-task-2");
        });
    }

    @LHWorkflow("searchable-variable-wf")
    public Workflow getSearchableVariableWf() {
        return Workflow.newWorkflow("searchable-variable-wf", wf -> {
            wf.addVariable("my-var", VariableType.STR).searchable();
        });
    }

    @LHWorkflow("child-workflow")
    public Workflow getChildWorkflow() {
        var workflow = Workflow.newWorkflow("child-workflow", wf -> {
            wf.addVariable("my-var", VariableType.STR).searchable();
        });
        workflow.setParent("searchable-variable-wf");
        return workflow;
    }

    @LHWorkflow("grand-child-workflow")
    public Workflow getGrandChildWorkflow() {
        var workflow = Workflow.newWorkflow("grand-child-workflow", wf -> {
            wf.addVariable("my-var", VariableType.STR).searchable();
        });
        workflow.setParent("child-workflow");
        return workflow;
    }

    @LHTaskMethod("my-task-2")
    public void myTask() {
        System.out.println("Hello from my task");
    }
}
