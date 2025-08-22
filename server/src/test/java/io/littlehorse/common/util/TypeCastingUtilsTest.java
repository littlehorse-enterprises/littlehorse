package io.littlehorse.common.util;

import static org.junit.jupiter.api.Assertions.*;

import io.littlehorse.common.exceptions.validation.InvalidMutationException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class TypeCastingUtilsTest {

    private final ExecutionContext context = Mockito.mock(ExecutionContext.class);

    static Stream<Arguments> automaticCastingCases() {
        return Stream.of(
                Arguments.of(VariableType.INT, VariableType.INT, true),
                Arguments.of(VariableType.STR, VariableType.STR, true),
                Arguments.of(VariableType.DOUBLE, VariableType.DOUBLE, true),
                Arguments.of(VariableType.BOOL, VariableType.BOOL, true),
                Arguments.of(VariableType.INT, VariableType.STR, true),
                Arguments.of(VariableType.DOUBLE, VariableType.STR, true),
                Arguments.of(VariableType.BOOL, VariableType.STR, true),
                Arguments.of(VariableType.BYTES, VariableType.STR, true),
                Arguments.of(VariableType.WF_RUN_ID, VariableType.STR, true),
                Arguments.of(VariableType.INT, VariableType.DOUBLE, true),
                Arguments.of(VariableType.STR, VariableType.INT, false),
                Arguments.of(VariableType.STR, VariableType.DOUBLE, false),
                Arguments.of(VariableType.STR, VariableType.BOOL, false),
                Arguments.of(VariableType.DOUBLE, VariableType.INT, false),
                Arguments.of(VariableType.DOUBLE, VariableType.BOOL, false),
                Arguments.of(VariableType.INT, VariableType.BOOL, false));
    }

    static Stream<Arguments> manualCastingCases() {
        return Stream.of(
                // STR → other primitives (manual)
                Arguments.of(VariableType.STR, VariableType.INT, true),
                Arguments.of(VariableType.STR, VariableType.DOUBLE, true),
                Arguments.of(VariableType.STR, VariableType.BOOL, true),
                Arguments.of(VariableType.STR, VariableType.BYTES, true),
                Arguments.of(VariableType.STR, VariableType.WF_RUN_ID, true),

                // DOUBLE → INT (manual)
                Arguments.of(VariableType.DOUBLE, VariableType.INT, true),

                // Cases that don't require manual casting
                Arguments.of(VariableType.INT, VariableType.STR, false),
                Arguments.of(VariableType.INT, VariableType.DOUBLE, false),
                Arguments.of(VariableType.INT, VariableType.INT, false),

                // Unsupported cases
                Arguments.of(VariableType.DOUBLE, VariableType.BOOL, false),
                Arguments.of(VariableType.INT, VariableType.BOOL, false));
    }

    static Stream<Arguments> supportedCastingCases() {
        return Stream.of(
                Arguments.of(VariableType.INT, VariableType.INT, true),
                Arguments.of(VariableType.STR, VariableType.STR, true),
                Arguments.of(VariableType.INT, VariableType.STR, true),
                Arguments.of(VariableType.DOUBLE, VariableType.STR, true),
                Arguments.of(VariableType.BOOL, VariableType.STR, true),
                Arguments.of(VariableType.INT, VariableType.DOUBLE, true),
                Arguments.of(VariableType.STR, VariableType.INT, true),
                Arguments.of(VariableType.STR, VariableType.DOUBLE, true),
                Arguments.of(VariableType.STR, VariableType.BOOL, true),
                Arguments.of(VariableType.STR, VariableType.BYTES, true),
                Arguments.of(VariableType.STR, VariableType.WF_RUN_ID, true),
                Arguments.of(VariableType.DOUBLE, VariableType.INT, true),
                Arguments.of(VariableType.DOUBLE, VariableType.BOOL, false),
                Arguments.of(VariableType.INT, VariableType.BOOL, false),
                Arguments.of(VariableType.BOOL, VariableType.INT, false),
                Arguments.of(VariableType.BOOL, VariableType.DOUBLE, false));
    }

    @Nested
    class CanCastToTest {

        @ParameterizedTest(name = "{0} → {1} should return {2}")
        @MethodSource("io.littlehorse.common.util.TypeCastingUtilsTest#supportedCastingCases")
        void testCanCastTo(VariableType sourceType, VariableType targetType, boolean expected) {
            boolean result = TypeCastingUtils.canCastTo(sourceType, targetType);
            assertEquals(expected, result);
        }
    }

    @Nested
    class CanAssignAutomaticallyTest {

        @ParameterizedTest(name = "{0} → {1} automatic = {2}")
        @MethodSource("io.littlehorse.common.util.TypeCastingUtilsTest#automaticCastingCases")
        void testCanAssignAutomatically(VariableType sourceType, VariableType targetType, boolean expected) {
            boolean result = TypeCastingUtils.canAssignWithoutCast(sourceType, targetType);
            assertEquals(expected, result);
        }
    }

    @Nested
    class RequiresManualCastTest {

        @ParameterizedTest(name = "{0} → {1} manual = {2}")
        @MethodSource("io.littlehorse.common.util.TypeCastingUtilsTest#manualCastingCases")
        void testRequiresManualCast(VariableType sourceType, VariableType targetType, boolean expected) {
            boolean result = TypeCastingUtils.requiresManualCast(sourceType, targetType);
            assertEquals(expected, result);
        }
    }

    @Nested
    class CanAssignWithoutCastTest {

        @Test
        void shouldAllowAutomaticCasting() {
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.INT, VariableType.STR));
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.INT, VariableType.DOUBLE));
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.DOUBLE, VariableType.STR));
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.BOOL, VariableType.STR));
        }

        @Test
        void shouldRejectManualCasting() {
            assertFalse(TypeCastingUtils.canAssignWithoutCast(VariableType.STR, VariableType.INT));
            assertFalse(TypeCastingUtils.canAssignWithoutCast(VariableType.STR, VariableType.DOUBLE));
            assertFalse(TypeCastingUtils.canAssignWithoutCast(VariableType.STR, VariableType.BOOL));
            assertFalse(TypeCastingUtils.canAssignWithoutCast(VariableType.DOUBLE, VariableType.INT));
        }

        @Test
        void shouldAllowSameType() {
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.INT, VariableType.INT));
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.STR, VariableType.STR));
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.DOUBLE, VariableType.DOUBLE));
            assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.BOOL, VariableType.BOOL));
        }
    }

    @Nested
    class ValidateAssignmentTest {

        @Test
        void shouldPassForSameType() {
            assertDoesNotThrow(() -> TypeCastingUtils.validateTypeCompatibility(VariableType.INT, VariableType.INT));
        }

        @Test
        void shouldPassForAutomaticCastingWithoutExplicitCast() {
            assertDoesNotThrow(() -> TypeCastingUtils.validateTypeCompatibility(VariableType.INT, VariableType.DOUBLE));
        }

        @Test
        void shouldPassForManualCastingWithExplicitCast() {
            // For explicit casts, validateTypeCompatibility should be called only to check if the cast is possible
            assertDoesNotThrow(() -> TypeCastingUtils.validateTypeCompatibility(VariableType.STR, VariableType.INT));
            assertDoesNotThrow(() -> TypeCastingUtils.validateTypeCompatibility(VariableType.STR, VariableType.DOUBLE));
            assertDoesNotThrow(() -> TypeCastingUtils.validateTypeCompatibility(VariableType.DOUBLE, VariableType.INT));
        }

        @Test
        void shouldFailForManualCastingWithoutExplicitCast() {
            // Now, explicit cast logic is handled at the call site, so this test is not applicable for
            // validateTypeCompatibility
            // Instead, test canAssignWithoutCast for false, and canCastTo for true
            assertFalse(TypeCastingUtils.canAssignWithoutCast(VariableType.STR, VariableType.INT));
            assertTrue(TypeCastingUtils.canCastTo(VariableType.STR, VariableType.INT));
        }

        @Test
        void shouldFailForUnsupportedCasting() {
            InvalidMutationException exception = assertThrows(
                    InvalidMutationException.class,
                    () -> TypeCastingUtils.validateTypeCompatibility(VariableType.DOUBLE, VariableType.BOOL));
            assertTrue(exception.getMessage().contains("Cannot cast from DOUBLE to BOOL"));
            assertTrue(exception.getMessage().contains("not supported"));
        }
    }

    void shouldThrowIllegalArgumentExceptionForUnsupportedCasting() {
        VariableValueModel doubleValue = VariableValueModel.fromProto(
                io.littlehorse.sdk.common.proto.VariableValue.newBuilder()
                        .setDouble(123.45)
                        .build(),
                context);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> TypeCastingUtils.applyCast(doubleValue, VariableType.BOOL));

        assertTrue(exception.getMessage().contains("Casting from DOUBLE to BOOL is not supported"));
    }

    @Test
    void shouldThrowFriendlyMessageForInvalidStrToInt() {
        VariableValueModel strValue = VariableValueModel.fromProto(
                io.littlehorse.sdk.common.proto.VariableValue.newBuilder()
                        .setStr("not-a-number")
                        .build(),
                context);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> TypeCastingUtils.applyCast(strValue, VariableType.INT));

        assertTrue(exception.getMessage().contains("Cannot parse 'not-a-number' as INT"));
    }

    @Test
    void shouldThrowFriendlyMessageForInvalidStrToBool() {
        VariableValueModel strValue = VariableValueModel.fromProto(
                io.littlehorse.sdk.common.proto.VariableValue.newBuilder()
                        .setStr("maybe")
                        .build(),
                context);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> TypeCastingUtils.applyCast(strValue, VariableType.BOOL));

        assertTrue(exception.getMessage().contains("Cannot parse 'maybe' as BOOL"));
    }
}

@Nested
class EdgeCasesTest {

    @Test
    void shouldValidateComplexCastingChainsLogically() {
        assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.INT, VariableType.STR));
        assertTrue(TypeCastingUtils.requiresManualCast(VariableType.STR, VariableType.DOUBLE));

        assertTrue(TypeCastingUtils.canAssignWithoutCast(VariableType.DOUBLE, VariableType.STR));
        assertTrue(TypeCastingUtils.requiresManualCast(VariableType.STR, VariableType.INT));
    }
}
