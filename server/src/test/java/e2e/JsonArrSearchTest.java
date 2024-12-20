package e2e;

import static org.junit.Assert.assertEquals;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.VariableIdList;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.internal.TestExecutionContext;
import io.littlehorse.test.internal.step.SearchResultCaptor;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * Tests that we can index and search JSON_ARR variables properly.
 */
@LHTest
public class JsonArrSearchTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("json-arr-idx")
    private Workflow jsonArrTest;

    @Test
    void shouldFindVariable() {
        final SearchResultCaptor<VariableIdList> asdfVariableList = SearchResultCaptor.of(VariableIdList.class);
        final Function<TestExecutionContext, SearchVariableRequest> asdfSupplier = (ctx) -> {
            return SearchVariableRequest.newBuilder()
                    .setWfSpecName("json-arr-idx")
                    .setValue(LHLibUtil.objToVarVal("asdf"))
                    .setVarName("json-var")
                    .build();
        };

        final SearchResultCaptor<VariableIdList> fdsaVariableList = SearchResultCaptor.of(VariableIdList.class);
        final Function<TestExecutionContext, SearchVariableRequest> fdsaSupplier = (ctx) -> {
            return SearchVariableRequest.newBuilder()
                    .setWfSpecName("json-arr-idx")
                    .setValue(LHLibUtil.objToVarVal("fdsa"))
                    .setVarName("json-var")
                    .build();
        };

        final SearchResultCaptor<VariableIdList> emptyVariableList = SearchResultCaptor.of(VariableIdList.class);
        final Function<TestExecutionContext, SearchVariableRequest> emptySupplier = (ctx) -> {
            return SearchVariableRequest.newBuilder()
                    .setWfSpecName("json-arr-idx")
                    .setValue(LHLibUtil.objToVarVal("not-a-real-value"))
                    .setVarName("json-var")
                    .build();
        };

        WfRunId id = verifier.prepareRun(jsonArrTest, Arg.of("json-var", List.of("asdf", "fdsa", "asdf")))
                .doSearch(SearchVariableRequest.class, asdfVariableList.capture(), asdfSupplier)
                .doSearch(SearchVariableRequest.class, fdsaVariableList.capture(), fdsaSupplier)
                .doSearch(SearchVariableRequest.class, emptyVariableList.capture(), emptySupplier)
                .start();

        VariableIdList asdfVars = asdfVariableList.getValue().get();
        int matches = asdfVars.getResultsList().stream()
                .filter(variableId -> variableId.getWfRunId().getId().equals(id.getId()))
                .toList()
                .size();
        assertEquals(matches, 1);

        VariableIdList fdsaVars = fdsaVariableList.getValue().get();
        matches = fdsaVars.getResultsList().stream()
                .filter(variableId -> variableId.getWfRunId().getId().equals(id.getId()))
                .toList()
                .size();
        assertEquals(matches, 1);

        VariableIdList emptyVars = emptyVariableList.getValue().get();
        matches = emptyVars.getResultsList().stream()
                .filter(variableId -> variableId.getWfRunId().getId().equals(id.getId()))
                .toList()
                .size();
        assertEquals(matches, 0);
    }

    @LHWorkflow("json-arr-idx")
    public Workflow buildWorkflow() {
        return Workflow.newWorkflow("json-arr-idx", wf -> {
            wf.addVariable("json-var", VariableType.JSON_ARR).searchable();
            // Don't do anything with it
        });
    }
}
