using System;
using LittleHorse.Sdk.Common.Proto;
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
        const int leftHandSide = 20;
        const int rightHandSide = 10;
        var numberOfNopNodes = 2;
        var numberOfTasks = 1;
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        void Entrypoint(WorkflowThread thread)
        {
            thread.DoIf(
                thread.Condition(leftHandSide, Comparator.GreaterThan, rightHandSide),
                ifThread => ifThread.Execute("task"));
        }
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var wfThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);
        
        var actualSpec = wfThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-nop-NOP" }
            }
        };

        var nop1Node = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-TASK",
                    Condition = new EdgeCondition
                    {
                        Comparator = Comparator.GreaterThan,
                        Left = new VariableAssignment { LiteralValue = new VariableValue { Int = leftHandSide } },
                        Right = new VariableAssignment { LiteralValue = new VariableValue { Int = rightHandSide } }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };

        var taskNode = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "task" }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };

        var nop3Node = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "4-exit-EXIT"
                }
            }
        };

        var exitNode = new Node
        {
            Exit = new ExitNode(),
        };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-nop-NOP", nop1Node);
        expectedSpec.Nodes.Add("2-task-TASK", taskNode);
        expectedSpec.Nodes.Add("3-nop-NOP", nop3Node);
        expectedSpec.Nodes.Add("4-exit-EXIT", exitNode);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfNopNodes + numberOfTasks;

        Assert.Equal(expectedNumberOfNodes, actualSpec.Nodes.Count);
        Assert.Equal(expectedSpec, actualSpec);
    }

    [Fact]
    public void WfThread_WithIfElseStatement_ShouldCompileAnSpecAddingTaskNodesBothStatements()
    {
        const int leftHandSide = 1;
        const int rightHandSide = 0;
        var numberOfNopNodes = 2;
        var numberOfTasks = 2;
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        void Entrypoint(WorkflowThread thread)
        {
            thread.DoIf(
                thread.Condition(leftHandSide, Comparator.LessThan, rightHandSide),
                ifThread => ifThread.Execute("task-a"),
                elseThread => elseThread.Execute("task-b"));
        }
        
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>("test-workflow", _action);
        var wfThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);
        
        var actualSpec = wfThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-nop-NOP" }
            }
        };

        var nop1Node = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-task-a-TASK",
                    Condition = new EdgeCondition
                    {
                        Comparator = Comparator.LessThan,
                        Left = new VariableAssignment { LiteralValue = new VariableValue { Int = leftHandSide } },
                        Right = new VariableAssignment { LiteralValue = new VariableValue { Int = rightHandSide } }
                    }
                },
                new Edge
                {
                    SinkNodeName = "4-task-b-TASK"
                }
            }
        };

        var taskA = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "task-a" }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };
        
        var taskB = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "task-b" }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-nop-NOP"
                }
            }
        };

        var nop3Node = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "5-exit-EXIT"
                }
            }
        };

        var exitNode = new Node
        {
            Exit = new ExitNode(),
        };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-nop-NOP", nop1Node);
        expectedSpec.Nodes.Add("2-task-a-TASK", taskA);
        expectedSpec.Nodes.Add("4-task-b-TASK", taskB);
        expectedSpec.Nodes.Add("3-nop-NOP", nop3Node);
        expectedSpec.Nodes.Add("5-exit-EXIT", exitNode);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfNopNodes + numberOfTasks;
        
        Assert.Equal(expectedNumberOfNodes, actualSpec.Nodes.Count);
        Assert.Equal(expectedSpec, actualSpec);
    }

    [Fact]
    public void WfThread_WithDoWhileStatement_ShouldCompileWithAVarMutationChangingTheCondition()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfNopNodes = 2;
        var numberOfTasks = 1;
        void Entrypoint(WorkflowThread thread)
        {
            var numDonuts = thread.DeclareInt("number-of-donuts").Required();
            thread.DoWhile(thread.Condition(numDonuts, Comparator.GreaterThan, 0),
                whileThread =>
                {
                    whileThread.Execute("eating-donut", numDonuts);
                    thread.Mutate(numDonuts, VariableMutationType.Assign, 0);
                });
        }
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>("test-workflow", _action);
        var wfThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);
        
        var compiledWfThread = wfThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-nop-NOP" }
            }
        };

        var nop1Node = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-eating-donut-TASK",
                    Condition = new EdgeCondition
                    {
                        Comparator = Comparator.GreaterThan,
                        Left = new VariableAssignment { VariableName = "number-of-donuts" },
                        Right = new VariableAssignment { LiteralValue = new VariableValue { Int = 0 } }
                    }
                },
                new Edge
                {
                    SinkNodeName = "3-nop-NOP",
                    Condition = new EdgeCondition
                    {
                        Comparator = Comparator.LessThanEq,
                        Left = new VariableAssignment { VariableName = "number-of-donuts" },
                        Right = new VariableAssignment { LiteralValue = new VariableValue { Int = 0 } }
                    }
                }
            }
        };

        var eatingDonutTaskNode = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "eating-donut" },
                Variables = { new VariableAssignment { VariableName = "number-of-donuts" } }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-nop-NOP", VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "number-of-donuts",
                            Operation = VariableMutationType.Assign,
                            RhsAssignment = new VariableAssignment { LiteralValue = new VariableValue { Int = 0 } }
                        }
                    }
                }
            }
        };

        var nop3Node = new Node
        {
            Nop = new NopNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "1-nop-NOP",
                    Condition = new EdgeCondition
                    {
                        Comparator = Comparator.GreaterThan,
                        Left = new VariableAssignment { VariableName = "number-of-donuts" },
                        Right = new VariableAssignment { LiteralValue = new VariableValue { Int = 0 } }
                    }
                },
                new Edge
                {
                    SinkNodeName = "4-exit-EXIT"
                }
            }
        };

        var exitNode = new Node
        {
            Exit = new ExitNode(),
        };

        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "number-of-donuts",
                Type = VariableType.Int
            },
            Required = true,
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-nop-NOP", nop1Node);
        expectedSpec.Nodes.Add("2-eating-donut-TASK", eatingDonutTaskNode);
        expectedSpec.Nodes.Add("3-nop-NOP", nop3Node);
        expectedSpec.Nodes.Add("4-exit-EXIT", exitNode);
        expectedSpec.VariableDefs.Add(threadVarDef);
        
        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfNopNodes + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }
}