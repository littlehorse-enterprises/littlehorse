package io.littlehorse.common.model.getable.global.wfspec;

import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

/**
 * Small utility for applying expected TypeDefinitions at ingress points.
 *
 * Responsibilities:
 * - If an expected type is present and is an inline-array, set the provided
 *   VariableValueModel's Array.elementType to the expected element type when
 *   the value is an ARRAY. This mirrors server-side inference and avoids
 *   relying on client-supplied element_type.
 * - Validate the value against the expected type and throw an LHApiException
 *   with Status.INVALID_ARGUMENT on mismatch.
 */
public class IngressTypeUtils {

    public static void applyExpectedTypeAndValidate(
            Optional<TypeDefinitionModel> expected, VariableValueModel value, ReadOnlyMetadataManager metadataManager) {
        if (expected == null || expected.isEmpty()) return;

        TypeDefinitionModel typeDef = expected.get();

        // If the provided value is null, fail early to avoid any dereference.
        if (value == null) {
            throw new RuntimeException(String.format(
                    "VariableValue provided is null"
                            + " but expected type is %s. All VariableValues must be non-null, with ValueCase.VALUE_NOT_SET used to represent null values.",
                    typeDef));
        }

        // If expected is inline array and value is array, set authoritative element type
        if (typeDef.getDefinedTypeCase() == DefinedTypeCase.INLINE_ARRAY_DEF
                && value.getValueType() == VariableValue.ValueCase.ARRAY) {
            value.getArray().setElementType(typeDef.getInlineArrayDef().getArrayType());
        }

        // Validate using existing TypeDefinitionModel logic. It will perform
        // per-item checks for inline arrays as needed.
        if (!typeDef.isCompatibleWith(value, metadataManager)) {
            String actualType = String.valueOf(value.getTypeDefinition());
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    String.format(
                            "Provided value of type %s is incompatible with expected type %s", actualType, typeDef));
        }
    }
}
