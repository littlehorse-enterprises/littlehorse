using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowThreadTest
{
    public WorkflowThreadTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }
    
    [Fact]
    public void WorkflowThread_WithoutInvokingLogic_ShouldCompileSpecEntrypointAndExitNodes()
    {
        var workflowName = "TestWorkflow";
        var mockAction = new Mock<Action<WorkflowThread>>();
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockAction.Object);
        var workflowThread = new WorkflowThread(workflowName, mockWorkflow.Object, mockAction.Object);

        var wfThreadCompiled = workflowThread.Compile();
        
        var actualResult = LHMappingHelper.ProtoToJson(wfThreadCompiled);
        var expectedResult =
            "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": [ { \"sinkNodeName\":" +
            " \"1-exit-Exit\", \"variableMutations\": [ ] } ], " +
            "\"failureHandlers\": [ ], \"entrypoint\": { } }, " +
            "\"1-exit-Exit\": { \"outgoingEdges\": [ ], \"failureHandlers\": [ ], " +
            "\"exit\": { } } }, \"variableDefs\": [ ], \"interruptDefs\": [ ] }";
        var expectedNumberOfNodes = 2;
        Assert.Equal(expectedNumberOfNodes, wfThreadCompiled.Nodes.Count);
        Assert.Equal(expectedResult, actualResult);
    }

    [Fact]
    public void WorkflowThread_InvokingAddVariables_ShouldBuildSpecWithNodesAndVariableDefs()
    {
        var workflowName = "TestWorkflow";
        var mockEntrypointAction = new Mock<Action<WorkflowThread>>();
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockEntrypointAction.Object);
        Action<WorkflowThread> addVariablesAction = wf =>
        {
            wf.AddVariable("str-test-variable", VariableType.Str);
            wf.AddVariable("int-test-variable", 5);
        };
        var workflowThread = new WorkflowThread(workflowName, mockWorkflow.Object, addVariablesAction);
        
        var wfThreadCompiled = workflowThread.Compile();
        
        var actualResult = LHMappingHelper.ProtoToJson(wfThreadCompiled);
        var expectedResult = "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": " +
                             "[ { \"sinkNodeName\": \"1-exit-Exit\", \"variableMutations\": [ ] } ], " +
                             "\"failureHandlers\": [ ], \"entrypoint\": { } }, \"1-exit-Exit\": { \"outgoingEdges\": [ ], " +
                             "\"failureHandlers\": [ ], \"exit\": { } } }, \"variableDefs\": [ { \"varDef\": " +
                             "{ \"type\": \"STR\", \"name\": \"str-test-variable\", \"maskedValue\": false }, " +
                             "\"required\": false, \"searchable\": false, \"jsonIndexes\": [ ], \"accessLevel\": " +
                             "\"PRIVATE_VAR\" }, { \"varDef\": { \"type\": \"INT\", \"name\": \"int-test-variable\", " +
                             "\"defaultValue\": { \"int\": \"5\" }, \"maskedValue\": false }, " +
                             "\"required\": false, \"searchable\": false, \"jsonIndexes\": [ ], " +
                             "\"accessLevel\": \"PRIVATE_VAR\" } ], \"interruptDefs\": [ ] }";
        
        var expectedNumberOfNodes = 2;
        Assert.Equal(expectedNumberOfNodes, wfThreadCompiled.Nodes.Count);
        Assert.Equal(expectedResult, actualResult);
    }
}