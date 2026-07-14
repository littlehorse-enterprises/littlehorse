package io.littlehorse.common.model.getable.global.wfspec;

import io.littlehorse.common.exceptions.validation.TypeValidationException;
import io.littlehorse.common.model.getable.core.variable.StructFieldModel;
import io.littlehorse.common.model.getable.core.variable.StructModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.Map;
import java.util.Optional;

/**
 * Small utility for applying expected TypeDefinitions at ingress points.
 *
 * Responsibilities:
 * - If an expected type is present and is an inline-array, set the provided
 *   VariableValueModel's Array.elementType to the expected element type when
 *   the value is an ARRAY. Nested arrays, map values, and struct fields are
 *   pinned recursively so that inner containers also carry their authoritative
 *   types. This mirrors server-side inference and avoids relying on
 *   client-supplied element_type.
 * - Validate the value against the expected type and throw an LHApiException
 *   with Status.INVALID_ARGUMENT on mismatch.
 */
public class IngressTypeUtils {

    public static void applyExpectedTypeAndValidate(
            Optional<TypeDefinitionModel> expected, VariableValueModel value, ReadOnlyMetadataManager metadataManager)
            throws TypeValidationException {
        if (expected == null || expected.isEmpty()) return;

        TypeDefinitionModel typeDef = expected.get();

        // If the provided value is null, fail early to avoid any dereference.
        if (value == null) {
            throw new RuntimeException(String.format(
                    "VariableValue provided is null"
                            + " but expected type is %s. All VariableValues must be non-null, with ValueCase.VALUE_NOT_SET used to represent null values.",
                    typeDef));
        }

        // Set authoritative element/map/struct-field types on the value (recursively
        // for nested arrays, map values, and struct fields) before validation.
        applyExpectedType(typeDef, value, metadataManager);

        // Validate using existing TypeDefinitionModel logic. It will perform
        // per-item checks for inline arrays as needed. Map domain errors to API errors.
        typeDef.validateCompatibility(value, metadataManager);
    }

    /**
     * Recursively pins the authoritative element type onto ARRAY values, the map type onto MAP
     * values, and each field's type onto STRUCT values, descending into nested arrays, map entries,
     * and struct fields so that inner containers also carry their authoritative types rather than
     * relying on client-supplied ones.
     */
    private static void applyExpectedType(
            TypeDefinitionModel typeDef, VariableValueModel value, ReadOnlyMetadataManager metadataManager) {
        if (typeDef == null || value == null) return;

        switch (typeDef.getDefinedTypeCase()) {
            case INLINE_ARRAY_DEF:
                if (value.getValueType() == VariableValue.ValueCase.ARRAY) {
                    TypeDefinitionModel elementType =
                            typeDef.getInlineArrayDef().getArrayType();
                    value.getArray().setElementType(elementType);
                    if (value.getArray().getItems() != null) {
                        for (VariableValueModel item : value.getArray().getItems()) {
                            applyExpectedType(elementType, item, metadataManager);
                        }
                    }
                }
                break;
            case STRUCT_DEF_ID:
                if (value.getValueType() == VariableValue.ValueCase.STRUCT) {
                    applyExpectedTypeToStructFields(typeDef, value.getStruct(), metadataManager);
                }
                break;
            default:
                // Primitive types have no nested containers whose types need pinning.
                break;
        }
    }

    /**
     * Pins the authoritative type onto each field of a STRUCT value by resolving its StructDef and
     * recursing into the declared type of every present field. If the StructDef cannot be resolved,
     * pinning is skipped and the subsequent validation will surface the appropriate error.
     */
    private static void applyExpectedTypeToStructFields(
            TypeDefinitionModel typeDef, StructModel struct, ReadOnlyMetadataManager metadataManager) {
        if (metadataManager == null || struct == null || struct.getInlineStruct() == null) return;

        StructDefModel structDef = new WfService(metadataManager).getStructDef(typeDef.getStructDefId());
        if (structDef == null || structDef.getStructDef() == null) return;

        Map<String, StructFieldDefModel> fieldDefs = structDef.getStructDef().getFields();
        Map<String, StructFieldModel> fieldValues = struct.getInlineStruct().getFields();
        if (fieldDefs == null || fieldValues == null) return;

        for (Map.Entry<String, StructFieldModel> entry : fieldValues.entrySet()) {
            StructFieldDefModel fieldDef = fieldDefs.get(entry.getKey());
            StructFieldModel fieldValue = entry.getValue();
            if (fieldDef != null && fieldValue != null && fieldValue.getValue() != null) {
                applyExpectedType(fieldDef.getFieldType(), fieldValue.getValue(), metadataManager);
            }
        }
    }
}
