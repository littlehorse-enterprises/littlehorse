using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowThreadErrorsAndExceptionsTest
{
    private readonly Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowThreadErrorsAndExceptionsTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void WfThread_WithoutSpecificError_ShouldCompileErrorHandling()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTasks = 2;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            NodeOutput node = wf.Execute("fail");
            wf.HandleError(
                node,
                handler =>
                {
                    handler.Execute("my-task");
                }
            );
            wf.Execute("my-task");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedSpec = GetExpectedThreadSpec(
            new FailureHandlerDef
            {
                HandlerSpecName = "exn-handler-1-fail-TASK-FAILURE_TYPE_ERROR",
                AnyFailureOfType = FailureHandlerDef.Types.LHFailureType.FailureTypeError
            });

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }

    [Fact]
    public void WfThread_WithSpecificError_ShouldCompileErrorHandling()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTasks = 2;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            NodeOutput node = wf.Execute("fail");
            wf.HandleError(
                node,
                LHErrorType.Timeout,
                handler =>
                {
                    handler.Execute("my-task");
                }
            );
            wf.Execute("my-task");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedSpec = GetExpectedThreadSpec(
            new FailureHandlerDef
            {
                HandlerSpecName = $"exn-handler-1-fail-TASK-{LHConstants.ErrorTypes[LHErrorType.Timeout.ToString()]}",
                SpecificFailure = LHConstants.ErrorTypes[LHErrorType.Timeout.ToString()]
            });

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }
    
    [Fact]
    public void WfThread_WithExceptionName_ShouldCompileExceptionHandling()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTasks = 2;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var exceptionName = "any-business-exception";

        void EntryPointAction(WorkflowThread wf)
        {
            NodeOutput node = wf.Execute("fail");
            wf.HandleException(
                node,
                exceptionName,
                handler =>
                {
                    handler.Execute("my-task");
                }
            );
            wf.Execute("my-task");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedSpec = GetExpectedThreadSpec(
            new FailureHandlerDef
            {
                HandlerSpecName = $"exn-handler-1-fail-TASK-{exceptionName}",
                SpecificFailure = exceptionName
            });

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    } 
    
    [Fact]
    public void WfThread_WithoutExceptionName_ShouldCompileExceptionHandling()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTasks = 2;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            NodeOutput node = wf.Execute("fail");
            wf.HandleException(
                node,
                handler =>
                {
                    handler.Execute("my-task");
                }
            );
            wf.Execute("my-task");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();

        var expectedSpec = GetExpectedThreadSpec(
            new FailureHandlerDef
            {
                HandlerSpecName = "exn-handler-1-fail-TASK",
                AnyFailureOfType = FailureHandlerDef.Types.LHFailureType.FailureTypeException
            });

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    } 
    
    [Fact]
    public void WfThread_WithBusinessException_ShouldCompileAnyFailureHandling()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTasks = 2;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            NodeOutput node = wf.Execute("fail");
            wf.HandleAnyFailure(
                node,
                handler =>
                {
                    handler.Execute("my-task");
                }
            );
            wf.Execute("my-task");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();

        var expectedSpec = GetExpectedThreadSpec(
            new FailureHandlerDef
            {
                HandlerSpecName = $"exn-handler-1-fail-TASK-{LHConstants.AnyFailure}",
            });

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }

    private ThreadSpec GetExpectedThreadSpec(FailureHandlerDef failureHandlerDef)
    {
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-fail-TASK" }
            }
        };

        var failTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "fail" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-my-task-TASK" } },
            FailureHandlers =
            {
                failureHandlerDef
            }
        };
        
        var myTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "my-task" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "3-exit-EXIT" } }
        };

        var exitNode = new Node
        {
            Exit = new ExitNode()
        };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-fail-TASK", failTask);
        expectedSpec.Nodes.Add("2-my-task-TASK", myTask);
        expectedSpec.Nodes.Add("3-exit-EXIT", exitNode);

        return expectedSpec;
    }
}