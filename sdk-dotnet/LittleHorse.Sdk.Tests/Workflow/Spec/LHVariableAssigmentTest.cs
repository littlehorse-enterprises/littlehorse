using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Helper;

public class LHVariableAssigmentTest
{
    private WorkflowThread _parentWfThread;
    
    public LHVariableAssigmentTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        var workflowName = "TestWorkflow";
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, null!);
        var mockAction = new Mock<Action<WorkflowThread>>();
        _parentWfThread = new WorkflowThread(mockWorkflow.Object, mockAction.Object);
    }
    
    [Fact]
    public void VariableAssigment_WithNoVariable_ShouldReturnNoneLiteralValue()
    {
        var variableAssigment = _parentWfThread.AssignVariableHelper(null);
        
        Assert.Equal(VariableValue.ValueOneofCase.None, variableAssigment.LiteralValue.ValueCase);
        Assert.Equal(string.Empty, variableAssigment.VariableName);
    }
    
    [Fact]
    public void VariableAssigment_WithWfRunVariable_ShouldAssignNameToVariable()
    {
        var wfRunVariable = new WfRunVariable("TestVariable", VariableType.Str, _parentWfThread);
        
        var variableAssigment = _parentWfThread.AssignVariableHelper(wfRunVariable);
        
        Assert.Equal(wfRunVariable.Name, variableAssigment.VariableName);
        Assert.Equal(string.Empty, variableAssigment.JsonPath);
    }
    
    [Fact]
    public void VariableAssigment_WithWfRunVariableContainingJson_ShouldAssignDetailsToVariable()
    {
        var wfRunVariable = new WfRunVariable("TestVariable", VariableType.JsonObj, _parentWfThread);
        var wfRunVariableWithJson = wfRunVariable.WithJsonPath("$.order");

        var variableAssigment = _parentWfThread.AssignVariableHelper(wfRunVariableWithJson);
        
        Assert.Equal(wfRunVariableWithJson.Name, variableAssigment.VariableName);
        Assert.Equal(wfRunVariableWithJson.JsonPath, variableAssigment.JsonPath);
    }

    [Fact]
    public void VariableAssigment_WithNodeOutput_ShouldAssignNodeOutputToVariable()
    {
       var nodeOutput = new NodeOutput("wait-to-collect-order-data", _parentWfThread);
       
       var variableAssigment = _parentWfThread.AssignVariableHelper(nodeOutput);
       
       Assert.Equal(nodeOutput.NodeName, variableAssigment.NodeOutput.NodeName);
    }

    [Theory]
    [InlineData("TestVariable")]
    [InlineData(5)]
    [InlineData(true)]
    [InlineData(7.892)]
    public void VariableAssigment_WithNotDefinedObject_ShouldAssignObjectAsLiteralValue(object notDefinedObject)
    {
        var variableAssigment = _parentWfThread.AssignVariableHelper(notDefinedObject);
        
        Assert.Contains(notDefinedObject.ToString()!.ToLower(), variableAssigment.LiteralValue.ToString().ToLower());
    }
    
    [Fact]
    public void VariableAssigment_WithFormattedStringValue_ShouldAssignFormatAndArgs()
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