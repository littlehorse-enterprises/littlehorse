using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WfRunVariableTest
{
    private WorkflowThread _parentWfThread;
    
    public WfRunVariableTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        var workflowName = "TestWorkflow";
        void Entrypoint(WorkflowThread thread)
        {
            
        }
        Action<WorkflowThread> action = Entrypoint; 
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, action);
        _parentWfThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);
    }
    
    [Fact]
    public void WfRunVariable_WithoutTypeOrDefaultValue_ShouldThrownAnException()
    {
        var exception = Assert.Throws<InvalidOperationException>(() => 
            new WfRunVariable("test-var", null!, _parentWfThread));
        
        Assert.Contains("The 'typeOrDefaultVal' argument must be either a VariableType", exception.Message);
    }
    
    [Fact]
    public void WfRunVariable_WithDefaultValue_ShouldSetAVariableType()
    {
        var variableValue = "This is a test";
        
        var wfRunVariable = new WfRunVariable("test-var", variableValue, _parentWfThread);
        
        Assert.Equal(VariableType.Str, wfRunVariable.Type);
    }

    [Fact]
    public void WfRunVariable_WithType_ShouldSetAVariableType()
    {
        const VariableType expectedType = VariableType.Bool;
        
        var wfRunVariable = new WfRunVariable("test-var", expectedType, _parentWfThread);
        
        Assert.Equal(expectedType, wfRunVariable.Type);
    }
    
    [Fact]
    public void WfRunVariable_WithThreadVarDef_ShouldCompileSuccessfully()
    {
        const VariableType expectedType = VariableType.Str;
        var wfRunVariable = new WfRunVariable("test-var", expectedType, _parentWfThread);
        
        var actualVarDef = wfRunVariable.Compile();


        var varDef = new VariableDef
            { TypeDef = new TypeDefinition { Type = expectedType }, Name = wfRunVariable.Name };
        var expectedVarDef = new ThreadVarDef
        {
            VarDef = varDef,
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        Assert.Equal(expectedVarDef, actualVarDef);
    }
    
    [Fact]
    public void WfRunVariable_WithNullParentThread_ShouldThrowArgumentNullException()
    {
        const VariableType expectedType = VariableType.Str;
        
        var exception = Assert.Throws<ArgumentNullException>(() =>
            new WfRunVariable("test-var", expectedType, null!));
        
        Assert.Equal("Value cannot be null. (Parameter 'parent')", exception.Message);
    }
}