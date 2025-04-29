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
            thread.DoIfWithResult(thread.Condition(5, Comparator.GreaterThanEq, 9),
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
            WorkflowIfStatement ifStatement = thread.DoIfWithResult(thread.Condition(5, Comparator.GreaterThanEq, 9),
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
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, MyEntrypoint);
        
        var compiledWfThread = workflowThread.Compile();
        var startNopNode = compiledWfThread.Nodes["1-nop-NOP"];
        var taskNodeA = compiledWfThread.Nodes["2-task-a-TASK"];
        var taskNodeB = compiledWfThread.Nodes["4-task-b-TASK"];
        var taskNodeC = compiledWfThread.Nodes["5-task-c-TASK"];
        var lastNopNode = compiledWfThread.Nodes["3-nop-NOP"];

        var expectedNumberOutgoingEdgesFromFirstNopNode = 4;
        var expectedLastSinkNopNodeName = "3-nop-NOP";
        var expectedExitSinkNodeName = "6-exit-EXIT";

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
        Assert.Equal(expectedExitSinkNodeName, lastNopNode.OutgoingEdges[0].SinkNodeName);
    }
}