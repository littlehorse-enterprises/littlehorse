using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Workflow.Spec;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WfRunVariableTest
{
    [Fact]
    public void WfRunVariable_WithoutTypeOrDefaultValue_ShouldThrownAnException()
    {
        var exception = Assert.Throws<InvalidOperationException>(() => 
            new WfRunVariable("test-var", null, null));
        
        Assert.Contains("The 'typeOrDefaultVal' argument must be either a VariableType", exception.Message);
    }
    
    [Fact]
    public void WfRunVariable_WithDefaultValue_ShouldSetAVariableType()
    {
        var variableValue = "This is a test";
        
        var wfRunVariable = new WfRunVariable("test-var", variableValue, null);
        
        Assert.Equal(VariableType.Str, wfRunVariable.Type);
    }

    [Fact]
    public void WfRunVariable_WithType_ShouldSetAVariableType()
    {
        const VariableType expectedType = VariableType.Bool;
        
        var wfRunVariable = new WfRunVariable("test-var", expectedType, null);
        
        Assert.Equal(expectedType, wfRunVariable.Type);
    }
    
    [Fact]
    public void WfRunVariable_WithThreadVarDef_ShouldCompileSuccessfully()
    {
        const VariableType expectedType = VariableType.Str;
        var wfRunVariable = new WfRunVariable("test-var", expectedType, null);
        
        var compiledWfRunVariable = wfRunVariable.Compile();
        
        var actualResult = LHMappingHelper.ProtoToJson(compiledWfRunVariable);
        var expectedResult =
            "{ \"varDef\": { \"type\": \"STR\", \"name\": \"test-var\", \"maskedValue\": false }, \"required\": false, \"searchable\": false, \"jsonIndexes\": [ ], \"accessLevel\": \"PRIVATE_VAR\" }";
        
        Assert.Equal(expectedResult, actualResult);
    }
}