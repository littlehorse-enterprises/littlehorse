package e2e;

import static org.assertj.core.api.Assertions.*;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
class StructOneOfTest {
    private LittleHorseBlockingStub client;

    @Test
    void validatesIngressAndFinalMutationResultsWithRollback() {
        String name = "oneof-" + UUID.randomUUID();
        StructDefId id = putPaymentDef(name);
        var input = VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStructDefId(id)
                        .setStruct(InlineStruct.newBuilder().putFields("card", stringField("old"))))
                .build();
        VariableMutation clearCard = mutation("card", VariableValue.getDefaultInstance());
        VariableMutation setBank =
                mutation("bank", VariableValue.newBuilder().setStr("new").build());
        record Case(String suffix, List<VariableMutation> mutations, LHStatus status, String storedField) {}
        for (Case c : List.of(
                new Case("-switch", List.of(clearCard, setBank), LHStatus.COMPLETED, "bank"),
                // Only the edge's final value must be valid, so order doesn't matter.
                new Case("-reverse", List.of(setBank, clearCard), LHStatus.COMPLETED, "bank"),
                new Case("-conflict", List.of(setBank), LHStatus.ERROR, "card"))) {
            String wfName = name + c.suffix();
            client.putWfSpec(wfSpec(wfName, id, c.mutations()));
            var request = RunWfRequest.newBuilder()
                    .setWfSpecName(wfName)
                    .putVariables("payment", input)
                    .build();
            WfRun run = Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .ignoreExceptionsMatching(io.littlehorse.test.exception.LHTestExceptionUtil::isNotFoundException)
                    .until(() -> client.runWf(request), result -> true);
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(
                            client.getWfRun(run.getId()).getStatus())
                    .isEqualTo(c.status()));
            var stored = client.getVariable(VariableId.newBuilder()
                            .setWfRunId(run.getId())
                            .setThreadRunNumber(0)
                            .setName("payment")
                            .build())
                    .getValue()
                    .getStruct()
                    .getStruct();
            assertThat(stored.getFieldsMap().keySet()).containsExactly(c.storedField());
            if (c.status() == LHStatus.ERROR) {
                assertThat(client.getNodeRun(NodeRunId.newBuilder()
                                        .setWfRunId(run.getId())
                                        .setThreadRunNumber(0)
                                        .setPosition(0)
                                        .build())
                                .getFailuresList()
                                .toString())
                        .contains("VAR_SUB_ERROR");
            }
            var invalid = input.toBuilder()
                    .setStruct(input.getStruct().toBuilder()
                            .setStruct(
                                    input.getStruct().getStruct().toBuilder().putFields("bank", stringField("extra"))))
                    .build();
            assertThatThrownBy(() -> client.runWf(
                            request.toBuilder().putVariables("payment", invalid).build()))
                    .isInstanceOf(StatusRuntimeException.class);
        }
    }

    @Test
    void rejectsStructBuilderWithMultipleSelections() {
        String name = "oneof-builder-" + UUID.randomUUID();
        StructDefId id = putPaymentDef(name);
        InlineStructFieldValue str = InlineStructFieldValue.newBuilder()
                .setSimpleValue(VariableAssignment.newBuilder()
                        .setLiteralValue(VariableValue.newBuilder().setStr("x")))
                .build();
        VariableMutation build = VariableMutation.newBuilder()
                .setLhsName("payment")
                .setOperation(VariableMutationType.ASSIGN)
                .setRhsAssignment(VariableAssignment.newBuilder()
                        .setStructBuilder(StructBuilder.newBuilder()
                                .setStructDefId(id)
                                .setValue(InlineStructBuilder.newBuilder()
                                        .putFields("card", str)
                                        .putFields("bank", str))))
                .build();
        assertThatThrownBy(() -> client.putWfSpec(wfSpec(name, id, List.of(build))))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("multiple selections");
    }

    private StructDefId putPaymentDef(String name) {
        StructFieldDef alternative = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setOneofGroup("method")
                .build();
        return client.putStructDef(PutStructDefRequest.newBuilder()
                        .setName(name)
                        .setStructDef(InlineStructDef.newBuilder()
                                .putFields("card", alternative)
                                .putFields("bank", alternative))
                        .build())
                .getId();
    }

    // Builds a WfSpec with a Struct variable "payment" whose entrypoint edge applies the mutations.
    private static PutWfSpecRequest wfSpec(String wfName, StructDefId id, List<VariableMutation> mutations) {
        PutWfSpecRequest.Builder wf =
                Workflow.newWorkflow(wfName, thread -> thread.declareStr("payment"), new LHConfig())
                        .compileWorkflow()
                        .toBuilder();
        ThreadSpec.Builder thread = wf.getThreadSpecsOrThrow(wf.getEntrypointThreadName()).toBuilder();
        thread.getVariableDefsBuilder(0)
                .getVarDefBuilder()
                .clearType()
                .setTypeDef(TypeDefinition.newBuilder().setStructDefId(id));
        String entryName = thread.getNodesMap().entrySet().stream()
                .filter(e -> e.getValue().hasEntrypoint())
                .findFirst()
                .orElseThrow()
                .getKey();
        Node.Builder entry = thread.getNodesOrThrow(entryName).toBuilder();
        entry.getOutgoingEdgesBuilder(0).addAllVariableMutations(mutations);
        thread.putNodes(entryName, entry.build());
        return wf.putThreadSpecs(wf.getEntrypointThreadName(), thread.build()).build();
    }

    private static StructField stringField(String value) {
        return StructField.newBuilder()
                .setValue(VariableValue.newBuilder().setStr(value))
                .build();
    }

    private static VariableMutation mutation(String field, VariableValue value) {
        return VariableMutation.newBuilder()
                .setLhsName("payment")
                .setOperation(VariableMutationType.ASSIGN)
                .setLhsLhPath(
                        LHPath.newBuilder().addPath(LHPath.Selector.newBuilder().setKey(field)))
                .setRhsAssignment(VariableAssignment.newBuilder().setLiteralValue(value))
                .build();
    }
}
