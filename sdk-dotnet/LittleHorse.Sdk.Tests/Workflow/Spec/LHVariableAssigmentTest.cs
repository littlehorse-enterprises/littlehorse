using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Helper;

public class LHVariableAssignmentTest
{
    private WorkflowThread _parentWfThread;

    public LHVariableAssignmentTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        var workflowName = "TestWorkflow";
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, null!);
        var mockAction = new Mock<Action<WorkflowThread>>();
        _parentWfThread = new WorkflowThread(mockWorkflow.Object, mockAction.Object);
    }

    [Fact]
    public void VariableAssignment_WithNoVariable_ShouldReturnNoneLiteralValue()
    {
        var variableAssigment = _parentWfThread.AssignVariableHelper(null);

        Assert.Equal(VariableValue.ValueOneofCase.None, variableAssigment.LiteralValue.ValueCase);
        Assert.Equal(string.Empty, variableAssigment.VariableName);
    }

    [Fact]
    public void VariableAssignment_WithWfRunVariable_ShouldAssignNameToVariable()
    {
        var wfRunVariable = WfRunVariable.CreatePrimitiveVar("TestVariable", VariableType.Str, _parentWfThread);

        var variableAssigment = _parentWfThread.AssignVariableHelper(wfRunVariable);

        Assert.Equal(wfRunVariable.Name, variableAssigment.VariableName);
        Assert.Equal(string.Empty, variableAssigment.JsonPath);
    }

    [Fact]
    public void VariableAssignment_WithWfRunVariableContainingJson_ShouldAssignDetailsToVariable()
    {
        var wfRunVariable = WfRunVariable.CreatePrimitiveVar("TestVariable", VariableType.JsonObj, _parentWfThread);
        var wfRunVariableWithJson = wfRunVariable.WithJsonPath("$.order");

        var variableAssigment = _parentWfThread.AssignVariableHelper(wfRunVariableWithJson);

        Assert.Equal(wfRunVariableWithJson.Name, variableAssigment.VariableName);
        Assert.Equal(wfRunVariableWithJson.JsonPath, variableAssigment.JsonPath);
    }

    [Fact]
    public void VariableAssignment_WithNodeOutput_ShouldAssignNodeOutputToVariable()
    {
       var nodeOutput = new NodeOutput("wait-to-collect-order-data", _parentWfThread);

       var variableAssigment = _parentWfThread.AssignVariableHelper(nodeOutput);

       Assert.Equal(nodeOutput.NodeName, variableAssigment.NodeOutput.NodeName);
    }

    [Fact]
    public void VariableAssignment_WithString_ShouldAssignStringAsLiteralValue()
    {
        const string notDefinedObject = "TestVariable";

        var variableAssigment = _parentWfThread.AssignVariableHelper(notDefinedObject);

        Assert.Equal(notDefinedObject, variableAssigment.LiteralValue.Str);
    }

    [Fact]
    public void VariableAssignment_WithInt_ShouldAssignIntAsLiteralValue()
    {
        const int notDefinedObject = 5;

        var variableAssigment = _parentWfThread.AssignVariableHelper(notDefinedObject);

        Assert.Equal(notDefinedObject, variableAssigment.LiteralValue.Int);
    }

    [Fact]
    public void VariableAssignment_WithBool_ShouldAssignBoolAsLiteralValue()
    {
        const bool notDefinedObject = true;

        var variableAssigment = _parentWfThread.AssignVariableHelper(notDefinedObject);

        Assert.Equal(notDefinedObject, variableAssigment.LiteralValue.Bool);
    }

    [Fact]
    public void VariableAssignment_WithDouble_ShouldAssignDoubleAsLiteralValue()
    {
        const double notDefinedObject = 7.892;

        var variableAssigment = _parentWfThread.AssignVariableHelper(notDefinedObject);

        Assert.Equal(notDefinedObject, variableAssigment.LiteralValue.Double);
    }

    [Fact]
    public void VariableAssignment_WithFormattedStringValue_ShouldAssignFormatAndArgs()
    {
        object[] args = { 4, "Hello World!" };
        var formatString = "This is {} try of {}";
        var lhFormatString = new LHFormatString(_parentWfThread, "This is {} try of {}", args);

        var variableAssigment = _parentWfThread.AssignVariableHelper(lhFormatString);

        var expectedVariableAssigned = new VariableAssignment { LiteralValue = new VariableValue { Str = formatString } };
        VariableAssignment[] expectedArgsAssigned =
        {
            new() { LiteralValue = new VariableValue { Int = (int)args[0] } },
            new() { LiteralValue = new VariableValue { Str = (string)args[1] } }
        };
        Assert.Equal(expectedVariableAssigned, variableAssigment.FormatString.Format);
        Assert.Equal(expectedArgsAssigned, variableAssigment.FormatString.Args);
    }
}
