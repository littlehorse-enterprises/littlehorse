using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WfThreadDoIfTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }

    public WfThreadDoIfTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void WfThread_WithIfStatement_ShouldCompileAnSpecAddingATaskNode()
    {
        var workflowName = "TestWorkflow";
        var leftHandSide = 20;
        var RightHandSide = 10;
        var numberOfNopNodes = 2;
        var numberOfTasks = 1;
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        void Entrypoint(WorkflowThread thread)
        {
            thread.DoIf(
                thread.Condition(leftHandSide, Comparator.GreaterThan, RightHandSide),
                ifThread => ifThread.Execute("task"));
        }
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var wfThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);
        
        var compiledWfThread = wfThread.Compile();
        
        var actualSpec = LHMappingHelper.ProtoToJson(compiledWfThread);
        var expectedSpec =
            "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": [ { \"sinkNodeName\": " +
            "\"1-nop-NOP\", \"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"entrypoint\": " +
            "{ } }, \"1-nop-NOP\": { \"outgoingEdges\": [ { \"sinkNodeName\": \"2-task-TASK\", \"condition\": " +
            "{ \"comparator\": \"GREATER_THAN\", \"left\": { \"literalValue\": { \"int\": \"20\" } }, \"right\": " +
            "{ \"literalValue\": { \"int\": \"10\" } } }, \"variableMutations\": [ ] }, { \"sinkNodeName\": " +
            "\"3-nop-NOP\", \"condition\": { \"comparator\": \"LESS_THAN_EQ\", \"left\": { \"literalValue\": " +
            "{ \"int\": \"20\" } }, \"right\": { \"literalValue\": { \"int\": \"10\" } } }, \"variableMutations\":" +
            " [ ] } ], \"failureHandlers\": [ ], \"nop\": { } }, \"2-task-TASK\": { \"outgoingEdges\": " +
            "[ { \"sinkNodeName\": \"3-nop-NOP\", \"variableMutations\": [ ] } ], \"failureHandlers\": [ ]," +
            " \"task\": { \"taskDefId\": { \"name\": \"task\" }, \"timeoutSeconds\": 0, \"retries\": 0, " +
            "\"variables\": [ ] } }, \"3-nop-NOP\": { \"outgoingEdges\": [ { \"sinkNodeName\": \"4-exit-EXIT\", " +
            "\"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"nop\": { } }, \"4-exit-EXIT\": " +
            "{ \"outgoingEdges\": [ ], \"failureHandlers\": [ ], \"exit\": { } } }, \"variableDefs\": [ ]," +
            " \"interruptDefs\": [ ] }";
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfNopNodes + numberOfTasks;

        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Contains(expectedSpec, actualSpec!);
    }

    [Fact]
    public void WfThread_WithIfElseStatement_ShouldCompileAnSpecAddingATaskNodesBothStatements()
    {
        var leftHandSide = 1;
        var RightHandSide = 0;
        var numberOfNopNodes = 2;
        var numberOfTasks = 2;
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        void Entrypoint(WorkflowThread thread)
        {
            thread.DoIf(
                thread.Condition(leftHandSide, Comparator.LessThan, RightHandSide),
                ifThread => ifThread.Execute("task-a"),
                elseThread => elseThread.Execute("task-b"));
        }
        
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>("test-workflow", _action);
        var wfThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);
        
        var compiledWfThread = wfThread.Compile();
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfNopNodes + numberOfTasks;
        var actualSpec = LHMappingHelper.ProtoToJson(compiledWfThread);
        var expectedSpec = "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": [ " +
                           "{ \"sinkNodeName\": \"1-nop-NOP\", \"variableMutations\": [ ] } ], \"failureHandlers\":" +
                           " [ ], \"entrypoint\": { } }, \"1-nop-NOP\": { \"outgoingEdges\": [ { \"sinkNodeName\":" +
                           " \"2-task-a-TASK\", \"condition\": { \"comparator\": \"LESS_THAN\", \"left\":" +
                           " { \"literalValue\": { \"int\": \"1\" } }, \"right\": { \"literalValue\": { \"int\": " +
                           "\"0\" } } }, \"variableMutations\": [ ] }, { \"sinkNodeName\": \"3-task-b-TASK\", " +
                           "\"condition\": { \"comparator\": \"GREATER_THAN_EQ\", \"left\": { \"literalValue\": " +
                           "{ \"int\": \"1\" } }, \"right\": { \"literalValue\": { \"int\": \"0\" } } }, " +
                           "\"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"nop\": { } }, " +
                           "\"2-task-a-TASK\": { \"outgoingEdges\": [ { \"sinkNodeName\": \"4-nop-NOP\", " +
                           "\"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"task\": { \"taskDefId\": " +
                           "{ \"name\": \"task-a\" }, \"timeoutSeconds\": 0, \"retries\": 0, \"variables\": [ ] } }, " +
                           "\"3-task-b-TASK\": { \"outgoingEdges\": [ { \"sinkNodeName\": \"4-nop-NOP\", " +
                           "\"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"task\": { \"taskDefId\": " +
                           "{ \"name\": \"task-b\" }, \"timeoutSeconds\": 0, \"retries\": 0, \"variables\": [ ] } }, " +
                           "\"4-nop-NOP\": { \"outgoingEdges\": [ { \"sinkNodeName\": \"5-exit-EXIT\", " +
                           "\"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"nop\": { } }, " +
                           "\"5-exit-EXIT\": { \"outgoingEdges\": [ ], \"failureHandlers\": [ ], \"exit\": { } } }, " +
                           "\"variableDefs\": [ ], \"interruptDefs\": [ ] }";
        
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, actualSpec);
    }
}