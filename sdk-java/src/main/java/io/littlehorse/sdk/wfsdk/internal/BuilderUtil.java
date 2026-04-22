package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHPath;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructBuilder;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.Expression;
import io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;

class BuilderUtil {

    static VariableAssignment assignVariable(Object variable) {
        if (variable instanceof VariableAssignment) {
            return (VariableAssignment) variable;
        }

        return assignVariable(variable, LHTypeAdapterRegistry.empty());
    }

    static VariableAssignment assignVariable(Object variable, LHTypeAdapterRegistry typeAdapterRegistry) {
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
            return buildFromFormatString((LHFormatStringImpl) variable, typeAdapterRegistry);
        }
        if (variable instanceof LHStructBuilderImpl) {
            return buildFromStructBuilder((LHStructBuilderImpl) variable);
        }
        if (variable instanceof CastExpressionImpl) {
            return buildFromCastExpression((CastExpressionImpl) variable, typeAdapterRegistry);
        }
        if (variable instanceof LHExpressionImpl) {
            return buildFromLHExpression((LHExpressionImpl) variable, typeAdapterRegistry);
        }

        return buildFromLiteral(variable, typeAdapterRegistry);
    }

    private static VariableAssignment buildNullAssignment() {
        return VariableAssignment.newBuilder()
                .setLiteralValue(VariableValue.newBuilder())
                .build();
    }

    private static VariableAssignment buildFromWfRunVariable(WfRunVariableImpl wfRunVariable) {
        VariableAssignment.Builder builder = VariableAssignment.newBuilder().setVariableName(wfRunVariable.name);
        if (wfRunVariable.getJsonPath() != null) {
            builder.setJsonPath(wfRunVariable.getJsonPath());
        } else if (wfRunVariable.getLhPath() != null
                && !wfRunVariable.getLhPath().isEmpty()) {
            builder.setLhPath(
                    LHPath.newBuilder().addAllPath(wfRunVariable.getLhPath()).build());
        }
        return builder.build();
    }

    private static VariableAssignment buildFromNodeOutput(NodeOutputImpl nodeOutput) {
        VariableAssignment.Builder builder = VariableAssignment.newBuilder()
                .setNodeOutput(NodeOutputReference.newBuilder()
                        .setNodeName(nodeOutput.nodeName)
                        .build());
        if (nodeOutput.getJsonPath() != null) {
            builder.setJsonPath(nodeOutput.getJsonPath());
        } else if (nodeOutput.getLhPath() != null && !nodeOutput.getLhPath().isEmpty()) {
            builder.setLhPath(
                    LHPath.newBuilder().addAllPath(nodeOutput.getLhPath()).build());
        }
        return builder.build();
    }

    private static VariableAssignment buildFromFormatString(
            LHFormatStringImpl inputFormat, LHTypeAdapterRegistry typeAdapterRegistry) {
        return VariableAssignment.newBuilder()
                .setFormatString(VariableAssignment.FormatString.newBuilder()
                        .setFormat(assignVariable(inputFormat.getFormat(), typeAdapterRegistry))
                        .addAllArgs(inputFormat.getArgs()))
                .build();
    }

    private static VariableAssignment buildFromCastExpression(
            CastExpressionImpl castingExpresion, LHTypeAdapterRegistry typeAdapterRegistry) {
        VariableAssignment sourceAssignment = assignVariable(castingExpresion.getSource(), typeAdapterRegistry);
        return sourceAssignment.toBuilder()
                .setTargetType(TypeDefinition.newBuilder()
                        .setPrimitiveType(castingExpresion.getTargetType())
                        .setMasked(false)
                        .build())
                .build();
    }

    private static VariableAssignment buildFromStructBuilder(LHStructBuilderImpl structBuilder) {
        StructBuilder builderProto = structBuilder.toProto();
        return VariableAssignment.newBuilder().setStructBuilder(builderProto).build();
    }

    private static VariableAssignment buildFromLHExpression(
            LHExpressionImpl expresion, LHTypeAdapterRegistry typeAdapterRegistry) {
        Expression.Builder builder = Expression.newBuilder()
                .setLhs(assignVariable(expresion.getLhs(), typeAdapterRegistry))
                .setRhs(assignVariable(expresion.getRhs(), typeAdapterRegistry));
        if (expresion.getOperation() != null) {
            builder.setMutationType(expresion.getOperation());
        } else {
            builder.setComparator(expresion.getComparator());
        }
        return VariableAssignment.newBuilder().setExpression(builder.build()).build();
    }

    private static VariableAssignment buildFromLiteral(Object variable, LHTypeAdapterRegistry typeAdapterRegistry) {
        try {
            VariableValue defVal = LHLibUtil.objToVarVal(variable, typeAdapterRegistry);
            return VariableAssignment.newBuilder().setLiteralValue(defVal).build();
        } catch (LHSerdeException exn) {
            throw new IllegalArgumentException(
                    "Failed to convert literal to VariableAssignment for variable: " + variable, exn);
        }
    }

    static ReturnType javaTypeToReturnType(Class<?> payloadClass) {
        return javaTypeToReturnType(payloadClass, LHTypeAdapterRegistry.empty());
    }

    static ReturnType javaTypeToReturnType(Class<?> payloadClass, LHTypeAdapterRegistry typeAdapterRegistry) {
        if (payloadClass == null) {
            // We don't set the typeDef: the event has no payload
            return ReturnType.newBuilder().build();
        }

        LHClassType lhClassType = LHClassType.fromJavaClass(payloadClass, typeAdapterRegistry);

        return ReturnType.newBuilder()
                .setReturnType(lhClassType.getTypeDefinition())
                .build();
    }
}
