using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;

namespace LittleHorse.Sdk.Helper;

internal static class LHVariableAssigmentHelper
{
    internal static VariableAssignment AssignVariable(object? variable) 
    {
        var variableAssignment = new VariableAssignment();

        if (variable == null) 
        {
            variableAssignment.LiteralValue = new VariableValue();
        } 
        else if (variable.GetType() == typeof(WfRunVariable)) 
        {
            var wrVariable = (WfRunVariable) variable;
            if (wrVariable.JsonPath != null) 
            {
                variableAssignment.JsonPath = wrVariable.JsonPath;
            }
            variableAssignment.VariableName = wrVariable.Name;
        } 
        else if (variable is NodeOutput nodeReference) 
        {
            // We can use the new `VariableAssignment` feature: NodeOutputReference
            var nodeOutputReference = new VariableAssignment.Types.NodeOutputReference
            {
                NodeName = nodeReference.NodeName
            };
            variableAssignment.NodeOutput = nodeOutputReference;

            if (nodeReference.JsonPath != null)
            {
                variableAssignment.JsonPath = nodeReference.JsonPath;
            }
        }
        /*else if (variable.getClass().equals(LHFormatStringImpl.class)) 
        {
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
        } */
        else 
        {
            VariableValue defVal = LHMappingHelper.ObjectToVariableValue(variable);
            variableAssignment.LiteralValue = defVal;
        }

        return variableAssignment;
    }
}