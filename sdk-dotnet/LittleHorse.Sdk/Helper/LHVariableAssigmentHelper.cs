using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;

namespace LittleHorse.Sdk.Helper;

internal static class LHVariableAssigmentHelper
{
    internal static VariableAssignment AssignVariable(Object variable) 
    {
        var variableAssignment = new VariableAssignment();

        if (variable == null) 
        {
            variableAssignment.LiteralValue = new VariableValue();
        } 
        else if (variable.GetType() == typeof(WfRunVariable)) 
        {
            var wrVariable = (WfRunVariable) variable;
            if (wrVariable.JsonPath != null
                ) {
                variableAssignment.JsonPath = wrVariable.JsonPath;
            }
            variableAssignment.VariableName = wrVariable.Name;
        } 
        /*else if (variable is NodeOutput) 
        {
            // We can use the new `VariableAssignment` feature: NodeOutputReference
            NodeOutputImpl nodeReference = (NodeOutputImpl) variable;

            variableAssignment.setNodeOutput(VariableAssignment.Types.NodeOutputReference.newBuilder()
                    .setNodeName(nodeReference.nodeName)
                    .build());

            if (nodeReference.jsonPath != null) {
                variableAssignment.setJsonPath(nodeReference.jsonPath);
            }
        } else if (variable.getClass().equals(LHFormatStringImpl.class)) {
            LHFormatStringImpl format = (LHFormatStringImpl) variable;
            variableAssignment.setFormatString(VariableAssignment.FormatString.newBuilder()
                    .setFormat(assignVariable(format.getFormat()))
                    .addAllArgs(format.getArgs()));

        } else if (variable instanceof LHExpressionImpl) {
            LHExpressionImpl expr = (LHExpressionImpl) variable;
            variableAssignment.setExpression(Expression.newBuilder()
                    .setLhs(assignVariable(expr.getLhs()))
                    .setOperation(expr.getOperation())
                    .setRhs(assignVariable(expr.getRhs())));
        } else {
            try {
                VariableValue defVal = LHLibUtil.objToVarVal(variable);
                variableAssignment.setLiteralValue(defVal);
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }*/

        return variableAssignment;
    }
}