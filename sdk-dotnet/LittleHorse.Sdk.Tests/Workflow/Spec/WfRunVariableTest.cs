using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
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
        var exception = Assert.Throws<ArgumentException>(() => 
            WfRunVariable.CreatePrimitiveVar("test-var", null, _parentWfThread));
        
        Assert.Contains("The 'typeOrDefaultVal' argument must be either a VariableType", exception.Message);
    }
    
    [Fact]
    public void WfRunVariable_WithDefaultValue_ShouldSetAVariableType()
    {
        var variableValue = "This is a test";
        
        var wfRunVariable = WfRunVariable.CreatePrimitiveVar("test-var", variableValue, _parentWfThread);
        
        Assert.Equal(VariableType.Str, wfRunVariable.TypeDef.PrimitiveType);
    }

    [Fact]
    public void WfRunVariable_WithType_ShouldSetAVariableType()
    {
        const VariableType expectedType = VariableType.Bool;
        
        var wfRunVariable = WfRunVariable.CreatePrimitiveVar("test-var", expectedType, _parentWfThread);
        
        Assert.Equal(expectedType, wfRunVariable.TypeDef.PrimitiveType);
    }
    
    [Fact]
    public void WfRunVariable_WithThreadVarDef_ShouldCompileSuccessfully()
    {
        const VariableType expectedType = VariableType.Str;
        var wfRunVariable = WfRunVariable.CreatePrimitiveVar("test-var", expectedType, _parentWfThread);
        
        var actualVarDef = wfRunVariable.Compile();


        var varDef = new VariableDef
            { TypeDef = new TypeDefinition { PrimitiveType = expectedType }, Name = wfRunVariable.Name };
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
            WfRunVariable.CreatePrimitiveVar("test-var", expectedType, null!));
        
        Assert.Equal("Value cannot be null. (Parameter 'parent')", exception.Message);
    }

    [Theory]
    [InlineData(VariableType.Str)]
    [InlineData(VariableType.Int)]
    [InlineData(VariableType.Double)]
    [InlineData(VariableType.Bytes)]
    [InlineData(VariableType.Bool)]
    public void WfRunVariable_WithSearchableOn_NonJsonVariableTypes_ShouldThrowAnException(VariableType type)
    {
        string variableName = "test-var";
        WfRunVariable testVar = WfRunVariable.CreatePrimitiveVar(variableName, type, _parentWfThread);
        var exception = Assert.Throws<LHMisconfigurationException>(() => 
            testVar.SearchableOn("$.Content", type)
            );

        Assert.Equal($"Non-Json {variableName} variable contains jsonIndex.", exception.Message);
    }
    
    [Fact]
    public void WfRunVariable_WithSearchableOn_WrongJsonPath_ShouldThrowAnException()
    {
        string variableName = "test-var";
        string fieldPath = "Content";
        WfRunVariable testVar = WfRunVariable.CreatePrimitiveVar(variableName, VariableType.JsonObj, _parentWfThread);
        var exception = Assert.Throws<LHMisconfigurationException>(() => 
            testVar.SearchableOn(fieldPath, VariableType.JsonObj)
        );

        Assert.Equal($"Invalid JsonPath: {fieldPath}", exception.Message);
    }
    
    [Fact]
    public void WfRunVariable_WithSearchableOn_ShouldCompile()
    {
        string variableName = "test-var";
        string fieldPath = "$.Content";
        WfRunVariable testVar = WfRunVariable.CreatePrimitiveVar(variableName, VariableType.JsonObj, _parentWfThread);
        testVar.SearchableOn(fieldPath, VariableType.JsonObj);
       
        var actualVarDef = testVar.Compile();
        
        var varDef = new VariableDef
            { TypeDef = new TypeDefinition { PrimitiveType = VariableType.JsonObj }, Name = variableName };
        var expectedVarDef = new ThreadVarDef
        {
            VarDef = varDef,
            AccessLevel = WfRunVariableAccessLevel.PrivateVar,
            JsonIndexes = { new JsonIndex { FieldPath = fieldPath }}
        };

        Assert.Equal(actualVarDef, expectedVarDef);
    }
    
    [Theory]
    [InlineData(WfRunVariableAccessLevel.PublicVar)]
    [InlineData(WfRunVariableAccessLevel.PrivateVar)]
    [InlineData(WfRunVariableAccessLevel.InheritedVar)]
    public void WfRunVariable_WithAccessLevel_ShouldCompile(WfRunVariableAccessLevel accessLevel)
    {
        string variableName = "test-var";
        WfRunVariable testVar = WfRunVariable.CreatePrimitiveVar(variableName, VariableType.Str, _parentWfThread);
        testVar.WithAccessLevel(accessLevel);
       
        var actualVarDef = testVar.Compile();
        
        var varDef = new VariableDef
            { TypeDef = new TypeDefinition { PrimitiveType = VariableType.Str }, Name = variableName };
        var expectedVarDef = new ThreadVarDef
        {
            VarDef = varDef,
            AccessLevel = accessLevel
        };

        Assert.Equal(actualVarDef, expectedVarDef);
    }
    
    [Fact]
    public void WfRunVariable_AsPublic_ShouldCompile()
    {
        string variableName = "test-var";
        WfRunVariable testVar = WfRunVariable.CreatePrimitiveVar(variableName, VariableType.Str, _parentWfThread);
        testVar.AsPublic();
       
        var actualVarDef = testVar.Compile();
        
        var varDef = new VariableDef
            { TypeDef = new TypeDefinition { PrimitiveType = VariableType.Str }, Name = variableName };
        var expectedVarDef = new ThreadVarDef
        {
            VarDef = varDef,
            AccessLevel = WfRunVariableAccessLevel.PublicVar
        };

        Assert.Equal(actualVarDef, expectedVarDef);
    }
    
    [Fact]
    public void WfRunVariable_AsInherited_ShouldCompile()
    {
        string variableName = "test-var";
        WfRunVariable testVar = WfRunVariable.CreatePrimitiveVar(variableName, VariableType.Str, _parentWfThread);
        testVar.AsInherited();
       
        var actualVarDef = testVar.Compile();
        
        var varDef = new VariableDef
            { TypeDef = new TypeDefinition { PrimitiveType = VariableType.Str }, Name = variableName };
        var expectedVarDef = new ThreadVarDef
        {
            VarDef = varDef,
            AccessLevel = WfRunVariableAccessLevel.InheritedVar
        };

        Assert.Equal(actualVarDef, expectedVarDef);
    }
}