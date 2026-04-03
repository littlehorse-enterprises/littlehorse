package io.littlehorse.sdk.worker.internal.util;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionException;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.LHTaskParameter;
import java.util.Objects;

public class VariableMapping {

    private final LHTypeAdapterRegistry typeAdapterRegistry;
    private final String variableName;
    private final Class<?> parameterJavaType;
    private final boolean expectsNativeLHArray;

    public VariableMapping(
            VariableDef variableDef, LHTaskParameter lhTaskParameter, LHTypeAdapterRegistry typeAdapterRegistry)
            throws TaskSchemaMismatchError {
        Objects.requireNonNull(variableDef, "VariableDef cannot be null");
        Objects.requireNonNull(lhTaskParameter, "LHTaskParameter cannot be null");
        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry, "Type adapter registry cannot be null");
        this.variableName = variableDef.getName();
        this.parameterJavaType = lhTaskParameter.getParameterType();
        this.expectsNativeLHArray =
                lhTaskParameter.getVariableDef().getTypeDef().getDefinedTypeCase()
                        == TypeDefinition.DefinedTypeCase.INLINE_ARRAY_DEF;

        try {
            validateParamAgainstVariableDef(variableDef, lhTaskParameter);
        } catch (TaskSchemaMismatchError taskSchemaMismatchError) {
            throw new TaskSchemaMismatchError("Invalid assignment for var " + lhTaskParameter.getVariableName() + ": "
                    + taskSchemaMismatchError.getMessage());
        }
    }

    private void validateParamAgainstVariableDef(VariableDef variableDef, LHTaskParameter lhTaskParameter)
            throws TaskSchemaMismatchError {
        VariableDef expected = lhTaskParameter.getVariableDef();

        if (!variableDef.getName().equals(expected.getName())) {
            throw new TaskSchemaMismatchError(String.format(
                    "TaskDef variable name <%s>, func expects parameter name <%s>",
                    variableDef.getName(), expected.getName()));
        }

        TypeDefinition providedType = requireTypeDefinition(variableDef);
        TypeDefinition expectedType = lhTaskParameter.getVariableDef().getTypeDef();

        if (!areTypesCompatible(providedType, expectedType)) {
            throw new TaskSchemaMismatchError(String.format(
                    "TaskDef provides type <%s>, func expects <%s>",
                    formatTypeDefinition(providedType), formatTypeDefinition(expectedType)));
        }

        if (providedType.getDefinedTypeCase() != expectedType.getDefinedTypeCase()) {
            throw new TaskSchemaMismatchError(String.format(
                    "TaskDef provides type <%s>, func expects <%s>",
                    formatTypeDefinition(providedType), formatTypeDefinition(expectedType)));
        }
    }

    private static boolean areTypesCompatible(TypeDefinition providedType, TypeDefinition expectedType) {
        if (providedType.getDefinedTypeCase() != expectedType.getDefinedTypeCase()) {
            return false;
        }

        switch (providedType.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                return providedType.getPrimitiveType() == expectedType.getPrimitiveType();
            case STRUCT_DEF_ID:
                return providedType.getStructDefId().equals(expectedType.getStructDefId());
            case INLINE_ARRAY_DEF:
                return areTypesCompatible(
                        providedType.getInlineArrayDef().getArrayType(),
                        expectedType.getInlineArrayDef().getArrayType());
            case DEFINEDTYPE_NOT_SET:
            default:
                return false;
        }
    }

    private static TypeDefinition requireTypeDefinition(VariableDef variableDef) {
        if (!variableDef.hasTypeDef()) {
            throw new TaskSchemaMismatchError(String.format(
                    "TaskDef variable <%s> is missing a TypeDefinition. This client requires TypeDefinitions for all VariableDefs.",
                    variableDef.getName()));
        }

        TypeDefinition typeDef = variableDef.getTypeDef();
        if (typeDef.getDefinedTypeCase() == TypeDefinition.DefinedTypeCase.DEFINEDTYPE_NOT_SET) {
            throw new TaskSchemaMismatchError(String.format(
                    "TaskDef variable <%s> has an unrecognized TypeDefinition incompatible with this client.",
                    variableDef.getName()));
        }

        return typeDef;
    }

    private static String formatTypeDefinition(TypeDefinition typeDefinition) {
        switch (typeDefinition.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                return typeDefinition.getPrimitiveType().name();
            case STRUCT_DEF_ID:
                return "StructDef<" + typeDefinition.getStructDefId().getName() + ">";
            case INLINE_ARRAY_DEF:
                return "Array<"
                        + formatTypeDefinition(
                                typeDefinition.getInlineArrayDef().getArrayType()) + ">";
            default:
                return "UNSPECIFIED";
        }
    }

    private static VarNameAndVal getAssignmentByName(ScheduledTask taskInstance, String variableName)
            throws InputVarSubstitutionException {
        for (VarNameAndVal assignment : taskInstance.getVariablesList()) {
            if (variableName.equals(assignment.getVarName())) {
                return assignment;
            }
        }

        throw new InputVarSubstitutionException(
                "Could not find assignment for variable: " + variableName,
                new Exception("ScheduledTask did not include a VarNameAndVal entry for that variable name."));
    }

    public Object assign(ScheduledTask taskInstance) throws InputVarSubstitutionException {
        VarNameAndVal assignment = getAssignmentByName(taskInstance, variableName);
        String taskDefParamName = assignment.getVarName();

        VariableValue val = assignment.getValue();

        try {
            if (expectsNativeLHArray && val.getValueCase() == VariableValue.ValueCase.ARRAY) {
                return assignNativeArray(val);
            }
            return LHLibUtil.varValToObj(val, this.parameterJavaType, this.typeAdapterRegistry);
        } catch (LHSerdeException e) {
            throw new InputVarSubstitutionException(
                    "Failed serializing Java object for variable: " + taskDefParamName, e);
        }
    }

    private Object assignNativeArray(VariableValue val) throws LHSerdeException {
        if (!parameterJavaType.isArray()) {
            throw new LHSerdeException("Native LittleHorse arrays can only be assigned to Java array parameters.");
        }

        Class<?> componentType = parameterJavaType.getComponentType();
        int size = val.getArray().getItemsCount();
        Object outputArray = java.lang.reflect.Array.newInstance(componentType, size);

        for (int i = 0; i < size; i++) {
            Object item = LHLibUtil.varValToObj(val.getArray().getItems(i), componentType, typeAdapterRegistry);
            java.lang.reflect.Array.set(outputArray, i, item);
        }

        return outputArray;
    }
}
