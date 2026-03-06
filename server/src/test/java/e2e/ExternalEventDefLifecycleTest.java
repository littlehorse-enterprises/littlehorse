package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.test.LHTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class ExternalEventDefLifecycleTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldRejectExternalEventDefWithNonExistentStructDef() {
        String name = "eed-missing-struct-" + UUID.randomUUID();
        PutExternalEventDefRequest req = PutExternalEventDefRequest.newBuilder()
                .setName(name)
                .setContentType(ReturnType.newBuilder()
                        .setReturnType(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder()
                                        .setName("non-existent-struct-def")
                                        .setVersion(0))))
                .build();

        assertThatThrownBy(() -> client.putExternalEventDef(req))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Refers to non-existent StructDef non-existent-struct-def");
    }

    @Test
    void shouldAcceptExternalEventDefWithExistingStructDef() {
        String structDefName = "eed-struct-" + UUID.randomUUID();
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

        String name = "eed-with-struct-" + UUID.randomUUID();
        PutExternalEventDefRequest req = PutExternalEventDefRequest.newBuilder()
                .setName(name)
                .setContentType(ReturnType.newBuilder()
                        .setReturnType(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder()
                                        .setName(structDefName)
                                        .setVersion(0))))
                .build();

        ExternalEventDef result = client.putExternalEventDef(req);
        assertThat(result.getId().getName()).isEqualTo(name);
    }
}
