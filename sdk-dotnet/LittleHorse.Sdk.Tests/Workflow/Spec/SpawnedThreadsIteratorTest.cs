using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class SpawnedThreadsIteratorTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public SpawnedThreadsIteratorTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }

    [Fact]
    public void SpawnedThreadsIterator_With_JsonArrayVariable_ShouldBuildWaitForThreadsNode()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        var wfRunVariable = WfRunVariable.CreatePrimitiveVar("Person",
            VariableType.JsonArr, workflowThread);
       
        var spawnedThreadsIterator = new SpawnedThreadsIterator(wfRunVariable);
        
        var actualSpawnedThreadsIterator = spawnedThreadsIterator.BuildNode(WaitForThreadsStrategy.WaitForAll);
        
        var waitNode = new WaitForThreadsNode
        {
            ThreadList = new VariableAssignment
            {
                VariableName = wfRunVariable.Name
            }
        };
        var expectedSpawnedThreadsIterator = waitNode;
        
        Assert.Equal(expectedSpawnedThreadsIterator, actualSpawnedThreadsIterator);
    }

    [Fact]
    public void SpawnedThreadsIterator_With_StringVariable_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        var wfRunVariable = WfRunVariable.CreatePrimitiveVar("Person",
            VariableType.Str, workflowThread);

        var exception = Assert.Throws<ArgumentException>(
            () => new SpawnedThreadsIterator(wfRunVariable));
            
        Assert.Equal("Only support for json arrays.", exception.Message);
    }

    [Fact]
    public void SpawnedThreadsIterator_WithStructVariable_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        var wfRunVariable = WfRunVariable.CreateStructDefVar("Person", "test-struct", workflowThread);

        var exception = Assert.Throws<ArgumentException>(
            () => new SpawnedThreadsIterator(wfRunVariable));

        Assert.Equal("Only support for json arrays.", exception.Message);
    }
}