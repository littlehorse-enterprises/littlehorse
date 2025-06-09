using System;
using System.Collections.Generic;
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
                TypeDef = new TypeDefinition { Type = VariableType.Str }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };

        var intVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "int-test-variable",
                TypeDef = new TypeDefinition { Type = VariableType.Int },
                DefaultValue = new VariableValue { Int = 5 }
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
            var variable = wf.DeclareStr("str-test-variable");
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
            var variableDef = wf.DeclareStr("str-test-variable");
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
                TypeDef = new TypeDefinition { Type = VariableType.Str }
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
                TypeDef = new TypeDefinition { Type = VariableType.Str }
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
    public void WfThread_WithCorrelaatedExternalEvent_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfExternalEvents = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable name = wf.DeclareStr("name");
            wf.WaitForEvent("name-event").withCorrelationId(name);
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
                CorrelationKey = new VariableAssignment { VariableName = "name" }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-exit-EXIT",
                }
            }
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
                TypeDef = new TypeDefinition { Type = VariableType.Str }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-name-event-EXTERNAL_EVENT", externalEvent);
        expectedSpec.Nodes.Add("2-exit-EXIT", exitNode);
        expectedSpec.VariableDefs.Add(threadVarDef);

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfExternalEvents;
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
            VarDef = new VariableDef
            {
                Name = "1-child-thread-1-START_THREAD",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };

        var threadVarDef2 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "2-child-thread-2-START_THREAD",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };

        var threadVarDef3 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "3-child-thread-3-START_THREAD",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
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

    [Fact]
    public void WorkflowThread_WithInterruptDefAndSleepNode_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTaskNodesInMainThread = 1;
        var numberOfSleepNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            wf.RegisterInterruptHandler(
                "interruption-event",
                handler =>
                {
                    handler.Execute("some-task");
                });
            wf.SleepSeconds(30);
            wf.Execute("my-task");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-sleep-SLEEP" }
            }
        };

        var sleepNode = new Node
        {
            Sleep = new SleepNode
            {
                RawSeconds = new VariableAssignment
                {
                    LiteralValue = new VariableValue { Int = 30 }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-my-task-TASK" } }
        };

        var taskNode = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId
                {
                    Name = "my-task"
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "3-exit-EXIT" } }
        };
        
        var exit = new Node { Exit = new ExitNode() };

        var interruptDef = new InterruptDef
        {
            ExternalEventDefId = new ExternalEventDefId { Name = "interruption-event" },
            HandlerSpecName = "interrupt-interruption-event"
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-sleep-SLEEP", sleepNode);
        expectedThreadSpec.Nodes.Add("2-my-task-TASK", taskNode);
        expectedThreadSpec.Nodes.Add("3-exit-EXIT", exit);
        expectedThreadSpec.InterruptDefs.Add(interruptDef);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfSleepNodes + numberOfTaskNodesInMainThread
                                    + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void WorkflowThread_WithSleepNodeUntilAWfRunIntVariableReachesDeadline_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfTaskNodesInMainThread = 1;
        var numberOfSleepNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable timeout = wf.DeclareInt("timeout");
            wf.SleepUntil(timeout);
            wf.Execute("my-task");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-sleep-SLEEP" }
            }
        };

        var sleepNode = new Node
        {
            Sleep = new SleepNode
            {
                Timestamp = new VariableAssignment
                {
                    VariableName = "timeout"
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-my-task-TASK" } }
        };

        var taskNode = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId
                {
                    Name = "my-task"
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "3-exit-EXIT" } }
        };
        
        var exit = new Node { Exit = new ExitNode() };

        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "timeout",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-sleep-SLEEP", sleepNode);
        expectedThreadSpec.Nodes.Add("2-my-task-TASK", taskNode);
        expectedThreadSpec.Nodes.Add("3-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfSleepNodes + numberOfTaskNodesInMainThread
                                    + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }

    [Fact]
    public void WorkflowThread_WaitForConditionNode_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfWaitForConditionsNodes = 1;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable counter = wf.DeclareInt("counter").WithDefault(2);

            wf.WaitForCondition(wf.Condition(counter, Comparator.Equals, 0));

            // Interrupt handler which mutates the parent variable
            wf.RegisterInterruptHandler("change-counter-event", handler =>
            {
                handler.Mutate(counter, VariableMutationType.Subtract, 1);
            });
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-wait-for-condition-WAIT_FOR_CONDITION" }
            }
        };
        
        var waitForConditionNode = new Node
        {
            WaitForCondition = new WaitForConditionNode
            {
                Condition = new EdgeCondition
                {
                    Left = new VariableAssignment { VariableName = "counter" },
                    Comparator = Comparator.Equals,
                    Right = new VariableAssignment { LiteralValue = new VariableValue { Int = 0 } }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-exit-EXIT" } }
        };
        
        var exit = new Node { Exit = new ExitNode() };

        var threadVarDefs = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "counter",
                TypeDef = new TypeDefinition { Type = VariableType.Int },
                DefaultValue = new VariableValue { Int = 2 }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };

        var interruptDef = new InterruptDef
        {
            ExternalEventDefId = new ExternalEventDefId { Name = "change-counter-event" },
            HandlerSpecName = "interrupt-change-counter-event"
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-wait-for-condition-WAIT_FOR_CONDITION", waitForConditionNode);
        expectedThreadSpec.Nodes.Add("2-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDefs);
        expectedThreadSpec.InterruptDefs.Add(interruptDef);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfWaitForConditionsNodes
                                    + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }

    [Fact]
    public void WorkflowThread_ThrowingManyEvents_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfThrowEventNodes = 3;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable eventInput = wf.DeclareStr("input");
            WfRunVariable eventPayload = wf.DeclareJsonObj("complex-data");

            wf.ThrowEvent("one-event", eventInput);
            wf.ThrowEvent("other-event", eventPayload);
            wf.ThrowEvent("another-event", "any-content");
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-throw-one-event-THROW_EVENT" }
            }
        };
        
        var throwEventNode1 = new Node
        {
            ThrowEvent = new ThrowEventNode
            {
                EventDefId = new WorkflowEventDefId { Name = "one-event" },
                Content = new VariableAssignment { VariableName = "input" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-throw-other-event-THROW_EVENT" } }
        };
        
        var throwEventNode2 = new Node
        {
            ThrowEvent = new ThrowEventNode
            {
                EventDefId = new WorkflowEventDefId { Name = "other-event" },
                Content = new VariableAssignment { VariableName = "complex-data" }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "3-throw-another-event-THROW_EVENT" } }
        };
        
        var throwEventNode3 = new Node
        {
            ThrowEvent = new ThrowEventNode
            {
                EventDefId = new WorkflowEventDefId { Name = "another-event" },
                Content = new VariableAssignment { LiteralValue = new VariableValue { Str = "any-content" } }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "4-exit-EXIT" } }
        };
        
        var exit = new Node { Exit = new ExitNode() };

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "input",
                TypeDef = new TypeDefinition { Type = VariableType.Str }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        var threadVarDef2 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "complex-data",
                TypeDef = new TypeDefinition { Type = VariableType.JsonObj }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-throw-one-event-THROW_EVENT", throwEventNode1);
        expectedThreadSpec.Nodes.Add("2-throw-other-event-THROW_EVENT", throwEventNode2);
        expectedThreadSpec.Nodes.Add("3-throw-another-event-THROW_EVENT", throwEventNode3);
        expectedThreadSpec.Nodes.Add("4-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        expectedThreadSpec.VariableDefs.Add(threadVarDef2);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfThrowEventNodes
                                                            + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }

    [Fact]
    public void WorkflowThread_WithNullDefaultRetentionPolicyInParent_ShouldCompileWithoutRetentionPolicyInThread()
    {
        var workflowName = "TestWorkflow";
        var workflow = new Sdk.Workflow.Spec.Workflow(workflowName, _action);
        workflow.WithDefaultThreadRetentionPolicy(null);

        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task-name");
        }

        var workflowThread = new WorkflowThread(workflow, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        
        Assert.Null(compiledWfThread.RetentionPolicy);
    }
    
    [Fact]
    public void WorkflowThread_WithRetentionPolicyInThread_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var workflow = new Sdk.Workflow.Spec.Workflow(workflowName, _action);
        var secondsRetentionPolicy = 120;

        void EntryPointAction(WorkflowThread wf)
        {
            wf.WithRetentionPolicy(new ThreadRetentionPolicy
            {
                SecondsAfterThreadTermination = secondsRetentionPolicy
            });
        }

        var workflowThread = new WorkflowThread(workflow, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        
        Assert.Null(workflow.GetDefaultThreadRetentionPolicy());
        Assert.Equal(secondsRetentionPolicy, compiledWfThread.RetentionPolicy.SecondsAfterThreadTermination);
    }
    
    [Fact]
    public void WorkflowThread_WitDefaultRetentionPolicyInParent_ShouldCompileWithRetentionPolicyInThread()
    {
        var workflowName = "TestWorkflow";
        var workflow = new Sdk.Workflow.Spec.Workflow(workflowName, _action);
        var secondsRetentionPolicy = 120;
        workflow.WithDefaultThreadRetentionPolicy(new ThreadRetentionPolicy 
            { SecondsAfterThreadTermination = secondsRetentionPolicy });

        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task-name");
        }

        var workflowThread = new WorkflowThread(workflow, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        
        Assert.Equal(workflow.GetDefaultThreadRetentionPolicy(), compiledWfThread.RetentionPolicy);
        Assert.Equal(secondsRetentionPolicy, compiledWfThread.RetentionPolicy.SecondsAfterThreadTermination);
    }
    
    [Fact]
    public void WorkflowThread_WithWfRunVariableAsTaskNameAndArgsInExecuteTask_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable taskDef = wf.DeclareStr("task-name");
            WfRunVariable input1 = wf.DeclareStr("input");
            WfRunVariable input2 = wf.DeclareJsonObj("complex-data");
            
            wf.Execute(taskDef, input1, input2);
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();

        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-task-name-TASK" }
            }
        };

        var task = new Node
        {
            Task = new TaskNode
            {
                Variables =
                {
                    new VariableAssignment { VariableName = "input" },
                    new VariableAssignment { VariableName = "complex-data" }
                },
                DynamicTask = new VariableAssignment { VariableName = "task-name" }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-exit-EXIT"
                }
            }
        };
        
        var exit = new Node { Exit = new ExitNode() };
        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "task-name",
                TypeDef = new TypeDefinition { Type = VariableType.Str }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        var threadVarDef2 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "input",
                TypeDef = new TypeDefinition { Type = VariableType.Str }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        var threadVarDef3 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "complex-data",
                TypeDef = new TypeDefinition { Type = VariableType.JsonObj }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-task-name-TASK", task);
        expectedThreadSpec.Nodes.Add("2-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        expectedThreadSpec.VariableDefs.Add(threadVarDef2);
        expectedThreadSpec.VariableDefs.Add(threadVarDef3);
        var numberOfNodes = 3;
        
        Assert.Equal(numberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void WorkflowThread_WithFormattedStringAsTaskNameAndArgsInExecuteTask_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable taskDef = wf.DeclareStr("task-name");
            WfRunVariable input1 = wf.DeclareStr("input");
            WfRunVariable input2 = wf.DeclareJsonObj("complex-data");
            
            wf.Execute(wf.Format("prefix-{}-suffix", taskDef), input1, input2);
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();

        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-prefix-{}-suffix-TASK" }
            }
        };

        var task = new Node
        {
            Task = new TaskNode
            {
                Variables =
                {
                    new VariableAssignment { VariableName = "input" },
                    new VariableAssignment { VariableName = "complex-data" }
                },
                DynamicTask = new VariableAssignment
                {
                    FormatString = new VariableAssignment.Types.FormatString
                    {
                        Format = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Str = "prefix-{}-suffix" }
                        },
                        Args =
                        {
                            new VariableAssignment { VariableName = "task-name" }
                        }
                    }
                }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-exit-EXIT"
                }
            }
        };
        
        var exit = new Node { Exit = new ExitNode() };
        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "task-name",
                TypeDef = new TypeDefinition { Type = VariableType.Str }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        var threadVarDef2 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "input",
                TypeDef = new TypeDefinition { Type = VariableType.Str }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        var threadVarDef3 = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "complex-data",
                TypeDef = new TypeDefinition { Type = VariableType.JsonObj }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-prefix-{}-suffix-TASK", task);
        expectedThreadSpec.Nodes.Add("2-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        expectedThreadSpec.VariableDefs.Add(threadVarDef2);
        expectedThreadSpec.VariableDefs.Add(threadVarDef3);
        var numberOfNodes = 3;
        
        Assert.Equal(numberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void WorkflowThread_WithExitNode_ShouldCompileWithCompleteName()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("any-task-def-name");
            wf.Complete();
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();

        var expectedThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-any-task-def-name-TASK" }
            }
        };

        var task = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "any-task-def-name" }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-complete-EXIT"
                }
            }
        };
        
        var exit = new Node { Exit = new ExitNode() };
        
        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-any-task-def-name-TASK", task);
        expectedThreadSpec.Nodes.Add("2-complete-EXIT", exit);
        var numberOfNodes = 3;
        
        Assert.Equal(numberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }

    [Fact]
    public void WorkflowThread_WithNullParentWorkflow_ShouldThrowArgumentNullException()
    {
        var exception = Assert.Throws<ArgumentNullException>(() =>
            new WorkflowThread(null!, _action));
        
        Assert.Equal("Value cannot be null. (Parameter 'parent')", exception.Message);
    }

    [Fact]
    public void WorkflowThread_UsingDoesContainAsCondition_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var data = wf.DeclareJsonArr("data");
            wf.DoIf(data.DoesContain("address"), body =>
            {
                body.Execute("add-address");
            });
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        var actualNode = compiledWfThread.Nodes["1-nop-NOP"];
        
        var expectedNode = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-add-address-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue
                            {
                                Str = "address"
                            }
                        },
                        Comparator = Comparator.In,
                        Right = new VariableAssignment
                        {
                            VariableName = "data"
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };
        
        Assert.Equal(expectedNode, actualNode);
    }
    
    [Fact]
    public void WorkflowThread_UsingDoesNotContainAsCondition_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var data = wf.DeclareJsonArr("data");
            wf.DoIf(data.DoesNotContain("address"), body =>
            {
                body.Execute("add-address");
            });
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        var actualNode = compiledWfThread.Nodes["1-nop-NOP"];
        
        var expectedNode = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-add-address-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue
                            {
                                Str = "address"
                            }
                        },
                        Comparator = Comparator.NotIn,
                        Right = new VariableAssignment
                        {
                            VariableName = "data"
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };
        
        Assert.Equal(expectedNode, actualNode);
    }
    
    [Fact]
    public void WorkflowThread_UsingInAsCondition_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var input = wf.DeclareStr("input");
            wf.DoIf(input.IsIn(new List<string> {"A", "B", "C"}), body =>
            {
                body.Execute("task");
            });
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        var actualNode = compiledWfThread.Nodes["1-nop-NOP"];
        
        var expectedNode = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            VariableName = "input"
                        },
                        Comparator = Comparator.In,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue
                            {
                                JsonArr = "[\"A\",\"B\",\"C\"]"
                            }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };
        
        Assert.Equal(expectedNode, actualNode);
    }
    
    [Fact]
    public void WorkflowThread_UsingNotInAsCondition_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var input = wf.DeclareStr("input");
            wf.DoIf(input.IsNotIn(new List<string> {"A", "B", "C"}), body =>
            {
                body.Execute("task");
            });
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        var actualNode = compiledWfThread.Nodes["1-nop-NOP"];
        
        var expectedNode = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            VariableName = "input"
                        },
                        Comparator = Comparator.NotIn,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue
                            {
                                JsonArr = "[\"A\",\"B\",\"C\"]"
                            }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };
        
        Assert.Equal(expectedNode, actualNode);
    }

    [Theory]
    [InlineData(Comparator.LessThan, 23)]
    [InlineData(Comparator.LessThanEq, 26)]
    [InlineData(Comparator.GreaterThanEq, 5)]
    [InlineData(Comparator.GreaterThan, 7)]
    [InlineData(Comparator.Equals, 9)]
    [InlineData(Comparator.NotEquals, 10000)]
    public void WorkflowThread_UsingDifferentConditionals_ShouldCompile(Comparator comparator, int value)
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var input = wf.DeclareInt("input");
            wf.DoIf(wf.Condition(input, comparator, value), body =>
            {
                body.Execute("task");
            });
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);

        var compiledWfThread = workflowThread.Compile();
        var actualNode = compiledWfThread.Nodes["1-nop-NOP"];
        
        var expectedNode = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            VariableName = "input"
                        },
                        Comparator = comparator,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue
                            {
                                Int = value
                            }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };
        
        Assert.Equal(expectedNode, actualNode);
    }
}