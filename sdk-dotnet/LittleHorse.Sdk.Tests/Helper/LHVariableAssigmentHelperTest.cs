using System;
using System.Collections.Generic;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Helper;

public class LHVariableAssigmentHelperTest
{
    private WorkflowThread _parentWfThread;
    
    public LHVariableAssigmentHelperTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        var workflowName = "TestWorkflow";
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, null!);
        var mockAction = new Mock<Action<WorkflowThread>>();
        _parentWfThread = new WorkflowThread(workflowName, mockWorkflow.Object, mockAction.Object);
    }
    
    [Fact]
    public void VariableAssigment_WithNoVariable_ShouldReturnNoneLiteralValue()
    {
        var variableAssigment = LHVariableAssigmentHelper.AssignVariable(null);
        
        Assert.Equal(VariableValue.ValueOneofCase.None, variableAssigment.LiteralValue.ValueCase);
        Assert.Equal(String.Empty, variableAssigment.VariableName);
    }
    
    [Fact]
    public void VariableAssigment_WithWfRunVariable_ShouldAssignNameToVariable()
    {
        var wfRunVariable = new WfRunVariable("TestVariable", VariableType.Str, _parentWfThread);
        
        var variableAssigment = LHVariableAssigmentHelper.AssignVariable(wfRunVariable);
        
        Assert.Equal(wfRunVariable.Name, variableAssigment.VariableName);
        Assert.Equal(String.Empty, variableAssigment.JsonPath);
    }
    
    [Fact]
    public void VariableAssigment_WithWfRunVariableContainingJson_ShouldAssignDetailsToVariable()
    {
        var wfRunVariable = new WfRunVariable("TestVariable", VariableType.Str, _parentWfThread)
        {
            JsonPath = "$.order"
        };

        var variableAssigment = LHVariableAssigmentHelper.AssignVariable(wfRunVariable);
        
        Assert.Equal(wfRunVariable.Name, variableAssigment.VariableName);
        Assert.Equal(wfRunVariable.JsonPath, variableAssigment.JsonPath);
    }

    [Fact]
    public void VariableAssigment_WithNodeOutput_ShouldAssignNodeOutputToVariable()
    {
       var nodeOutput = new NodeOutput("wait-to-collect-order-data", _parentWfThread);
       nodeOutput.JsonPath = "$.order";
       
       var variableAssigment = LHVariableAssigmentHelper.AssignVariable(nodeOutput);
       
       Assert.Equal(nodeOutput.NodeName, variableAssigment.NodeOutput.NodeName);
       Assert.Equal(nodeOutput.JsonPath, variableAssigment.JsonPath);
    }

    [Fact]
    public void VariableAssigment_WithNotDefinedObject_ShouldAssignObjectAsDefaultVariable()
    {
        var notDefinedObjects = new List<object>
        {
            "TestVariable", 5, true, 7.892
        };

        foreach (var notDefinedObject in notDefinedObjects)
        {
            var variableAssigment = LHVariableAssigmentHelper.AssignVariable(notDefinedObject);
            
            Assert.Contains(notDefinedObject.ToString()!.ToLower(), variableAssigment.LiteralValue.ToString().ToLower());
        }
    }
}