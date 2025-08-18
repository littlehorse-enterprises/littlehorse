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
        VariableAssignment.Builder builder = VariableAssignment.newBuilder();

        if (variable == null) {
            builder.setLiteralValue(VariableValue.newBuilder());
        } else if (variable.getClass().equals(WfRunVariableImpl.class)) {
            WfRunVariableImpl wrv = (WfRunVariableImpl) variable;
            if (wrv.jsonPath != null) {
                builder.setJsonPath(wrv.jsonPath);
            }
            builder.setVariableName(wrv.name);
        } else if (NodeOutputImpl.class.isAssignableFrom(variable.getClass())) {
            // We can use the new `VariableAssignment` feature: NodeOutputReference
            NodeOutputImpl nodeReference = (NodeOutputImpl) variable;

            builder.setNodeOutput(NodeOutputReference.newBuilder()
                    .setNodeName(nodeReference.nodeName)
                    .build());

            if (nodeReference.jsonPath != null) {
                builder.setJsonPath(nodeReference.jsonPath);
            }
        } else if (variable.getClass().equals(LHFormatStringImpl.class)) {
            LHFormatStringImpl format = (LHFormatStringImpl) variable;
            builder.setFormatString(VariableAssignment.FormatString.newBuilder()
                    .setFormat(assignVariable(format.getFormat()))
                    .addAllArgs(format.getArgs()));

        } else if (variable instanceof CastExpressionImpl) {
            CastExpressionImpl castExpr = (CastExpressionImpl) variable;
            VariableAssignment sourceAssignment = assignVariable(castExpr.getSource());
            builder = sourceAssignment.toBuilder();

            // Set the target_type field
            builder.setTargetType(io.littlehorse.sdk.common.proto.TypeDefinition.newBuilder()
                    .setType(castExpr.getTargetType())
                    .setMasked(false)
                    .build());
        } else if (variable instanceof LHExpressionImpl) {
            LHExpressionImpl expr = (LHExpressionImpl) variable;
            builder.setExpression(Expression.newBuilder()
                    .setLhs(assignVariable(expr.getLhs()))
                    .setOperation(expr.getOperation())
                    .setRhs(assignVariable(expr.getRhs())));
        } else {
            try {
                VariableValue defVal = LHLibUtil.objToVarVal(variable);
                builder.setLiteralValue(defVal);
            } catch (LHSerdeException exn) {
                throw new RuntimeException(exn);
            }
        }

        return builder.build();
    }

    static ReturnType javaTypeToReturnType(Class<?> payloadClass) {
        if (payloadClass == null) {
            // We don't set the typeDef: the event has no payload
            return ReturnType.newBuilder().build();
        }
        TypeDefinition.Builder typeDef = TypeDefinition.newBuilder();
        if (String.class.isAssignableFrom(payloadClass)) {
            typeDef.setType(VariableType.STR);
        } else if (Double.class.isAssignableFrom(payloadClass)) {
            typeDef.setType(VariableType.DOUBLE);
        } else if (Integer.class.isAssignableFrom(payloadClass)) {
            typeDef.setType(VariableType.INT);
        } else if (Boolean.class.isAssignableFrom(payloadClass)) {
            typeDef.setType(VariableType.BOOL);
        } else if (Map.class.isAssignableFrom(payloadClass)) {
            typeDef.setType(VariableType.JSON_OBJ);
        } else if (List.class.isAssignableFrom(payloadClass)) {
            typeDef.setType(VariableType.JSON_ARR);
        } else {
            throw new IllegalArgumentException(
                    "ExternalEventDef payload class must be one of String, Double, Integer or Boolean");
        }
        return ReturnType.newBuilder().setReturnType(typeDef).build();
    }
}
