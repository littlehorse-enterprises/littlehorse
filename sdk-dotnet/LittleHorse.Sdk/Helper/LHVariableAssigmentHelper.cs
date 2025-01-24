using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;

namespace LittleHorse.Sdk.Helper;

internal static class LHVariableAssigmentHelper
{
    internal static VariableAssignment AssignVariable(object? value)
    {
        var variableAssignment = new VariableAssignment();

        if (value == null)
        {
            variableAssignment.LiteralValue = new VariableValue();
        }
        else if (value.GetType() == typeof(WfRunVariable))
        {
            var wrVariable = (WfRunVariable) value;
            
            if (wrVariable.JsonPath != null) 
            {
                variableAssignment.JsonPath = wrVariable.JsonPath;
            }
            variableAssignment.VariableName = wrVariable.Name;
        } 
        else if (value is NodeOutput nodeReference)
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
        } // TODO: Add else if conditions for format strings and LH Expressions, it is not needed yet
        else
        {
            VariableValue defVal = LHMappingHelper.ObjectToVariableValue(value);
            variableAssignment.LiteralValue = defVal;
        }

        return variableAssignment;
    }
}