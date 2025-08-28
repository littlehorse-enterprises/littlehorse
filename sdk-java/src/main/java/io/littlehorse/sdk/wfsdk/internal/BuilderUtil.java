package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.Expression;
import io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.List;
import java.util.Map;

class BuilderUtil {

    static VariableAssignment assignVariable(Object variable) {
        if (variable == null) {
            return buildNullAssignment();
        }

        if (WfRunVariableImpl.class.equals(variable.getClass())) {
            return buildFromWfRunVariable((WfRunVariableImpl) variable);
        }
        if (NodeOutputImpl.class.isAssignableFrom(variable.getClass())) {
            return buildFromNodeOutput((NodeOutputImpl) variable);
        }
        if (LHFormatStringImpl.class.equals(variable.getClass())) {
            return buildFromFormatString((LHFormatStringImpl) variable);
        }
        if (variable instanceof CastExpressionImpl) {
            return buildFromCastExpression((CastExpressionImpl) variable);
        }
        if (variable instanceof LHExpressionImpl) {
            return buildFromLHExpression((LHExpressionImpl) variable);
        }

        return buildFromLiteral(variable);
    }

    private static VariableAssignment buildNullAssignment() {
        return VariableAssignment.newBuilder()
                .setLiteralValue(VariableValue.newBuilder())
                .build();
    }

    private static VariableAssignment buildFromWfRunVariable(WfRunVariableImpl wfRunVariable) {
        VariableAssignment.Builder builder = VariableAssignment.newBuilder()
                .setVariableName(wfRunVariable.name);
        if (wfRunVariable.jsonPath != null) {
            builder.setJsonPath(wfRunVariable.jsonPath);
        }
        return builder.build();
    }

    private static VariableAssignment buildFromNodeOutput(NodeOutputImpl nodeOutput) {
        VariableAssignment.Builder builder = VariableAssignment.newBuilder()
                .setNodeOutput(NodeOutputReference.newBuilder()
                        .setNodeName(nodeOutput.nodeName)
                        .build());
        if (nodeOutput.jsonPath != null) {
            builder.setJsonPath(nodeOutput.jsonPath);
        }
        return builder.build();
    }

    private static VariableAssignment buildFromFormatString(LHFormatStringImpl inputFormat) {
        return VariableAssignment.newBuilder()
                .setFormatString(VariableAssignment.FormatString.newBuilder()
                        .setFormat(assignVariable(inputFormat.getFormat()))
                        .addAllArgs(inputFormat.getArgs()))
                .build();
    }

    private static VariableAssignment buildFromCastExpression(CastExpressionImpl castingExpresion) {
        VariableAssignment sourceAssignment = assignVariable(castingExpresion.getSource());
        return sourceAssignment.toBuilder()
                .setTargetType(TypeDefinition.newBuilder()
                        .setType(castingExpresion.getTargetType())
                        .setMasked(false)
                        .build())
                .build();
    }

    private static VariableAssignment buildFromLHExpression(LHExpressionImpl expresion) {
        return VariableAssignment.newBuilder()
                .setExpression(Expression.newBuilder()
                        .setLhs(assignVariable(expresion.getLhs()))
                        .setOperation(expresion.getOperation())
                        .setRhs(assignVariable(expresion.getRhs())))
                .build();
    }

    private static VariableAssignment buildFromLiteral(Object variable) {
        try {
            VariableValue defVal = LHLibUtil.objToVarVal(variable);
            return VariableAssignment.newBuilder()
                    .setLiteralValue(defVal)
                    .build();
        } catch (LHSerdeException exn) {
            throw new IllegalArgumentException(
                    "Failed to convert literal to VariableAssignment for variable: " + variable, exn
            );
        }
    }


    private static final ReturnType STRING_TYPE = buildReturnType(VariableType.STR);
    private static final ReturnType DOUBLE_TYPE = buildReturnType(VariableType.DOUBLE);
    private static final ReturnType INT_TYPE = buildReturnType(VariableType.INT);
    private static final ReturnType BOOL_TYPE = buildReturnType(VariableType.BOOL);
    private static final ReturnType JSON_OBJ_TYPE = buildReturnType(VariableType.JSON_OBJ);
    private static final ReturnType JSON_ARR_TYPE = buildReturnType(VariableType.JSON_ARR);
    private static final ReturnType EMPTY_RETURN_TYPE = ReturnType.newBuilder().build();

    static ReturnType javaTypeToReturnType(Class<?> payloadClass) {
        if (payloadClass == null) return EMPTY_RETURN_TYPE;

        if (String.class.isAssignableFrom(payloadClass)) return STRING_TYPE;
        if (Double.class.isAssignableFrom(payloadClass)) return DOUBLE_TYPE;
        if (Integer.class.isAssignableFrom(payloadClass)) return INT_TYPE;
        if (Boolean.class.isAssignableFrom(payloadClass)) return BOOL_TYPE;
        if (Map.class.isAssignableFrom(payloadClass)) return JSON_OBJ_TYPE;
        if (List.class.isAssignableFrom(payloadClass)) return JSON_ARR_TYPE;

        throw new IllegalArgumentException(
                "Unsupported payload type: " + payloadClass.getName()
                        + ". Must be one of String, Double, Integer, Boolean, Map, or List"
        );
    }

    private static ReturnType buildReturnType(VariableType type) {
        return ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder().setType(type))
                .build();
    }
}
