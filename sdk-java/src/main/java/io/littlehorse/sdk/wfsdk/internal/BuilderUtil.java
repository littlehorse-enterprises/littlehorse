package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.Expression;
import io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference;
import io.littlehorse.sdk.common.proto.VariableValue;

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
            if (wrv.getTypeToCastTo() != null) {
                builder.setTypeToCastTo(wrv.getTypeToCastTo());
            }
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

        } else if (variable instanceof LHExpressionImpl) {
            LHExpressionImpl expr = (LHExpressionImpl) variable;
            builder.setExpression(Expression.newBuilder()
                    .setLhs(assignVariable(expr.getLhs()))
                    .setOperation(expr.getOperation())
                    .setRhs(assignVariable(expr.getRhs())));

            if (expr.getTypeToCastTo() != null) {
                builder.getExpressionBuilder().setTypeToCastTo(expr.getTypeToCastTo());
            }
        } else {
            try {
                VariableValue defVal = LHLibUtil.objToVarVal(variable);
                builder.setLiteralValue(defVal);
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }

        return builder.build();
    }
}
