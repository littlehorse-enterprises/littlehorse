package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowEventDef;
import io.littlehorse.test.LHTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class WorkflowEventDefLifecycleTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldRejectWorkflowEventDefWithNonExistentStructDef() {
        String name = "wed-missing-struct-" + UUID.randomUUID();
        PutWorkflowEventDefRequest req = PutWorkflowEventDefRequest.newBuilder()
                .setName(name)
                .setContentType(ReturnType.newBuilder()
                        .setReturnType(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder()
                                        .setName("non-existent-struct-def")
                                        .setVersion(0))))
                .build();

        assertThatThrownBy(() -> client.putWorkflowEventDef(req))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Refers to non-existent StructDef non-existent-struct-def");
    }

    @Test
    void shouldAcceptWorkflowEventDefWithExistingStructDef() {
        String structDefName = "wed-struct-" + UUID.randomUUID();
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName(structDefName)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "field",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build()))
                .build());

        String name = "wed-with-struct-" + UUID.randomUUID();
        PutWorkflowEventDefRequest req = PutWorkflowEventDefRequest.newBuilder()
                .setName(name)
                .setContentType(ReturnType.newBuilder()
                        .setReturnType(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder()
                                        .setName(structDefName)
                                        .setVersion(0))))
                .build();

        WorkflowEventDef result = client.putWorkflowEventDef(req);
        assertThat(result.getId().getName()).isEqualTo(name);
    }
}
