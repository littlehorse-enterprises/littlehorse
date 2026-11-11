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
    void validatesIngressAndOrderedMutationsWithRollback() {
        String name = "oneof-" + UUID.randomUUID();
        StructFieldDef alternative = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setOneofGroup("method")
                .build();
        StructDef definition = client.putStructDef(PutStructDefRequest.newBuilder()
                .setName(name)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields("card", alternative)
                        .putFields("bank", alternative))
                .build());
        var input = VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStructDefId(definition.getId())
                        .setStruct(InlineStruct.newBuilder().putFields("card", stringField("old"))))
                .build();
        for (boolean clearFirst : List.of(true, false)) {
            String wfName = name + (clearFirst ? "-switch" : "-conflict");
            PutWfSpecRequest.Builder wf =
                    Workflow.newWorkflow(wfName, thread -> thread.declareStr("payment"), new LHConfig())
                            .compileWorkflow()
                            .toBuilder();
            ThreadSpec.Builder thread = wf.getThreadSpecsOrThrow(wf.getEntrypointThreadName()).toBuilder();
            thread.getVariableDefsBuilder(0)
                    .getVarDefBuilder()
                    .clearType()
                    .setTypeDef(TypeDefinition.newBuilder().setStructDefId(definition.getId()));
            String entryName = thread.getNodesMap().entrySet().stream()
                    .filter(e -> e.getValue().hasEntrypoint())
                    .findFirst()
                    .orElseThrow()
                    .getKey();
            Node.Builder entry = thread.getNodesOrThrow(entryName).toBuilder();
            Edge.Builder edge = entry.getOutgoingEdgesBuilder(0);
            if (clearFirst) edge.addVariableMutations(mutation("card", VariableValue.getDefaultInstance()));
            edge.addVariableMutations(
                    mutation("bank", VariableValue.newBuilder().setStr("new").build()));
            // A later clear must not make an earlier conflicting selection acceptable.
            if (!clearFirst) edge.addVariableMutations(mutation("card", VariableValue.getDefaultInstance()));
            thread.putNodes(entryName, entry.build());
            client.putWfSpec(wf.putThreadSpecs(wf.getEntrypointThreadName(), thread.build())
                    .build());
            var request = RunWfRequest.newBuilder()
                    .setWfSpecName(wfName)
                    .putVariables("payment", input)
                    .build();
            WfRun run = Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .ignoreExceptionsMatching(io.littlehorse.test.exception.LHTestExceptionUtil::isNotFoundException)
                    .until(() -> client.runWf(request), result -> true);
            LHStatus expected = clearFirst ? LHStatus.COMPLETED : LHStatus.ERROR;
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(
                            client.getWfRun(run.getId()).getStatus())
                    .isEqualTo(expected));
            var stored = client.getVariable(VariableId.newBuilder()
                            .setWfRunId(run.getId())
                            .setThreadRunNumber(0)
                            .setName("payment")
                            .build())
                    .getValue()
                    .getStruct()
                    .getStruct();
            assertThat(stored.getFieldsMap().keySet()).containsExactly(clearFirst ? "bank" : "card");
            if (!clearFirst) {
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
