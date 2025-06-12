using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowIfStatementTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowIfStatementTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }

    [Fact]
    public void WorkFlowThread_WithDoIf_ShouldCreateAnEdgeWithTheConditionToDefaultLastNopNode()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void MyEntrypoint(WorkflowThread thread)
        {
            thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body => {});
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);

        var compiledWfThread = workflowThread.Compile();
        var actualFirstNopNode = compiledWfThread.Nodes["1-nop-NOP"];
        
        Assert.Equal(new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-nop-NOP",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment 
                        {
                            LiteralValue = new VariableValue {Int = 5}
                        },
                        Comparator = Comparator.GreaterThanEq,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue {Int = 9}
                        }
                    }   
                },
                new Edge { SinkNodeName = "2-nop-NOP" }
            }
        }, actualFirstNopNode);
    }
    
    [Fact]
    public void WorkflowThread_WithMultipleIfElseConditions_ShouldCompileTaskNodesArrowedToLastNopNode()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            var myInt = thread.DeclareInt("my-int");
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Execute("task-a");
                    myInt.Assign(9);
                });

            ifStatement.DoElseIf(thread.Condition(7, Comparator.LessThan, 4),
                body =>
                {
                    myInt.Assign(10);
                    body.Execute("task-b");
                });

            ifStatement.DoElseIf(thread.Condition(5, Comparator.Equals, 5),
                body => body.Execute("task-c"));

            myInt.Assign(0) ;
            thread.Execute("task-d");
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
        
        var compiledWfThread = workflowThread.Compile();
        var startNopNode = compiledWfThread.Nodes["1-nop-NOP"];
        var taskNodeA = compiledWfThread.Nodes["2-task-a-TASK"];
        var taskNodeB = compiledWfThread.Nodes["4-task-b-TASK"];
        var taskNodeC = compiledWfThread.Nodes["5-task-c-TASK"];
        var taskNodeD = compiledWfThread.Nodes["6-task-d-TASK"];
        var lastNopNode = compiledWfThread.Nodes["3-nop-NOP"];

        var expectedNumberOutgoingEdgesFromFirstNopNode = 4;
        var expectedLastSinkNopNodeName = "3-nop-NOP";
        var expectedExitSinkNodeName = "7-exit-EXIT";

        Assert.Equal(new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-a-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 5 }
                        },
                        Comparator = Comparator.GreaterThanEq,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 9 }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "4-task-b-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 7 }
                        },
                        Comparator = Comparator.LessThan,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 4 }
                        }
                    },
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "my-int",
                            Operation = VariableMutationType.Assign,
                            RhsAssignment = new VariableAssignment
                            {
                                LiteralValue = new VariableValue { Int = 10 }
                            }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "5-task-c-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 5 }
                        },
                        Comparator = Comparator.Equals,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 5 }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        }, startNopNode);
        
        Assert.Equal(new VariableMutation {
            LhsName = "my-int",
            Operation = VariableMutationType.Assign,
            RhsAssignment = new VariableAssignment
            {
                LiteralValue = new VariableValue {Int = 0}
            }
        }, lastNopNode.OutgoingEdges[0].VariableMutations[0]);
        Assert.Equal(expectedNumberOutgoingEdgesFromFirstNopNode, startNopNode.OutgoingEdges.Count);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeA.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeB.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeC.OutgoingEdges[0].SinkNodeName);
        Assert.Equal("6-task-d-TASK", lastNopNode.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedExitSinkNodeName, taskNodeD.OutgoingEdges[0].SinkNodeName);
    }
    
    
    [Fact]
    public void WorkflowThread_WithMultipleConditionsAndElseOne_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            var myInt = thread.DeclareInt("my-int");
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Execute("task-a");
                    myInt.Assign(9);
                });

            ifStatement.DoElseIf(thread.Condition(7, Comparator.LessThan, 4), body =>
            {
                myInt.Assign(10);
                body.Execute("task-b");
            });

            ifStatement.DoElse(body => body.Execute("task-c"));
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
        
        var compiledWfThread = workflowThread.Compile();
        var startNopNode = compiledWfThread.Nodes["1-nop-NOP"];
        var taskNodeA = compiledWfThread.Nodes["2-task-a-TASK"];
        var taskNodeB = compiledWfThread.Nodes["4-task-b-TASK"];
        var taskNodeC = compiledWfThread.Nodes["5-task-c-TASK"];
        var lastNopNode = compiledWfThread.Nodes["3-nop-NOP"];

        var expectedNumberOutgoingEdgesFromFirstNopNode = 3;
        var expectedLastSinkNopNodeName = "3-nop-NOP";
        var expectedExitSinkNodeName = "6-exit-EXIT";

        Assert.Equal(expectedNumberOutgoingEdgesFromFirstNopNode, startNopNode.OutgoingEdges.Count);
        Assert.Equal(new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-a-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 5 }
                        },
                        Comparator = Comparator.GreaterThanEq,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 9 }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "4-task-b-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 7 }
                        },
                        Comparator = Comparator.LessThan,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 4 }
                        }
                    },
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "my-int",
                            Operation = VariableMutationType.Assign,
                            RhsAssignment = new VariableAssignment
                            {
                                LiteralValue = new VariableValue { Int = 10 }
                            }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "5-task-c-TASK"
                }
            }
        }, startNopNode);
        
        Assert.Equal(new VariableMutation {
            LhsName = "my-int",
            Operation = VariableMutationType.Assign,
            RhsAssignment = new VariableAssignment
            {
                LiteralValue = new VariableValue {Int = 9}
            }
        }, taskNodeA.OutgoingEdges[0].VariableMutations[0]);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeA.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeB.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeC.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedExitSinkNodeName, lastNopNode.OutgoingEdges[0].SinkNodeName);
    }

    [Fact]
    public void WorkflowThread_WithMoreThanOneElseStatement_ShouldThrownAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        new WorkflowThread(mockParentWorkflow.Object, thread =>
        {
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body => body.Execute("task-a"));
            ifStatement.DoElse(body => body.Execute("task-b"));
            
            var exception = Assert.Throws<InvalidOperationException>(() =>
                ifStatement.DoElse(body => body.Execute("task-c")));
        
            Assert.Equal("Else block has already been executed. Cannot add another else block.", exception.Message);
        });
    }
    
    [Fact]
    public void WorkflowThread_WithDoElseIfCalledInTheMiddleOfOtherLHStatements_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Execute("task-a");
                });
            thread.Execute("task-c");
            ifStatement.DoElseIf(thread.Condition(7, Comparator.LessThan, 4), body =>
            {
                body.Execute("task-b");
            });
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
        
        var compiledWfThread = workflowThread.Compile();
        
        var startNopNode = compiledWfThread.Nodes["1-nop-NOP"];
        var taskNodeA = compiledWfThread.Nodes["2-task-a-TASK"];
        var taskNodeB = compiledWfThread.Nodes["5-task-b-TASK"];
        var taskNodeC = compiledWfThread.Nodes["4-task-c-TASK"];
        var lastNopNode = compiledWfThread.Nodes["3-nop-NOP"];

        var expectedNumberOutgoingEdgesFromFirstNopNode = 3;
        var expectedLastSinkNopNodeName = "3-nop-NOP";
        var expectedExitSinkNodeName = "6-exit-EXIT";

        Assert.Equal(expectedNumberOutgoingEdgesFromFirstNopNode, startNopNode.OutgoingEdges.Count);
        Assert.Equal(new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-a-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 5 }
                        },
                        Comparator = Comparator.GreaterThanEq,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 9 }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "5-task-b-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 7 }
                        },
                        Comparator = Comparator.LessThan,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 4 }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        }, startNopNode);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeA.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedLastSinkNopNodeName, taskNodeB.OutgoingEdges[0].SinkNodeName);
        Assert.Equal(expectedExitSinkNodeName, taskNodeC.OutgoingEdges[0].SinkNodeName);
        Assert.Equal("4-task-c-TASK", lastNopNode.OutgoingEdges[0].SinkNodeName);
    }

    [Fact]
    public void WorkflowThread_CallingACompleteInDoIf_ShouldCompileSpecWithElseBody()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            var test = thread.DeclareInt("test");
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Complete();
                });
            ifStatement.DoElse(body => body.Execute("task-b"));
            
            test.Assign(10);
            thread.Execute("task-a");
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
        
        var compiledWfThread = workflowThread.Compile();

        var threadSpec = new ThreadSpec();
        var entryPointNode = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "1-nop-NOP"
                }
            }
        };

        var firstNopNode = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-complete-EXIT",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 5 }
                        },
                        Comparator = Comparator.GreaterThanEq,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 9 }
                        }
                    }
                },
                new Edge {SinkNodeName = "4-task-b-TASK"}
            }
        };

        var lastNopNode = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "5-task-a-TASK",
                    VariableMutations = { new VariableMutation
                    {
                        LhsName = "test",
                        Operation = VariableMutationType.Assign,
                        RhsAssignment = new VariableAssignment
                        {
                            LiteralValue = new VariableValue
                            {
                                Int = 10
                            }
                        }
                    } }
                }
            }
        };
        
        var taskANode = new Node
        {
            Task = new TaskNode { TaskDefId = new TaskDefId { Name = "task-a"}},
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "6-exit-EXIT"
                }
            }
        };
        
        var taskBNode = new Node
        {
            Task = new TaskNode { TaskDefId = new TaskDefId { Name = "task-b"}},
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };

        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "test",
                TypeDef = new TypeDefinition { Type = VariableType.Int }
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        threadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entryPointNode);
        threadSpec.Nodes.Add("1-nop-NOP", firstNopNode);
        threadSpec.Nodes.Add("2-complete-EXIT", new Node { Exit = new ExitNode() });
        threadSpec.Nodes.Add("3-nop-NOP", lastNopNode);
        threadSpec.Nodes.Add("4-task-b-TASK", taskBNode);
        threadSpec.Nodes.Add("5-task-a-TASK", taskANode);
        threadSpec.Nodes.Add("6-exit-EXIT", new Node { Exit = new ExitNode() });
        threadSpec.VariableDefs.Add(threadVarDef);
        
        Assert.Equal(threadSpec, compiledWfThread);
    }

    [Fact]
    public void WorkflowThread_ExecutingATaskAfterCompleteMethod_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            var test = thread.DeclareInt("test");
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Complete();
                    var exception = Assert.Throws<InvalidOperationException>(() =>
                        body.Execute("task-a"));
        
                    Assert.Equal("You cannot add a Node in a given thread after the thread has completed.", exception.Message);
                });
            ifStatement.DoElse(body => body.Execute("task-b"));
        }

        _ = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
    }
    
    [Fact]
    public void WorkflowThread_MutatingAVariableAfterCompleteMethod_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            var test = thread.DeclareInt("test");
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Complete();
                    var exception = Assert.Throws<InvalidOperationException>(() => test.Assign(10));
        
                    Assert.Equal("You cannot mutate a variable in a given thread after the thread has completed.", exception.Message);
                });
            ifStatement.DoElse(body => body.Execute("task-b"));
        }

        _ = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
    }

    [Fact] public void WorkflowThread_DeclaringAVariableAfterCompleteMethod_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            var test = thread.DeclareInt("test");
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Complete();
                    var exception = Assert.Throws<InvalidOperationException>(() => body.DeclareStr("other-var"));
        
                    Assert.Equal("You cannot add a variable in a given thread after the thread has completed.", 
                        exception.Message);
                });
            ifStatement.DoElse(body => body.Execute("task-b"));
        }

        _ = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
    }

    [Fact]
    public void WorkflowThread_WithCompleteNodesInDoIfAndDoElse_ShouldCompile()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void MyEntrypoint(WorkflowThread thread)
        {
            WorkflowIfStatement ifStatement = thread.DoIf(thread.Condition(5, Comparator.GreaterThanEq, 9),
                body =>
                {
                    body.Complete();
                });
            ifStatement.DoElseIf(thread.Condition(2, Comparator.LessThan, 4), body =>
            {
                body.Execute("task-b");
                body.Complete();
            });
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
        
        var compiledWfThread = workflowThread.Compile();
        
        var threadSpec = new ThreadSpec();
        threadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-nop-NOP" }
            }
        });
        threadSpec.Nodes.Add("1-nop-NOP", new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-complete-EXIT",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 5 }
                        },
                        Comparator = Comparator.GreaterThanEq,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 9 }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "4-task-b-TASK",
                    Condition = new EdgeCondition
                    {
                        Left = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 2 }
                        },
                        Comparator = Comparator.LessThan,
                        Right = new VariableAssignment
                        {
                            LiteralValue = new VariableValue { Int = 4 }
                        }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        });
        
        threadSpec.Nodes.Add("2-complete-EXIT", new Node { Exit = new ExitNode() });
        threadSpec.Nodes.Add("3-nop-NOP", new Node
        {
            Nop = new NopNode(),
            OutgoingEdges = { new Edge
            {
                SinkNodeName = "6-exit-EXIT",
            } }
        });
        
        threadSpec.Nodes.Add("4-task-b-TASK", new Node
        {
            Task = new TaskNode { TaskDefId = new TaskDefId { Name = "task-b" }},
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "5-complete-EXIT"
                }
            }
        });
        threadSpec.Nodes.Add("5-complete-EXIT", new Node { Exit = new ExitNode() });
        threadSpec.Nodes.Add("6-exit-EXIT", new Node { Exit = new ExitNode() });
        
        Assert.Equal(threadSpec, compiledWfThread);
    }
}