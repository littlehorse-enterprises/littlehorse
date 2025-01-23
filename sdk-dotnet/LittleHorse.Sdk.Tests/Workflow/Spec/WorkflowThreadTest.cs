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
        var mockEntrypointFunction = new Mock<Action<WorkflowThread>>();
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockEntrypointFunction.Object);
        var workflowThread = new WorkflowThread(workflowName, mockWorkflow.Object, mockEntrypointFunction.Object);

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
    public void WorkflowThread_InvokingAddVariables_ShouldBuildSpecWithNodesAndVariablesDefs()
    {
        var workflowName = "TestWorkflow";
        var mockParentFunction = new Mock<Action<WorkflowThread>>();
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockParentFunction.Object);

        void AddVariablesAction(WorkflowThread wf)
        {
            wf.AddVariable("str-test-variable", VariableType.Str);
            wf.AddVariable("int-test-variable", 5);
        }

        var workflowThread = new WorkflowThread(workflowName, mockParentWorkflow.Object, AddVariablesAction);
        
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
    
    [Fact]
    public void WfThread_InvokingActionAfterItsInstantiation_ShouldThrowAnError()
    {
        var workflowName = "TestWorkflow";
        var mockParentFunction = new Mock<Action<WorkflowThread>>();
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockParentFunction.Object);
        var wfThread = new WorkflowThread(workflowName, mockParentWorkflow.Object, mockParentFunction.Object);
        
        var exception = Assert.Throws<InvalidOperationException>(() => wfThread.Execute("test-task-name"));
            
        Assert.Equal("Using an inactive thread", exception.Message);
    }

    [Fact]
    public void WfThread_InvokingExecuteTasksWithArgs_ShouldReturnATaskTypeNodeOutput()
    {
        var workflowName = "TestWorkflow";
        var mockParentFunction = new Mock<Action<WorkflowThread>>();
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockParentFunction.Object);
        
        void ExecuteAction(WorkflowThread wf)
        {
            var expectedTaskName = "test-task-name";
            var variable = wf.AddVariable("str-test-variable", VariableType.Str);
            var actualNodeOutput = wf.Execute(expectedTaskName, variable);
            
            Assert.Contains(expectedTaskName + "-Task", actualNodeOutput.NodeName);
        }
        
        new WorkflowThread(workflowName, mockParentWorkflow.Object, ExecuteAction);
    }
    
    [Fact]
    public void WfThread_InvokingExecuteTasksWithNoArgs_ShouldReturnATaskTypeNodeOutput()
    {
        var workflowName = "TestWorkflow";
        var mockParentFunction = new Mock<Action<WorkflowThread>>();
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockParentFunction.Object);
        
        void ExecuteAction(WorkflowThread wf)
        {
            var expectedTaskName = "test-task-name";
            var actualNodeOutput = wf.Execute(expectedTaskName);
            
            Assert.Contains(expectedTaskName + "-Task", actualNodeOutput.NodeName);
        }
        
        new WorkflowThread(workflowName, mockParentWorkflow.Object, ExecuteAction);
    }

    [Fact]
    public void WfThread_InvokingExecuteTasksWithArgs_ShouldBuildSpecWIthTaskNodeAndVariablesDefs()
    {
        var workflowName = "TestWorkflow";
        var mockParentFunction = new Mock<Action<WorkflowThread>>();
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, mockParentFunction.Object);
        void EntryPointAction(WorkflowThread wf)
        {
            var variableDef = wf.AddVariable("str-test-variable", VariableType.Str);
            wf.Execute("test-task-name", variableDef);
        }
        var workflowThread = new WorkflowThread(workflowName, mockParentWorkflow.Object, EntryPointAction);
        
        var wfThreadCompiled = workflowThread.Compile();
        
        var actualResult = LHMappingHelper.ProtoToJson(wfThreadCompiled);
        var expectedResult =
            "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": [ { \"sinkNodeName\": " +
            "\"1-test-task-name-Task\", \"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"entrypoint\": " +
            "{ } }, \"1-test-task-name-Task\": { \"outgoingEdges\": [ { \"sinkNodeName\": \"2-exit-Exit\", " +
            "\"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"task\": { \"taskDefId\": { \"name\": " +
            "\"test-task-name\" }, \"timeoutSeconds\": 0, \"retries\": 0, \"variables\": [ { \"variableName\": " +
            "\"str-test-variable\" } ] } }, \"2-exit-Exit\": { \"outgoingEdges\": [ ], \"failureHandlers\": [ ], " +
            "\"exit\": { } } }, \"variableDefs\": [ { \"varDef\": { \"type\": \"STR\", \"name\": \"str-test-variable\", " +
            "\"maskedValue\": false }, \"required\": false, \"searchable\": false, \"jsonIndexes\": [ ], " +
            "\"accessLevel\": \"PRIVATE_VAR\" } ], \"interruptDefs\": [ ] }";
        var expectedNumberOfNodes = 3;
        Assert.Equal(expectedNumberOfNodes, wfThreadCompiled.Nodes.Count);
        Assert.Equal(expectedResult, actualResult);
    }
}