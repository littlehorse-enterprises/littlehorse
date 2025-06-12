using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WaitForThreadsNodeOutputTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WaitForThreadsNodeOutputTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void WaitForThreadsNodeOutput_WithExceptionName_ShouldCompileAHandlerWithSpecificFailure()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfStartThreadNodes = 1;
        var numberOfWaitForThreadsNodes = 1;
        var numberOfTaskNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task");
            var childThread = wf.SpawnThread("child-thread",
                child => child.Execute("task-with-business-exception"));
            var waitForThreadsNodeOutput = wf.WaitForThreads(SpawnedThreads.Of(childThread));
            waitForThreadsNodeOutput.HandleExceptionOnChild(exceptionName: "business-exception", handler:
                handle => handle.Execute("task-with-process-to-handle-exception"));
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-any-task-TASK" }
            }
        };

        var anyTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "any-task" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-child-thread-START_THREAD" } }
        };

        var spawnThread = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-threads-WAIT_FOR_THREADS",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-child-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-child-thread-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };

        var waitForThreads = new Node
        {
            WaitForThreads = new WaitForThreadsNode
            {
                Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor
                {
                    Threads =
                    {
                        new WaitForThreadsNode.Types.ThreadToWaitFor
                        {
                            ThreadRunNumber = new VariableAssignment { VariableName = "2-child-thread-START_THREAD" }
                        }
                    }
                },
                PerThreadFailureHandlers =
                {
                    new FailureHandlerDef
                    {
                        SpecificFailure = "business-exception",
                        HandlerSpecName = "exn-handler-3-threads-WAIT_FOR_THREADS-business-exception"
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "4-exit-EXIT" } }
        };
        
        var exit = new Node {Exit = new ExitNode()};

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "2-child-thread-START_THREAD",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-any-task-TASK", anyTask);
        expectedThreadSpec.Nodes.Add("2-child-thread-START_THREAD", spawnThread);
        expectedThreadSpec.Nodes.Add("3-threads-WAIT_FOR_THREADS", waitForThreads);
        expectedThreadSpec.Nodes.Add("4-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfTaskNodes + numberOfStartThreadNodes +
                                    numberOfWaitForThreadsNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void WaitForThreadsNodeOutput_WithoutExceptionName_ShouldCompileAHandlerWithAnyFailureOfType()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfStartThreadNodes = 1;
        var numberOfWaitForThreadsNodes = 1;
        var numberOfTaskNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task");
            var childThread = wf.SpawnThread("child-thread",
                child => child.Execute("task-with-business-exception"));
            var waitForThreadsNodeOutput = wf.WaitForThreads(SpawnedThreads.Of(childThread));
            waitForThreadsNodeOutput.HandleExceptionOnChild(
                handle => handle.Execute("task-with-process-to-handle-exception"));
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-any-task-TASK" }
            }
        };

        var anyTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "any-task" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-child-thread-START_THREAD" } }
        };

        var spawnThread = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-threads-WAIT_FOR_THREADS",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-child-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-child-thread-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };

        var waitForThreads = new Node
        {
            WaitForThreads = new WaitForThreadsNode
            {
                Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor
                {
                    Threads =
                    {
                        new WaitForThreadsNode.Types.ThreadToWaitFor
                        {
                            ThreadRunNumber = new VariableAssignment { VariableName = "2-child-thread-START_THREAD" }
                        }
                    }
                },
                PerThreadFailureHandlers =
                {
                    new FailureHandlerDef
                    {
                        AnyFailureOfType = FailureHandlerDef.Types.LHFailureType.FailureTypeException,
                        HandlerSpecName = "exn-handler-3-threads-WAIT_FOR_THREADS-FAILURE_TYPE_EXCEPTION"
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "4-exit-EXIT" } }
        };
        
        var exit = new Node {Exit = new ExitNode()};

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "2-child-thread-START_THREAD",
                TypeDef = new TypeDefinition
                {
                    Type = VariableType.Int
                }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-any-task-TASK", anyTask);
        expectedThreadSpec.Nodes.Add("2-child-thread-START_THREAD", spawnThread);
        expectedThreadSpec.Nodes.Add("3-threads-WAIT_FOR_THREADS", waitForThreads);
        expectedThreadSpec.Nodes.Add("4-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfTaskNodes + numberOfStartThreadNodes +
                                    numberOfWaitForThreadsNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void WaitForThreadsNodeOutput_WithErrorTypeInParam_ShouldCompileAHandlerWithSpecificFailure()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfStartThreadNodes = 1;
        var numberOfWaitForThreadsNodes = 1;
        var numberOfTaskNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task");
            var childThread = wf.SpawnThread("child-thread",
                child => child.Execute("task-with-technical-error"));
            var waitForThreadsNodeOutput = wf.WaitForThreads(SpawnedThreads.Of(childThread));
            waitForThreadsNodeOutput.HandleErrorOnChild(error: LHErrorType.TaskError, handler:
                handle => handle.Execute("task-with-process-to-handle-error"));
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-any-task-TASK" }
            }
        };

        var anyTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "any-task" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-child-thread-START_THREAD" } }
        };

        var spawnThread = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-threads-WAIT_FOR_THREADS",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-child-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-child-thread-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };


        var exit = new Node {Exit = new ExitNode()};
        var waitForThreads = new Node
        {
            WaitForThreads = new WaitForThreadsNode
            {
                Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor
                {
                    Threads =
                    {
                        new WaitForThreadsNode.Types.ThreadToWaitFor
                        {
                            ThreadRunNumber = new VariableAssignment { VariableName = "2-child-thread-START_THREAD" }
                        }
                    }
                },
                PerThreadFailureHandlers =
                {
                    new FailureHandlerDef
                    {
                        SpecificFailure = LHConstants.ErrorTypes[LHErrorType.TaskError.ToString()],
                        HandlerSpecName = "error-handler-3-threads-WAIT_FOR_THREADS-TASK_ERROR",
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "4-exit-EXIT" } }
        };

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "2-child-thread-START_THREAD",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-any-task-TASK", anyTask);
        expectedThreadSpec.Nodes.Add("2-child-thread-START_THREAD", spawnThread);
        expectedThreadSpec.Nodes.Add("3-threads-WAIT_FOR_THREADS", waitForThreads);
        expectedThreadSpec.Nodes.Add("4-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfTaskNodes + numberOfStartThreadNodes +
                                    numberOfWaitForThreadsNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void WaitForThreadsNodeOutput_WithoutErrorTypeInParam_ShouldCompileAHandlerWithAnyFailureOfType()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfStartThreadNodes = 1;
        var numberOfWaitForThreadsNodes = 1;
        var numberOfTaskNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task");
            var childThread = wf.SpawnThread("child-thread",
                child => child.Execute("task-with-technical-error"));
            var waitForThreadsNodeOutput = wf.WaitForThreads(SpawnedThreads.Of(childThread));
            waitForThreadsNodeOutput.HandleErrorOnChild(
                handle => handle.Execute("task-with-process-to-handle-error"));
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-any-task-TASK" }
            }
        };

        var anyTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "any-task" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-child-thread-START_THREAD" } }
        };

        var spawnThread = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-threads-WAIT_FOR_THREADS",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-child-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-child-thread-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };


        var exit = new Node {Exit = new ExitNode()};
        var waitForThreads = new Node
        {
            WaitForThreads = new WaitForThreadsNode
            {
                Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor
                {
                    Threads =
                    {
                        new WaitForThreadsNode.Types.ThreadToWaitFor
                        {
                            ThreadRunNumber = new VariableAssignment { VariableName = "2-child-thread-START_THREAD" }
                        }
                    }
                },
                PerThreadFailureHandlers =
                {
                    new FailureHandlerDef
                    {
                        AnyFailureOfType = FailureHandlerDef.Types.LHFailureType.FailureTypeError,
                        HandlerSpecName = "error-handler-3-threads-WAIT_FOR_THREADS-FAILURE_TYPE_ERROR",
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "4-exit-EXIT" } }
        };

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "2-child-thread-START_THREAD",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-any-task-TASK", anyTask);
        expectedThreadSpec.Nodes.Add("2-child-thread-START_THREAD", spawnThread);
        expectedThreadSpec.Nodes.Add("3-threads-WAIT_FOR_THREADS", waitForThreads);
        expectedThreadSpec.Nodes.Add("4-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfTaskNodes + numberOfStartThreadNodes +
                                    numberOfWaitForThreadsNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void WaitForThreadsNodeOutput_WithAnyFailure_ShouldCompileAHandlerWithAnyFailureOfType()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfStartThreadNodes = 1;
        var numberOfWaitForThreadsNodes = 1;
        var numberOfTaskNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task");
            var childThread = wf.SpawnThread("child-thread",
                child => child.Execute("task-with-technical-problem"));
            var waitForThreadsNodeOutput = wf.WaitForThreads(SpawnedThreads.Of(childThread));
            waitForThreadsNodeOutput.HandleAnyFailureOnChild(
                handle => handle.Execute("task-with-process-to-handle-failure"));
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-any-task-TASK" }
            }
        };

        var anyTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "any-task" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-child-thread-START_THREAD" } }
        };

        var spawnThread = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-threads-WAIT_FOR_THREADS",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-child-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-child-thread-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };


        var exit = new Node {Exit = new ExitNode()};
        var waitForThreads = new Node
        {
            WaitForThreads = new WaitForThreadsNode
            {
                Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor
                {
                    Threads =
                    {
                        new WaitForThreadsNode.Types.ThreadToWaitFor
                        {
                            ThreadRunNumber = new VariableAssignment { VariableName = "2-child-thread-START_THREAD" }
                        }
                    }
                },
                PerThreadFailureHandlers =
                {
                    new FailureHandlerDef
                    {
                        HandlerSpecName = "failure-handler-3-threads-WAIT_FOR_THREADS-ANY_FAILURE",
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "4-exit-EXIT" } }
        };

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "2-child-thread-START_THREAD",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-any-task-TASK", anyTask);
        expectedThreadSpec.Nodes.Add("2-child-thread-START_THREAD", spawnThread);
        expectedThreadSpec.Nodes.Add("3-threads-WAIT_FOR_THREADS", waitForThreads);
        expectedThreadSpec.Nodes.Add("4-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfTaskNodes + numberOfStartThreadNodes +
                                    numberOfWaitForThreadsNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
}