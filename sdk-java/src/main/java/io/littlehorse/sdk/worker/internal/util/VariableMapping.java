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

    public VariableMapping(
            VariableDef variableDef, LHTaskParameter lhTaskParameter, LHTypeAdapterRegistry typeAdapterRegistry)
            throws TaskSchemaMismatchError {
        Objects.requireNonNull(variableDef, "VariableDef cannot be null");
        Objects.requireNonNull(lhTaskParameter, "LHTaskParameter cannot be null");
        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry, "Type adapter registry cannot be null");
        this.variableName = variableDef.getName();
        this.parameterJavaType = lhTaskParameter.getParameterType();

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

        if (providedType.getDefinedTypeCase() != expectedType.getDefinedTypeCase()) {
            throw new TaskSchemaMismatchError(String.format(
                    "TaskDef provides type <%s>, func expects <%s>",
                    formatTypeDefinition(providedType), formatTypeDefinition(expectedType)));
        }

        switch (providedType.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                if (providedType.getPrimitiveType() != expectedType.getPrimitiveType()) {
                    throw new TaskSchemaMismatchError(String.format(
                            "TaskDef provides primitive <%s>, func expects <%s>",
                            providedType.getPrimitiveType(), expectedType.getPrimitiveType()));
                }
                break;
            case STRUCT_DEF_ID:
                if (!providedType.getStructDefId().equals(expectedType.getStructDefId())) {
                    throw new TaskSchemaMismatchError(String.format(
                            "TaskDef provides StructDef <%s>, func expects StructDef <%s>",
                            providedType.getStructDefId(), expectedType.getStructDefId()));
                }
                break;
            case DEFINEDTYPE_NOT_SET:
            default:
                throw new TaskSchemaMismatchError(String.format(
                        "TaskDef variable <%s> has an unrecognized Type Definition incompatible with this client.",
                        variableDef.getName()));
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
            return LHLibUtil.varValToObj(val, this.parameterJavaType, this.typeAdapterRegistry);
        } catch (LHSerdeException e) {
            throw new InputVarSubstitutionException(
                    "Failed serializing Java object for variable: " + taskDefParamName, e);
        }
    }
}
