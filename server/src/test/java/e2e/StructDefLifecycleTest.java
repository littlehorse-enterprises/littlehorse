package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutStructDefRequest.AllowedStructDefUpdateType;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.test.LHTest;
import org.junit.jupiter.api.Test;

@LHTest
public class StructDefLifecycleTest {
    private LittleHorseBlockingStub client;

    @Test
    void shouldThrowErrorWhenEvolvingStructDefWithNoSchemaUpdatesCompatibilityType() {
        // TODO: Re-implement tests with StructDef builder helpers developed in following PR.
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car")
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.STR))
                                        .build()))
                .build());

        PutStructDefRequest updatedStructDef = PutStructDefRequest.newBuilder()
                .setName("car")
                .setAllowedUpdates(AllowedStructDefUpdateType.NO_SCHEMA_UPDATES)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.STR))
                                        .build())
                        .putFields(
                                "year",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.INT))
                                        .build()))
                .build();

        assertThatThrownBy(() -> client.putStructDef(updatedStructDef))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage(
                        "INVALID_ARGUMENT: StructDef [car] already exists and cannot be updated with the selected compatiblity type NO_SCHEMA_UPDATES.");
    }

    @Test
    void shouldAllowEvolvingStructDefWithFullyCompatibleSchemaUpdates() {
        // TODO: Re-implement tests with StructDef builder helpers developed in following PR.
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car-2")
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.STR))
                                        .build()))
                .build());

        PutStructDefRequest updatedStructDef = PutStructDefRequest.newBuilder()
                .setName("car-2")
                .setAllowedUpdates(AllowedStructDefUpdateType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.STR))
                                        .build())
                        .putFields(
                                "year",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.INT))
                                        .setDefaultValue(
                                                VariableValue.newBuilder().setInt(10))
                                        .build())
                        .putFields(
                                "sold",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.BOOL))
                                        .setDefaultValue(
                                                VariableValue.newBuilder().setBool(false))
                                        .build()))
                .build();

        assertThat(client.putStructDef(updatedStructDef))
                .extracting(item -> item.getId().getVersion())
                .isEqualTo(1);
    }

    @Test
    void shouldThrowErrorWhenPuttingIncompatibleStructDefEvolution() {
        // TODO: Re-implement tests with StructDef builder helpers developed in following PR.
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car-3")
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.STR))
                                        .build()))
                .build());

        PutStructDefRequest updatedStructDef = PutStructDefRequest.newBuilder()
                .setName("car-3")
                .setAllowedUpdates(AllowedStructDefUpdateType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.STR))
                                        .build())
                        .putFields(
                                "year",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.INT))
                                        .build())
                        .putFields(
                                "is-sold",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setType(VariableType.BOOL))
                                        .build()))
                .build();

        assertThatThrownBy(() -> client.putStructDef(updatedStructDef))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("INVALID_ARGUMENT: Incompatible schema evolution on field(s): [year], [is-sold]");
    }
}
