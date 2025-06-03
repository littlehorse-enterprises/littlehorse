package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@LHTest
public class StructDefLifecycleTest {
    private LittleHorseBlockingStub client;

    @Nested
    class NoSchemaUpdatesEvolutionTest {
        @Test
        void shouldThrowErrorWhenPuttingNewField() {
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
                    .setAllowedUpdates(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
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
                    .hasMessage("INVALID_ARGUMENT: Incompatible schema evolution on field(s): [year]");
        }

        @Test
        void shouldValidateInvalidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-4")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-4");

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-4"))
                            .setCompatibilityType(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setType(VariableType.STR))
                                                    .build())
                                    .putFields(
                                            "year",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setType(VariableType.INT))
                                                    .setDefaultValue(VariableValue.newBuilder()
                                                            .setInt(1970))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isFalse();
        }

        @Test
        void shouldValidateValidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-4")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-4");

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-4"))
                            .setCompatibilityType(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setType(VariableType.STR))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isTrue();
        }
    }

    @Nested
    class FullyCompatibleSchemaUpdatesEvolutionTest {
        @Test
        void shouldAllowCompatibleStructDefEvolution() {
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
                    .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
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
        void shouldThrowErrorWhenPuttingIncompatibleStructDefEvolutions() {
            // TODO: Re-implement tests with StructDef builder helpers developed in following PR.
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-3")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setType(VariableType.STR))
                                            .build())
                            .putFields(
                                    "is-sold",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setType(VariableType.BOOL))
                                            .build()))
                    .build());

            PutStructDefRequest updatedStructDef = PutStructDefRequest.newBuilder()
                    .setName("car-3")
                    .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setType(VariableType.INT))
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
                    .hasMessage("INVALID_ARGUMENT: Incompatible schema evolution on field(s): [year, model, is-sold]");
        }

        @Test
        void shouldValidateValidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-5")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-5");

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-5"))
                            .setCompatibilityType(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setType(VariableType.STR))
                                                    .build())
                                    .putFields(
                                            "year",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setType(VariableType.INT))
                                                    .setDefaultValue(VariableValue.newBuilder()
                                                            .setInt(1970))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isTrue();
        }

        @Test
        void shouldValidateInvalidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-6")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-6");

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-6"))
                            .setCompatibilityType(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setType(VariableType.STR))
                                                    .build())
                                    .putFields(
                                            "year",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setType(VariableType.INT))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isFalse();
        }
    }

    private void waitForStructDef(String name) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    client.getStructDef(StructDefId.newBuilder().setName(name).build());
                    return true;
                });
    }
}
