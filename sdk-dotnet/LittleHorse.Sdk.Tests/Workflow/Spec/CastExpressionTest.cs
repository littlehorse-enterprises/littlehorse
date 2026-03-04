using System;
using LittleHorse.Sdk.Common.Proto;
using WfWorkflow = LittleHorse.Sdk.Workflow.Spec.Workflow;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class CastExpressionTest
{
    public CastExpressionTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }

    [Fact]
    public void CastExpressionSetsTargetType()
    {
        var wf = new WfWorkflow("cast-test-workflow", wf => {
            var myVar = wf.DeclareBool("my-var");
            myVar.Assign(wf.Execute("string-method").CastToBool());
        });

        var putWf = wf.Compile();
        var entrypoint = putWf.ThreadSpecs[putWf.EntrypointThreadName];
        var taskNode = entrypoint.Nodes["1-string-method-TASK"];
        Assert.NotNull(taskNode);
        Assert.NotEmpty(taskNode.OutgoingEdges);

        var vmuts = taskNode.OutgoingEdges[0].VariableMutations;
        Assert.NotEmpty(vmuts);

        var rhs = vmuts[0].RhsAssignment;
        Assert.NotNull(rhs);
        Assert.NotNull(rhs.TargetType);
        Assert.Equal(VariableType.Bool, rhs.TargetType.PrimitiveType);
    }

    [Fact]
    public void CastNodeOutputToInt()
    {
        var wf = new WfWorkflow("cast-test-nodeoutput-int", wf => {
            var myVar = wf.DeclareInt("my-var");
            myVar.Assign(wf.Execute("double-method").CastToInt());
        });

        var putWf = wf.Compile();
        var entrypoint = putWf.ThreadSpecs[putWf.EntrypointThreadName];
        var taskNode = entrypoint.Nodes["1-double-method-TASK"];
        Assert.NotNull(taskNode);

        var vmuts = taskNode.OutgoingEdges[0].VariableMutations;
        Assert.NotEmpty(vmuts);
        var rhs = vmuts[0].RhsAssignment;
        Assert.NotNull(rhs.TargetType);
        Assert.Equal(VariableType.Int, rhs.TargetType.PrimitiveType);
    }

    [Fact]
    public void CastStringToDouble()
    {
        var wf = new WfWorkflow("cast-test-str-to-double", wf => {
            var myVar = wf.DeclareDouble("my-var");
            myVar.Assign(wf.Execute("string-method").CastToDouble());
        });

        var putWf = wf.Compile();
        var entrypoint = putWf.ThreadSpecs[putWf.EntrypointThreadName];
        var taskNode = entrypoint.Nodes["1-string-method-TASK"];
        Assert.NotNull(taskNode);

        var vmuts = taskNode.OutgoingEdges[0].VariableMutations;
        Assert.NotEmpty(vmuts);
        var rhs = vmuts[0].RhsAssignment;
        Assert.NotNull(rhs.TargetType);
        Assert.Equal(VariableType.Double, rhs.TargetType.PrimitiveType);
    }

    [Fact]
    public void CastStringToBool()
    {
        var wf = new WfWorkflow("cast-test-str-to-bool", wf => {
            var myVar = wf.DeclareBool("my-var");
            myVar.Assign(wf.Execute("string-method").CastToBool());
        });

        var putWf = wf.Compile();
        var entrypoint = putWf.ThreadSpecs[putWf.EntrypointThreadName];
        var taskNode = entrypoint.Nodes["1-string-method-TASK"];
        Assert.NotNull(taskNode);

        var vmuts = taskNode.OutgoingEdges[0].VariableMutations;
        Assert.NotEmpty(vmuts);
        var rhs = vmuts[0].RhsAssignment;
        Assert.NotNull(rhs.TargetType);
        Assert.Equal(VariableType.Bool, rhs.TargetType.PrimitiveType);
    }

    [Fact]
    public void JsonPathCastToInt()
    {
        var wf = new WfWorkflow("cast-test-jsonpath-to-int", wf => {
            var myVar = wf.DeclareInt("my-var");
            myVar.Assign(wf.Execute("int-method").WithJsonPath("$.int").CastToInt());
        });

        var putWf = wf.Compile();
        var entrypoint = putWf.ThreadSpecs[putWf.EntrypointThreadName];
        var taskNode = entrypoint.Nodes["1-int-method-TASK"];
        Assert.NotNull(taskNode);

        var vmuts = taskNode.OutgoingEdges[0].VariableMutations;
        Assert.NotEmpty(vmuts);
        var rhs = vmuts[0].RhsAssignment;
        Assert.NotNull(rhs.TargetType);
        Assert.Equal(VariableType.Int, rhs.TargetType.PrimitiveType);
        Assert.Equal("$.int", rhs.JsonPath);
    }
}
