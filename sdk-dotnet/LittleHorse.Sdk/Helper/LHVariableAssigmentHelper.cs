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
        } // TODO: Add else if conditions for format strings and LH Expressions, it is still not needed
        else
        {
            VariableValue defVal = LHMappingHelper.ObjectToVariableValue(variable);
            variableAssignment.LiteralValue = defVal;
        }

        return variableAssignment;
    }
}