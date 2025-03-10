using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowThreadTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowThreadTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void WorkflowThread_WithEmptyFunction_ShouldCompileSpecEntrypointAndExitNodes()
    {
        var workflowName = "TestWorkflow";
        void Entrypoint(WorkflowThread thread)
        {
            
        }
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var workflowThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);

        var actualSpec = workflowThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-exit-EXIT" }
            }
        };
        
        var exitNode = new Node
        {
            Exit = new ExitNode()
        };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-exit-EXIT", exitNode);
        
        var expectedNumberOfNodes = 2;
        Assert.Equal(expectedNumberOfNodes, actualSpec.Nodes.Count);
        Assert.Equal(expectedSpec, actualSpec);
    }

    [Fact]
    public void WorkflowThread_InvokingAddVariables_ShouldBuildSpecWithNodesAndVariablesDefs()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void AddVariablesAction(WorkflowThread wf)
        {
            wf.AddVariable("str-test-variable", VariableType.Str);
            wf.AddVariable("int-test-variable", 5);
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, AddVariablesAction);
        
        var actualSpec = workflowThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-exit-EXIT" }
            }
        };
        
        var exitNode = new Node
        {
            Exit = new ExitNode()
        };
        
        var strVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "str-test-variable",
                Type = VariableType.Str
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        var intVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "int-test-variable",
                Type = VariableType.Int,
                DefaultValue = new VariableValue {Int = 5}
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-exit-EXIT", exitNode);
        expectedSpec.VariableDefs.Add(strVarDef);
        expectedSpec.VariableDefs.Add(intVarDef);
        
        var expectedNumberOfNodes = 2;
        Assert.Equal(expectedNumberOfNodes, actualSpec.Nodes.Count);
        Assert.Equal(expectedSpec, actualSpec);
    }
    
    [Fact]
    public void WfThread_InvokingActionAfterItsInstantiation_ShouldThrowAnError()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var wfThread = new WorkflowThread(mockParentWorkflow.Object, ParentEntrypoint);
        
        var exception = Assert.Throws<InvalidOperationException>(() => wfThread.Execute("test-task-name"));
            
        Assert.Equal("Using an inactive thread", exception.Message);
    }

    [Fact]
    public void WfThread_InvokingExecuteTasksWithArgs_ShouldReturnATaskTypeNodeOutput()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void ExecuteAction(WorkflowThread wf)
        {
            var expectedTaskName = "test-task-name";
            var variable = wf.AddVariable("str-test-variable", VariableType.Str);
            var actualNodeOutput = wf.Execute(expectedTaskName, variable);
            
            Assert.Contains(expectedTaskName + "-TASK", actualNodeOutput.NodeName);
        }
        
        new WorkflowThread(mockParentWorkflow.Object, ExecuteAction);
    }
    
    [Fact]
    public void WfThread_InvokingExecuteTasksWithNoArgs_ShouldReturnATaskTypeNodeOutput()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void ExecuteAction(WorkflowThread wf)
        {
            var expectedTaskName = "test-task-name";
            var actualNodeOutput = wf.Execute(expectedTaskName);
            
            Assert.Contains(expectedTaskName + "-TASK", actualNodeOutput.NodeName);
        }
        
        new WorkflowThread(mockParentWorkflow.Object, ExecuteAction);
    }

    [Fact]
    public void WfThread_InvokingExecuteTasksWithArgs_ShouldBuildSpecWithTaskNodeAndVariablesDefs()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            var variableDef = wf.AddVariable("str-test-variable", VariableType.Str);
            wf.Execute("test-task-name", variableDef);
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var actualSpec = workflowThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-test-task-name-TASK" }
            }
        };

        var taskNode = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "test-task-name" },
                Variables = { new VariableAssignment { VariableName = "str-test-variable" } }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-exit-EXIT" } }
        };
        
        var exitNode = new Node
        {
            Exit = new ExitNode()
        };
        
        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "str-test-variable",
                Type = VariableType.Str
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-test-task-name-TASK", taskNode);
        expectedSpec.Nodes.Add("2-exit-EXIT", exitNode);
        expectedSpec.VariableDefs.Add(threadVarDef);
        
        var expectedNumberOfNodes = 3;
        Assert.Equal(expectedNumberOfNodes, actualSpec.Nodes.Count);
        Assert.Equal(expectedSpec, actualSpec);
    }
    
    [Fact]
    public void WfThread_WithExternalEvent_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfExternalEvents = 1;
        var numberOfTasks = 1;
        var workflowName = "TestWorkflow";
        var timeoutInSeconds = 30;
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable name = wf.DeclareStr("name");
            name.Assign(wf.WaitForEvent("name-event").WithTimeout(timeoutInSeconds));
            wf.Execute("greet", name);
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-name-event-EXTERNAL_EVENT" }
            }
        };

        var externalEvent = new Node
        {
            ExternalEvent = new ExternalEventNode
            {
                ExternalEventDefId = new ExternalEventDefId { Name = "name-event" },
                TimeoutSeconds = new VariableAssignment { LiteralValue = new VariableValue { Int = timeoutInSeconds }}
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-greet-TASK",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "name",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput =
                                    new VariableAssignment.Types.NodeOutputReference
                                        { NodeName = "1-name-event-EXTERNAL_EVENT" }
                            }
                        }
                    }
                }
            }
        };

        var greetTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "greet" }, Variables =
                {
                    new VariableAssignment { VariableName = "name" }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "3-exit-EXIT" } }
        };

        var exitNode = new Node
        {
            Exit = new ExitNode()
        };
        
        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "name",
                Type = VariableType.Str
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-name-event-EXTERNAL_EVENT", externalEvent);
        expectedSpec.Nodes.Add("2-greet-TASK", greetTask);
        expectedSpec.Nodes.Add("3-exit-EXIT", exitNode);
        expectedSpec.VariableDefs.Add(threadVarDef);

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfExternalEvents + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }

    [Fact]
    public void WorkflowThread_WaitingManyChildThreads_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfStartThreadNodes = 3;
        var numberOfWaitForThreadsNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            var childThread1 = wf.SpawnThread("child-thread-1", thread => thread.Execute("any-task"));
            var childThread2 = wf.SpawnThread("child-thread-2", thread => thread.Execute("any-task"));
            var childThread3 = wf.SpawnThread("child-thread-3", thread => thread.Execute("any-task"));
            wf.WaitForThreads(SpawnedThreads.Of(childThread1, childThread2, childThread3));
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-child-thread-1-START_THREAD" }
            }
        };

        var childThread1 = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread-1"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-child-thread-2-START_THREAD",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "1-child-thread-1-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "1-child-thread-1-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };
        
        var childThread2 = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread-2"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-child-thread-3-START_THREAD",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-child-thread-2-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-child-thread-2-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };
        
        var childThread3 = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "child-thread-3"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "4-threads-WAIT_FOR_THREADS",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "3-child-thread-3-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "3-child-thread-3-START_THREAD"
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
                            ThreadRunNumber = new VariableAssignment { VariableName = "1-child-thread-1-START_THREAD" }
                        },
                        new WaitForThreadsNode.Types.ThreadToWaitFor
                        {
                            ThreadRunNumber = new VariableAssignment { VariableName = "2-child-thread-2-START_THREAD" }
                        },
                        new WaitForThreadsNode.Types.ThreadToWaitFor
                        {
                            ThreadRunNumber = new VariableAssignment { VariableName = "3-child-thread-3-START_THREAD" }
                        }
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "5-exit-EXIT" } }
        };
        
        var exit = new Node {Exit = new ExitNode()};

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef { Name = "1-child-thread-1-START_THREAD", Type = VariableType.Int },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        var threadVarDef2 = new ThreadVarDef
        {
            VarDef = new VariableDef { Name = "2-child-thread-2-START_THREAD", Type = VariableType.Int },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        var threadVarDef3 = new ThreadVarDef
        {
            VarDef = new VariableDef { Name = "3-child-thread-3-START_THREAD", Type = VariableType.Int },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };

        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-child-thread-1-START_THREAD", childThread1);
        expectedThreadSpec.Nodes.Add("2-child-thread-2-START_THREAD", childThread2);
        expectedThreadSpec.Nodes.Add("3-child-thread-3-START_THREAD", childThread3);
        expectedThreadSpec.Nodes.Add("4-threads-WAIT_FOR_THREADS", waitForThreads);
        expectedThreadSpec.Nodes.Add("5-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        expectedThreadSpec.VariableDefs.Add(threadVarDef2);
        expectedThreadSpec.VariableDefs.Add(threadVarDef3);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfStartThreadNodes + numberOfWaitForThreadsNodes + 
                                    numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }

    [Fact]
    public void WorkflowThread_WaitingNoChildThreads_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfWaitForThreadsNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.WaitForThreads(SpawnedThreads.Of());
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-threads-WAIT_FOR_THREADS" }
            }
        };

        var waitForThreads = new Node
        {
            WaitForThreads = new WaitForThreadsNode
            {
                Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor()
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-exit-EXIT" } }
        };
        
        var exit = new Node {Exit = new ExitNode()};
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-threads-WAIT_FOR_THREADS", waitForThreads);
        expectedThreadSpec.Nodes.Add("2-exit-EXIT", exit);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfWaitForThreadsNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
}