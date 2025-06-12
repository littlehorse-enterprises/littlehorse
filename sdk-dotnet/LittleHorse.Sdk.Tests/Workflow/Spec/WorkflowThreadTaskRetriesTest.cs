using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowThreadTaskRetriesTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowThreadTaskRetriesTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void WfThread_WithRetriesInTaskNode_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTasks = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("greet").WithRetries(2);
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();

        var expectedSpec = new ThreadSpec();

        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges = { new Edge { SinkNodeName = "1-greet-TASK" } }
        };

        var greetTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "greet" },
                Retries = 2
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-exit-EXIT" } }
        };

        var exitNode = new Node { Exit = new ExitNode() };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-greet-TASK", greetTask);
        expectedSpec.Nodes.Add("2-exit-EXIT", exitNode);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }
}