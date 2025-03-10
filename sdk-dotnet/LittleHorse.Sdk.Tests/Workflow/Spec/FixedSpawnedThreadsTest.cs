using System;
using System.Collections.Generic;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class FixedSpawnedThreadsTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public FixedSpawnedThreadsTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact] 
    public void FixedSpawnedThreads_WithThreadNumberVariables_ShouldBuildWaitForThreadsNode()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        Random random = new Random();
        var wfRunVariable1 = new WfRunVariable("ID", 
            random.NextInt64(1111111111, 9999999999), workflowThread);
        var spawnedThread1 = new SpawnedThread(workflowThread, 
            "onboarding-new-app-old-clients", 
            wfRunVariable1);
        var wfRunVariable2 = new WfRunVariable("ACCOUNT_NUMBER", 
            random.NextInt64(1111111111, 9999999999), workflowThread);
        var spawnedThread2 = new SpawnedThread(workflowThread, 
            "onboarding-new-app-new-clients", 
            wfRunVariable2);
        var fixedSpawnedThreads = new FixedSpawnedThreads(spawnedThread1, spawnedThread2);
        
        var actualWaitForThreadNode = fixedSpawnedThreads.BuildNode();
        
        var expectedWaitForThreadNode = new WaitForThreadsNode();
        var threads = new List<WaitForThreadsNode.Types.ThreadToWaitFor>();
        threads.Add(new WaitForThreadsNode.Types.ThreadToWaitFor
        {
            ThreadRunNumber = new VariableAssignment
            {
                VariableName = wfRunVariable1.Name
            }
        });
        threads.Add(new WaitForThreadsNode.Types.ThreadToWaitFor
        {
            ThreadRunNumber = new VariableAssignment
            {
                VariableName = wfRunVariable2.Name
            }
        });
        expectedWaitForThreadNode.Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor
        {
            Threads = { threads }
        };
        
        var expectedNumberOfThreads = 2;
        Assert.Equal(expectedNumberOfThreads, actualWaitForThreadNode.Threads.Threads.Count);
        Assert.Equal(expectedWaitForThreadNode, actualWaitForThreadNode);
    }

    [Fact]
    public void FixedSpawnedThreads_WithThreadNonNumberVariables_ShouldThrowException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        string nonNumberDefaultValue = "1111111111";
        var wfRunVariable1 = new WfRunVariable("ID", nonNumberDefaultValue, workflowThread);
        var spawnedThread1 = new SpawnedThread(workflowThread, 
            "onboarding-new-app-old-clients", 
            wfRunVariable1);
        var fixedSpawnedThreads = new FixedSpawnedThreads(spawnedThread1);
        
        var exception = Assert.Throws<ArgumentException>(
            () => fixedSpawnedThreads.BuildNode());
            
        Assert.Equal("Only int variables are supported.", exception.Message);
    }
}