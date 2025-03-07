using System;
using System.Collections.Generic;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowThreadChildThreadsTest
{
    private readonly Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowThreadChildThreadsTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void SpawnThread_WithParams_ShouldCompileSuccessfully()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var numberOfStartThreadNodes = 1;
        var numberOfTaskNodesInMainThread = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfExitNodes = 1;

        void EntryPointAction(WorkflowThread wf)
        {
            var parentVar = wf.DeclareInt("parent-var");
            wf.Execute("parent-task");
            wf.SpawnThread(
                "spawned-thread",
                child =>
                {
                    var childVar = child.DeclareInt("child-var");
                    var childVar2 = child.DeclareInt("child-var2");
                    child.Execute("child-task", childVar, childVar2);
                },
                new Dictionary<string, object>
                {
                    {
                        "child-var", parentVar
                    },
                    {
                        "child-var2", parentVar
                    }
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
                new Edge { SinkNodeName = "1-parent-task-TASK" }
            }
        };
        var task = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "parent-task" }
            },
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "2-spawned-thread-START_THREAD" }
            }
        };

        var spawnThread = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "spawned-thread",
                Variables =
                {
                    { "child-var", new VariableAssignment { VariableName = "parent-var" } },
                    { "child-var2", new VariableAssignment { VariableName = "parent-var" } }
                }
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-exit-EXIT",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-spawned-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-spawned-thread-START_THREAD",
                                }
                            }
                        }
                    }
                }
            }
        };
        
        var exit = new Node {Exit = new ExitNode()};

        var threadVarDef1 = new ThreadVarDef
        {
            VarDef = new VariableDef { Name = "parent-var", Type = VariableType.Int },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        var threadVarDef2 = new ThreadVarDef
        {
            VarDef = new VariableDef { Name = "2-spawned-thread-START_THREAD", Type = VariableType.Int },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };

        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-parent-task-TASK", task);
        expectedThreadSpec.Nodes.Add("2-spawned-thread-START_THREAD", spawnThread);
        expectedThreadSpec.Nodes.Add("3-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef1);
        expectedThreadSpec.VariableDefs.Add(threadVarDef2);
        
        var expectedNumberOfNodes = numberOfStartThreadNodes + numberOfTaskNodesInMainThread + numberOfEntrypointNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
    
    [Fact]
    public void SpawnThread_WithoutParams_ShouldCompileSuccessfully()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var numberOfStartThreadNodes = 1;
        var numberOfTaskNodesInMainThread = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfExitNodes = 1;

        void EntryPointAction(WorkflowThread wf)
        {
            wf.Execute("parent-task");
            wf.SpawnThread(
                "spawned-thread",
                child =>
                {
                    child.Execute("child-task");
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
                new Edge { SinkNodeName = "1-parent-task-TASK" }
            }
        };
        var task = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "parent-task" }
            },
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "2-spawned-thread-START_THREAD" }
            }
        };

        var spawnThread = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "spawned-thread"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "3-exit-EXIT",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "2-spawned-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "2-spawned-thread-START_THREAD",
                                }
                            }
                        }
                    }
                }
            }
        };
        
        var exit = new Node {Exit = new ExitNode()};
        
        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef { Name = "2-spawned-thread-START_THREAD", Type = VariableType.Int },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };

        expectedThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedThreadSpec.Nodes.Add("1-parent-task-TASK", task);
        expectedThreadSpec.Nodes.Add("2-spawned-thread-START_THREAD", spawnThread);
        expectedThreadSpec.Nodes.Add("3-exit-EXIT", exit);
        expectedThreadSpec.VariableDefs.Add(threadVarDef);
        
        var expectedNumberOfNodes = numberOfStartThreadNodes + numberOfTaskNodesInMainThread + numberOfEntrypointNodes + numberOfExitNodes;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedThreadSpec, compiledWfThread);
    }
}